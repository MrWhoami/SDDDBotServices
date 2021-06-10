package com.mrwhoami.qqservices

import kotlinx.coroutines.*
import mu.KotlinLogging
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MemberJoinEvent

import net.mamoe.mirai.utils.BotConfiguration
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

suspend fun main() {
    // Login QQ. Use another data class to avoid password tracking.
    val login = BotLoginInfo()
    // Check it before using
    if ((login.qqId == 0L) or (login.qqPassword.isEmpty())) {
        logger.error { "You should provide your QQ ID and password first." }
        exitProcess(1)
    }
    val miraiBot = BotFactory.newBot(login.qqId, login.qqPassword) {
        fileBasedDeviceInfo("device.json")
        protocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE
    }
    miraiBot.login()

    logger.info { "${login.qqId} is logged in." }

    // Initialize helper
    BotHelper.loadConfig()
    // Initialize services
    val repeater = Repeater()
    val voteBan = VoteBan()
    val muteMenu = MuteMenu()
    val qAndA = QuestionAnswer()
    val welcome = Welcome(miraiBot.groups)
    val groupDaily = GroupDaily()
    val rng = RandomNumberGenerator()

    logger.info { "Initialization finished." }

    miraiBot.eventChannel.subscribeAlways<GroupMessageEvent> {
        // repeater behaviour
        rng.onGrpMsg(it)
        repeater.onGrpMsg(it)
        voteBan.onGrpMsg(it)
        muteMenu.onGrpMsg(it)
        qAndA.onGrpMsg(it)
        welcome.onGrpMsg(it)
        groupDaily.onGrpMsg(it)
    }

    miraiBot.eventChannel.subscribeAlways<MemberJoinEvent> {
        welcome.onMemberJoin(it)
    }

    // Check per minute
    GlobalScope.launch {
        while (miraiBot.isActive) {
            logger.info { "1-min heart beat event." }
            delay(60 * 1000L)
        }
    }

    // Check per hour
    GlobalScope.launch {
        while (miraiBot.isActive) {
            logger.info { "1-hour heart beat event." }
            groupDaily.onHourWake(miraiBot)
            delay(60 * 60 * 1000L)
        }
    }

    miraiBot.join() // 等待 Bot 离线, 避免主线程退出
}