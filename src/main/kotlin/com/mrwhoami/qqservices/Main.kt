package com.mrwhoami.qqservices

import mu.KotlinLogging
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.join
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.GroupMessageEvent

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
    val muteMenu = MuteMenu()
    val qAndA = QuestionAnswer()
    val welcome = Welcome(miraiBot.groups)

    miraiBot.subscribeAlways<GroupMessageEvent> {
        // repeater behaviour
        repeater.onGrpMsg(it)
        voteBan.onGrpMsg(it)
        muteMenu.onGrpMsg(it)
        qAndA.onGrpMsg(it)
        welcome.onGrpMsg(it)

    }

    miraiBot.subscribeAlways<MemberJoinEvent> {
        welcome.onMemberJoin(it)
    }

    miraiBot.join() // 等待 Bot 离线, 避免主线程退出
}