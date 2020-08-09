package com.mrwhoami.qqservices

import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.uploadImage
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

class Welcome (groupList : ContactList<Group>){
    class WelcomeMessage (
            private var txtMessage : String,
            private var picMessage : String?
    ) {
        suspend fun createMessageChain(botAsMember : Member) : MessageChain {
            var msg : MessageChain = PlainText(txtMessage).asMessageChain()
            if (picMessage != null) {
                msg += botAsMember.uploadImage(File(picMessage!!))
            }
            return msg
        }
    }


    private val defaultMsg = WelcomeMessage("欢迎新群友！请认真阅读群公告~", null)
    private var groupWelcomeMessages : HashMap<Long, WelcomeMessage> = hashMapOf()
    private val resPath : Path = FileSystems.getDefault().getPath("res", "Welcome")

    init {
        val groupIdList = groupList.map { it.id }
        if (Files.exists(resPath)) {
            for (groupId in groupIdList) {
                var txtMsg = ""
                var picMsg: String?
                // Check for txt file.
                val txtPath = resPath.resolve("$groupId.txt")
                if (Files.exists(txtPath)) {
                    txtMsg = txtPath.toFile().readText()
                }
                // Check for images
                val gifPath = resPath.resolve("$groupId.gif")
                val jpgPath = resPath.resolve("$groupId.jpg")
                val jpegPath = resPath.resolve("$groupId.jpeg")
                val pngPath = resPath.resolve("$groupId.png")
                picMsg = when {
                    Files.exists(gifPath) -> gifPath.toString()
                    Files.exists(jpgPath) -> jpgPath.toString()
                    Files.exists(jpegPath) -> jpegPath.toString()
                    Files.exists(pngPath) -> pngPath.toString()
                    else -> null
                }
                if (txtMsg.isNotEmpty() || picMsg != null) {
                    groupWelcomeMessages[groupId] = WelcomeMessage(txtMsg, picMsg)
                }
            }
        }
    }

    suspend fun onMemberJoin(event : MemberJoinEvent) {
        val groupId : Long = event.group.id
        if (groupWelcomeMessages.containsKey(groupId)) {
            event.group.sendMessage(event.member.at() + groupWelcomeMessages[groupId]!!.createMessageChain(event.group.botAsMember))
        } else {
            event.group.sendMessage(event.member.at() + defaultMsg.createMessageChain(event.group.botAsMember))
        }
    }

    suspend fun onGrpMsg(event: GroupMessageEvent) {
        if (!event.message.contentEquals("进群欢迎测试")) return
        val groupId : Long = event.group.id
        if (groupWelcomeMessages.containsKey(groupId)) {
            event.group.sendMessage(event.sender.at() + groupWelcomeMessages[groupId]!!.createMessageChain(event.group.botAsMember))
        } else {
            event.group.sendMessage(event.sender.at() + defaultMsg.createMessageChain(event.group.botAsMember))
        }
    }
}