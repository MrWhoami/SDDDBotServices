package com.mrwhoami.qqservices

import com.beust.klaxon.Klaxon
import kotlinx.coroutines.sync.Mutex
import mu.KotlinLogging
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.content
import java.io.File

class GroupDaily {
    // Logger
    private val logger = KotlinLogging.logger {}

    // JSON related constants
    private val baseDirName = "res/GroupDaily/"
    private val filename = "group_message.json"

    // Message buffer
    private var grp2Msg: MutableMap<Long, String> = mutableMapOf()

    // Updating related logic
    private var grp2Buffer: MutableMap<Long, String> = mutableMapOf()
    private var grp2Admin: HashMap<Long, Long> = hashMapOf()
    private var hourCounter = 0
    private var lock = Mutex()

    private fun grpIsUpdating(grpId: Long): Boolean {
        return grp2Admin.containsKey(grpId)
    }

    private fun grpIsUpdatingBy(grpId: Long, usrId: Long): Boolean {
        return grp2Admin.containsKey(grpId) && grp2Admin[grpId] == usrId
    }

    private suspend fun grpSetUpdatingBy(grpId: Long, usrId: Long): Boolean {
        lock.lock()
        if (grp2Admin.containsKey(grpId)) {
            val status = grp2Admin[grpId] == usrId
            lock.unlock()
            return status
        }
        grp2Admin[grpId] = usrId
        lock.unlock()
        return true
    }

    private suspend fun grpUpdate(grpId: Long, usrId: Long, message: String): Boolean {
        lock.lock()
        if (!grp2Admin.containsKey(grpId) || grp2Admin[grpId] != usrId) {
            lock.unlock()
            return false
        }
        grp2Msg[grpId] = message
        grp2Admin.remove(grpId)
        saveMapToJson()
        lock.unlock()
        return true
    }

    private suspend fun grpClear(grpId: Long) : Boolean {
        lock.lock()
        if (grp2Admin.containsKey(grpId) || !grp2Msg.contains(grpId)) {
            lock.unlock()
            return false
        }
        grp2Msg.remove(grpId)
        saveMapToJson()
        lock.unlock()
        return true
    }

    // Functions and class that helps save and load json file.
    data class GrpMsgJsonObject(val group: Long, val message: String)

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
        logger.info { "JSON file udpated on change." }
    }

    private fun readMapFromJson() {
        // Create the config file folder any way.
        val baseDir = File(baseDirName)
        if (!baseDir.exists()) baseDir.mkdirs()
        val jsonFile = File(baseDir, filename)
        if (jsonFile.exists()) {
            val jsonContent = jsonFile.readText()
            logger.info { "Parsing json: $jsonContent" }
            val jsonObjList = Klaxon().parseArray<GrpMsgJsonObject>(jsonContent)
            if (jsonObjList != null) {
                for (it in jsonObjList) grp2Msg[it.group] = it.message
            } else {
                logger.warn { "Failed to read $baseDirName$filename." }
            }
        } else {
            logger.info { "$baseDirName$filename doesn't exist." }
        }
    }

    // Constructor
    init {
        readMapFromJson()
    }

    // Reaction to group message
    private fun messageChainToPureText(messageChain: MessageChain): String? {
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

    suspend fun onGrpMsg(event: GroupMessageEvent) {
        // If the sender is modifying the daily.
        if (grpIsUpdatingBy(event.group.id, event.sender.id)) {
            val parsedMessage = messageChainToPureText(event.message)
            if (parsedMessage == null) {
                event.group.sendMessage(event.sender.at() + "更新失败。目前只支持纯文本信息，且不能有英文双引号，请重新发送")
                return
            }
            val result = grpUpdate(event.group.id, event.sender.id, parsedMessage)
            if (!result) {
                event.group.sendMessage(event.sender.at() + "更新失败。不应该啊，让我们问一下" + event.group[844548205L].at())
                logger.error {
                    "Failed to update. Status: [${event.group.id}][${event.sender.id}] Group is updating: ${grpIsUpdatingBy(
                        event.group.id,
                        event.sender.id
                    )}"
                }
                return
            }
            event.group.sendMessage(event.sender.at() + ("更新成功。当前消息为:\n" + grp2Msg[event.group.id]))
            return
        }
        // If the sender is trying to get the daily, just return it if available.
        if (event.message.content.contains("查看日报")) {
            if (grp2Msg.containsKey(event.group.id)) {
                event.group.sendMessage(event.sender.at() + grp2Msg[event.group.id]!!)
            } else {
                event.group.sendMessage(event.sender.at() + "\"本群还没有日报\"")
            }
            return
        }
        // If the sender is try to get help,
        if (event.message.content.contains("日报") && (
                    event.message.content.contains("怎么用") || event.message.content.contains("帮助"))
        ) {
            event.group.sendMessage(event.sender.at() + "查看日报\n#更新日报\n#清空日报")
            return
        }
        // If the sender is trying to Modify the daily.
        if (event.message.content == "#更新日报") {
            if (!BotHelper.memberIsAdmin(event.sender)) return
            if (grpIsUpdating(event.group.id)) {
                event.group.sendMessage(event.sender.at() + "其他管理员正在更新群日报")
                return
            }
            val result = grpSetUpdatingBy(event.group.id, event.sender.id)
            if (result) {
                event.group.sendMessage(event.sender.at() + "进入日报更新模式，您的下一条消息将作为更新内容，仅限纯文本，且请勿使用\"")
            } else {
                event.group.sendMessage(event.sender.at() + "进入更新模式失败，可能原因：1.机器人正忙；2.你手慢了")
            }
            return
        }
        // If the sender is trying to Clear the daily.
        if (event.message.content == "#清空日报") {
            if (!BotHelper.memberIsAdmin(event.sender)) return
            val result = grpClear(event.group.id)
            if (result) {
                event.group.sendMessage(event.sender.at() + "本群日报已清空")
            } else {
                event.group.sendMessage(event.sender.at() + "群日报清空失败：1.机器人正忙；2.其他管理员正在更新日报；3.本群根本就没有日报")
            }
            return
        }
        // Special command
        if (event.sender.id == 844548205L) {
            if (event.message.content == "#复位状态") {
                lock.tryLock()
                grp2Admin.clear()
                lock.unlock()
                event.group.sendMessage("复位完毕")
            }
        }
        // No command
        return
    }

    // Accumulate every 8 hour.
    suspend fun onHourWake(bot : Bot) {
        if (hourCounter == 8) hourCounter = 0
        if (hourCounter == 0) {
            for (group in bot.groups) {
                if (grp2Msg.contains(group.id)) {
                    group.sendMessage(grp2Msg[group.id]!!)
                }
            }
        }
        hourCounter += 1
    }
}