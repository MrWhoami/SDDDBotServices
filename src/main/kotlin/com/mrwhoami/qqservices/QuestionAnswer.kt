package com.mrwhoami.qqservices

import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.sendImage
import kotlin.random.Random

class QuestionAnswer {
    private val disabledInGroup = listOf(1094098748L)

    private fun containsBotName(msgContent : String) : Boolean{
        return msgContent.contains("bot") || msgContent.contains("Bot") ||
                msgContent.contains("BOT") || msgContent.contains("波特") ||
                msgContent.contains("机器人") || msgContent.contains("機器人")
    }

    private fun getPlainText(messageChain : MessageChain) : String? {
        var buffer = ""
        for (msg in messageChain) {
            if (msg.isContentEmpty()) continue
            else if (msg.isPlain()) {
                buffer += msg.content
            } else continue
        }
        if (buffer.isEmpty()) return null
        return buffer
    }

    suspend fun onGrpMsg(event: GroupMessageEvent) {
        if (disabledInGroup.contains(event.group.id)) return

        val msg = event.message
        val msgContent = getPlainText(msg) ?: return
        val grp = event.group

        when {
            msgContent.contains("zaima") -> grp.sendMessage("buzai, cmn (　^ω^)")
            msgContent.contains("老婆") -> {
                val picture = this::class.java.getResource("/QuestionAnswer/sjb_fsnrjsnlpm.jpg")
                grp.sendImage(picture.openStream())
            }
            msgContent.contains("我爱你") || msgContent.contains("我愛你")-> {
                val picture = this::class.java.getResource("/QuestionAnswer/love.jpg")
                grp.sendImage(picture.openStream())
            }
            msgContent == "给我精致睡眠" ||
            msgContent == "給我精緻睡眠" ||
            msgContent == "给我精致水母" ||
            msgContent == "給我精緻水母" ||
            msgContent == "我想梦到花花" ||
            msgContent == "我想夢到花花" ||
            msgContent == "我要梦到花花" ||
            msgContent == "我要夢到花花" -> {
                if (event.group.botPermission.isAdministrator()) {
                    event.sender.mute(7 * 60 * 60)
                }
                grp.sendMessage("大臭猪晚安(❁´◡`❁)")
            }
            msgContent == "呐" ||
            msgContent == "吶" -> {
                if (event.group.id == 1033928478L && event.group.botPermission.isAdministrator()) {
                    event.sender.mute(24 * 60 * 60)
                }
                grp.sendMessage("正義，執行！")
            }
            containsBotName(msgContent) && msgContent.contains("爬") -> {
                if (BotHelper.memberIsAdmin(event.sender)) {
                    grp.sendMessage("呜呜呜，不要欺负我( TдT)")
                } else {
                    if (grp.botPermission.isAdministrator()) {
                        event.sender.mute(Random.nextInt(1, 120) * 60)
                    }
                    grp.sendMessage("大臭猪你爬( `д´)")
                }
            }
            containsBotName(msgContent) && msgContent.contains("傻") -> {
                if (BotHelper.memberIsAdmin(event.sender)) {
                    grp.sendMessage("呜呜呜，別罵了( TдT)")
                } else {
                    grp.sendMessage("你才傻！你全家都傻！(　^ω^)")
                }
            }
            containsBotName(msgContent) && msgContent.contains("可爱")  -> {
                grp.sendMessage("欸嘿~(*ﾟ∀ﾟ*)")
            }
            containsBotName(msgContent) && (msgContent.contains("亲亲") ||
                                            msgContent.contains("啾啾") ||
                                            msgContent.contains("mua")) -> {
                grp.sendMessage("大臭猪不要啾啾我⊂彡☆))∀`)")
            }
            containsBotName(msgContent) && (msgContent.contains("日我") ||
                                            msgContent.contains("上我") ||
                                            msgContent.contains("曰我")) -> {
                grp.sendMessage("你不对劲，你有问题，你快点爬(`ヮ´ )")
            }
            containsBotName(msgContent) && (msgContent.contains("能") ||
                                            msgContent.contains("可以") ||
                    ((msgContent.contains("有没有") || msgContent.contains("吗")) && msgContent.contains("功能"))) -> {
                grp.sendMessage("你来帮我码代码就有了\nhttps://github.com/MrWhoami/SDDDBotServices")
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
                grp.sendMessage(BotHelper.functionsToString(event.group.id))
            }
            containsBotName(msgContent) -> {
                val answers = listOf(
                        "没事就别找我了",
                        "似乎听到了DD的声音",
                        "希望不是在说我"
                )
                val picAnswer = listOf(
                        "/QuestionAnswer/hina_sdys.jpg",
                        "/QuestionAnswer/hina_diantou.gif",
                        "/QuestionAnswer/hina_what.gif",
                        "/QuestionAnswer/kaf_question.jpg"
                )
                val idx = Random.nextInt(-picAnswer.size, answers.size)
                if (idx >= 0){
                    grp.sendMessage(answers[idx])
                } else {
                    val picture = this::class.java.getResource(picAnswer[-idx - 1])
                    grp.sendImage(picture.openStream())
                }
            }
        }
    }
}
