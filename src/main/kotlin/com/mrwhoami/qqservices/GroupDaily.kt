package com.mrwhoami.qqservices

import com.beust.klaxon.Klaxon
import mu.KotlinLogging
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOwner
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.content
import java.io.File

class GroupDaily {
    private var grp2AdminLock : HashMap<Long, Long> = hashMapOf()
    private var grp2Msg : MutableMap<Long, String> = mutableMapOf()
    private val baseDirName = "res/GroupDaily/"
    private val filename = "group_message.csv"
    private val logger = KotlinLogging.logger {}

    data class GrpMsgJsonObject (val group : Long, val message : String)

    private fun saveMapToJson() {
        var buffer = "["
        var isStart = true
        for (it in grp2Msg) {
            if (!isStart) buffer += ","
            buffer += "{\"group\":${it.key},\"message\":\"${it.value}\"}"
            isStart = false
        }
        buffer += "]"
        val jsonFile = File(baseDirName, filename)
        jsonFile.writeText(buffer)
    }

    init {
        // Create the config file folder any way.
        val baseDir = File(baseDirName)
        baseDir.mkdirs()
        val jsonFile = File(baseDir, filename)
        if (jsonFile.exists()) {
            val jsonObjList = Klaxon().parseArray<GrpMsgJsonObject>(jsonFile)
            if (jsonObjList != null) {
                for (it in jsonObjList) grp2Msg[it.group] = it.message
            } else {
                logger.warn { "Failed to read $baseDirName$filename." }
            }
        } else {
            logger.info { "$baseDirName$filename doesn't exist." }
        }
    }
    
    private fun messageChainToPureText (messageChain : MessageChain) : String? {
        var buffer = ""
        for (msg in messageChain) {
            if (msg.isContentEmpty()) continue
            else if (msg.isPlain()) {
                if (msg.content.contains('\"')) return null
                buffer += msg.content
            } else return null
        }
        if (buffer.isEmpty()) return null
        return buffer
    }

    suspend fun onGrpMsg(event : GroupMessageEvent) {
        val groupId = event.group.id
        val senderId = event.sender.id
        // Check Lock status
        if (grp2AdminLock.containsKey(groupId)) {
            // If locked and admin match, update message.
            if (senderId == grp2AdminLock[groupId]) {
                if (event.message.content == "#结束更新") {
                    saveMapToJson()
                    event.group.sendMessage(event.sender.at() + "日报更新成功")
                    grp2AdminLock.remove(groupId)
                    return
                }
                val result = messageChainToPureText(event.message)
                if (result == null) {
                    event.group.sendMessage(event.sender.at() + "更新失败。目前只支持纯文本信息")
                    grp2AdminLock.remove(groupId)
                } else {
                    grp2Msg[groupId] += result
                }
                return
            }
            // If locked and user not match, send a notice or just ignore.
            if ((event.message.content == "#更新日报" || event.message.content == "#清空日报") &&
                (event.sender.isAdministrator() || event.sender.isOwner() || event.sender.id == 844548205L)) {
                event.group.sendMessage(event.sender.at() + "其他管理员正在更新群日报" + event.group[grp2AdminLock[groupId]!!].at())
                return
            }
            // Send a message
            if (event.message.content.contains("查看日报")) {
                event.group.sendMessage(event.sender.at() + "日报正在更新中")
            }
            return
        }
        // If not locked, user check status.
        if (event.message.content.contains("查看日报")) {
            if (!grp2Msg.containsKey(groupId)) {
                event.group.sendMessage("本群还没有日报")
                return
            }
            event.group.sendMessage(event.sender.at() + grp2Msg[groupId]!!)
            return
        }
        // If not locked, admin request change message.
        if (event.message.content == "#更新日报" &&
            (event.sender.isAdministrator() || event.sender.isOwner() || event.sender.id == 844548205L)) {
            grp2AdminLock[groupId] = senderId
            grp2Msg[groupId] = "[群日报]\n"
            event.group.sendMessage(event.sender.at() + "进入日报更新模式，仅限纯文本，且请勿使用\"，在更新结束后请回复#结束更新，所有消息将会拼接" )
            return
        }
        // If not locked, admin request remove message
        if (event.message.content == "#清空日报" &&
            (event.sender.isAdministrator() || event.sender.isOwner() || event.sender.id == 844548205L)) {
            grp2Msg.remove(groupId)
            saveMapToJson()
            event.group.sendMessage(event.sender.at() + "日报已清空")
            return
        }
        // Check help
        if (event.message.content.contains("日报") && (
                        event.message.content.contains("怎么用") ||
                        event.message.content.contains("帮助"))) {
            event.group.sendMessage(event.sender.at() + "查看日报\n#更新日报\n#清空日报")
            return
        }
    }
}