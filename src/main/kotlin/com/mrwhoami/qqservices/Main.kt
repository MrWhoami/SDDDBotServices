package com.mrwhoami.qqservices

import mu.KotlinLogging
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.join
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.content

private val logger = KotlinLogging.logger {}

suspend fun main() {
    // Login QQ. Use another data class to avoid password tracking.
    val login = BotLoginInfo()
    val miraiBot = Bot(login.qqId, login.qqPassword) {
        fileBasedDeviceInfo("device.json")
    }.alsoLogin()
    logger.info { "${login.qqId} is logged in." }

    // Initialize services
    val repeater = Repeater()

    miraiBot.subscribeAlways<GroupMessageEvent> { event ->
        logger.info { "Grp [${event.group.id}][${event.sender.id}]：${event.message.content}" }
        // repeater behaviour
        val reply_msg = repeater.recvGrpMsg(event.group.id, event.sender.id, event.message.content)
        if (reply_msg != null) {
            reply(reply_msg)
        }
    }
    miraiBot.join() // 等待 Bot 离线, 避免主线程退出
}