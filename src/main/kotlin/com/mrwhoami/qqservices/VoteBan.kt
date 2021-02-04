package com.mrwhoami.qqservices

import net.mamoe.mirai.contact.AnonymousMember
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isMuted
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.at
import net.mamoe.mirai.message.data.content
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

class VoteBan {
    data class VoteBuffer (
            var voter_count : Int = 0,
            var lastTime : Instant = Instant.now()
    )

    init {
        BotHelper.registerFunctions("投票禁言", listOf("口他 @xxx"))
    }
    private val disabledInGroup = listOf(1094098748L)
    private var grp2Buffer = hashMapOf<Pair<Long, Long>, VoteBuffer>()
    private var usrId2LastVoteTime = hashMapOf<Long, Instant>()

    suspend fun onGrpMsg(event: GroupMessageEvent) {
        if (disabledInGroup.contains(event.group.id)) return

        // Check if this is a ban vote message.
        val groupId = event.group.id
        val voter = event.sender
        val voterId = voter.id

        val msg = event.message
        if (voter is AnonymousMember) {
            event.group.sendMessage("藏头藏尾算什么好汉!")
            return
        }
        if (!(msg.content.contains("口他") ||
                    msg.content.contains("口maki") ||
                    msg.content.contains("口水母") ||
                    msg.content.contains("口时雨") ||
                    msg.content.contains("口時雨") ||
                    msg.content.contains("口熊貓") ||
                    msg.content.contains("口熊猫"))) {
            return
        }
        if (!event.group.botPermission.isAdministrator()) {
            event.group.sendMessage("没权限我也没办法~")
            return
        }
        // Check voter time
        val now = Instant.now()
        if (!BotHelper.memberIsAdmin(voter) && usrId2LastVoteTime.containsKey(voterId) &&
                Duration.between(usrId2LastVoteTime[voterId], now).toMinutes() < 15) {
            event.group.sendMessage(voter.at() + "你在15分钟内投过票了。")
            return
        } else {
            usrId2LastVoteTime[voterId] = now
        }
        // Get the target.
        val targetId = when {
            event.message.filterIsInstance<At>().firstOrNull() != null -> event.message.filterIsInstance<At>().firstOrNull()!!.target
            msg.content.contains("口水母") -> 1260775699L
            msg.content.contains("口时雨") || msg.content.contains("口時雨") -> 1094829199L
            msg.content.contains("口熊猫") || msg.content.contains("口熊貓") -> 441702144L
            msg.content.contains("口maki") -> 374721531L
            else -> {
                event.group.sendMessage("请指定一个投票目标")
                return
            }
        }

        // Check if the target is actually in the group and mutable
        if (event.bot.id == targetId) {
            if (BotHelper.memberIsAdmin(event.sender)) {
                event.group.sendMessage("求求你不要这样")
                return
            } else {
                val timeLength = Random.nextInt(10, 41)
                event.group.sendMessage(voter.at() + "谁给你的勇气口我！休息 $timeLength 分钟吧！")
                voter.mute(timeLength * 60)
                return
            }
        } else if (!event.group.members.contains(targetId)) {
            event.group.sendMessage("$targetId 并不在群内")
            return
        }
        var target = event.group[targetId]!!
        if (BotHelper.memberIsAdmin(target) && BotHelper.memberIsAdmin(voter)) {
            event.group.sendMessage("你们不要再打啦~")
            return
        }
        if (BotHelper.memberIsAdmin(target)) {
            event.group.sendMessage("啊这……目标出了反甲……")
            target = voter as NormalMember
        }
        val targetPair = Pair(groupId, target.id)
        // Check if target already exists
        if (!grp2Buffer.containsKey(targetPair)) {
            grp2Buffer[targetPair] = VoteBuffer(1, now)
            event.group.sendMessage(target.at() + "你已经被投 1 票，15分钟集齐3票即可获得10~20分钟随机口球一份！")
            return
        } else {
            val buffer = grp2Buffer[targetPair]!!
            // Check the buffer timeout. If so, clear it first.
            val duration = Duration.between(buffer.lastTime, now)
            if (duration.toMinutes() > 15L) {
                buffer.voter_count = 0
                buffer.lastTime = now
            }
            // If vote count meet criteria, ban the user, output a message and clear the buffer.
            buffer.voter_count += 1
            buffer.lastTime = now
            if (buffer.voter_count >= 3) {
                // Ban is confirmed. clear the buffer.
                buffer.voter_count = 0
                // Get the random time in seconds
                val timeLength = Random.nextInt(10, 21) * 60
                // Check for time accumulation
                if (target.isMuted || target.muteTimeRemaining > 0) {
                    var timeRemaining = target.muteTimeRemaining + timeLength
                    if (timeRemaining > 2 * 60 * 60) {
                        timeRemaining = 2 * 60 * 60
                        event.group.sendMessage(target.at() + "啊~好惨啊~算了就休息两小时吧！")
                    } else {
                        event.group.sendMessage(target.at() + "再多休息 ${timeLength / 60} 分钟~")
                    }
                    target.mute(timeRemaining)
                } else {
                    event.group.sendMessage(target.at() + "大成功~休息 ${timeLength / 60} 分钟吧！")
                    target.mute(timeLength)
                }
                return
            } else {
                event.group.sendMessage(target.at() + "你已经被投 ${buffer.voter_count} 票，15分钟集齐3票即可获得10~20分钟随机口球一份！")
                return
            }
        }
    }
}
