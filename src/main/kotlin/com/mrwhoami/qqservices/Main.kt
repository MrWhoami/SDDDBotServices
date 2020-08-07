package com.mrwhoami.qqservices

import mu.KotlinLogging
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.join
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.at

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
    val voteBan = VoteBan()

    miraiBot.subscribeAlways<GroupMessageEvent> {
        // repeater behaviour
        repeater.recvGrpMsg(it)
        voteBan.recvGrpMsg(it)
    }

    miraiBot.subscribeAlways<MemberJoinEvent> {
        it.group.sendMessage(it.member.at() + "欢迎新群友！请认真阅读群公告~")
    }

    miraiBot.join() // 等待 Bot 离线, 避免主线程退出
}