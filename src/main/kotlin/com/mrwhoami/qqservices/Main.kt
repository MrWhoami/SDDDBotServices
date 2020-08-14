package com.mrwhoami.qqservices

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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

    // Initialize helper
    BotHelper.loadConfig()
    // Initialize services
    val repeater = Repeater()
    val voteBan = VoteBan()
    val muteMenu = MuteMenu()
    val qAndA = QuestionAnswer()
    val welcome = Welcome(miraiBot.groups)
    val groupDaily = GroupDaily()

    logger.info { "Initialization finished." }

    miraiBot.subscribeAlways<GroupMessageEvent> {
        // repeater behaviour
        repeater.onGrpMsg(it)
        voteBan.onGrpMsg(it)
        muteMenu.onGrpMsg(it)
        qAndA.onGrpMsg(it)
        welcome.onGrpMsg(it)
        groupDaily.onGrpMsg(it)
    }

    miraiBot.subscribeAlways<MemberJoinEvent> {
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