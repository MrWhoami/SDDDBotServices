package com.mrwhoami.qqservices

import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOwner
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.sendImage
import kotlin.random.Random

class QuestionAnswer {
    private fun containsBotName(msgContent : String) : Boolean{
        return msgContent.contains("bot") || msgContent.contains("波特") || msgContent.contains("机器人")
    }

    suspend fun onGrpMsg(event: GroupMessageEvent) {
        val msg = event.message
        val msgContent = msg.content
        val grp = event.group

        when {
            msgContent.contains("zaima") -> grp.sendMessage("buzai, cmn")
            msgContent.contains("老婆") -> {
                val picture = this::class.java.getResource("/QuestionAnswer/sjb_fsnrjsnlpm.jpg")
                grp.sendImage(picture)
            }
            msgContent.contains("给我精致睡眠") || msgContent.contains("给我精致水母") -> {
                event.sender.mute(7 * 60 * 60)
                grp.sendMessage("大臭猪晚安(❁´◡`❁)")
            }
            containsBotName(msgContent) && msgContent.contains("爬") -> {
                if (event.sender.isAdministrator() || event.sender.isOwner()) {
                    grp.sendMessage("呜呜呜，不要欺负我( TдT)")
                } else {
                    if (grp.botPermission.isAdministrator()) {
                        event.sender.mute(Random.nextInt(1, 120) * 60)
                    }
                    grp.sendMessage("大臭猪你爬( `д´)")
                }
            }
            containsBotName(msgContent) && (msgContent.contains("亲亲") || msgContent.contains("啾啾")) -> {
                grp.sendMessage("大臭猪不要啾啾我⊂彡☆))∀`)")
            }
            containsBotName(msgContent) && msgContent.contains("日我") -> {
                grp.sendMessage("你不对劲，你有问题，你快点爬")
            }
            containsBotName(msgContent) && msgContent.contains("活着") -> {
                val answers = listOf(
                        "应该算活着吧",
                        "还能死了不成",
                        "大概？"
                )
                grp.sendMessage(answers[Random.nextInt(answers.size)])
            }
            msgContent.contains("功能列表") -> {
                grp.sendMessage("投票禁言 - 口水母\n禁言套餐 - 我要休息/口我x分钟/小时\n精致睡眠 - 给我精致睡眠/水母\n复读机\nQ&A")
            }
            containsBotName(msgContent) -> {
                val answers = listOf(
                        "没事就别找我了",
                        "似乎听到了DD的声音",
                        "希望不是在说我"
                )
                grp.sendMessage(answers[Random.nextInt(answers.size)])
            }
        }
    }
}
