package com.mrwhoami.qqservices

import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.at
import net.mamoe.mirai.message.data.content
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

class VoteBan {
    data class VoteBuffer (
            var voters : HashSet<Long> = hashSetOf(),
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
        if (!event.group.botPermission.isAdministrator()) return
        // Check if this is a ban vote message.
        val groupId = event.group.id
        val voter = event.sender
        val voterId = voter.id

        val msg = event.message
        if (!(msg.content.contains("口水母") || msg.content.contains("口他"))) {
            return
        }
        // Check voter time
        val now = Instant.now()
        if (usrId2LastVoteTime.containsKey(voterId) && Duration.between(usrId2LastVoteTime[voterId], now).toMinutes() < 15) {
            event.group.sendMessage(voter.at() + "你在15分钟内投过票了。")
            return
        } else {
            usrId2LastVoteTime[voterId] = now
        }
        // Get the target.
        val targetId = when {
            msg.content.contains("口水母") -> 1260775699L
            event.message[At] != null -> event.message[At]!!.target
            else -> {
                event.group.sendMessage("请指定一个投票目标")
                return
            }
        }
        val targetPair = Pair(groupId, targetId)
        // Check if the target is actually in the group and mutable
        if (!event.group.members.contains(targetId)) {
            event.group.sendMessage("$targetId 并不在群内")
            return
        }
        val target = event.group[targetId]
        if (BotHelper.memberIsAdmin(target)) {
            event.group.sendMessage("啊这")
            return
        }
        // Check if target already exists
        if (!grp2Buffer.containsKey(targetPair)) {
            grp2Buffer[targetPair] = VoteBuffer(hashSetOf(voterId), now)
            event.group.sendMessage(target.at() + "你已经被投 1 票，15分钟集齐3票即可获得10~40分钟随机口球一份！")
            return
        } else {
            val buffer = grp2Buffer[targetPair]!!
            // Check the buffer timeout. If so, clear it first.
            val duration = Duration.between(buffer.lastTime, now)
            if (duration.toMinutes() > 15L) {
                buffer.voters.clear()
                buffer.lastTime = now
            }
            // If vote count meet criteria, ban the user, output a message and clear the buffer.
            buffer.voters.add(voterId)
            buffer.lastTime = now
            if (buffer.voters.size >= 3) {
                buffer.voters.clear()
                val timeLength = Random.nextInt(10, 41)
                event.group.sendMessage(target.at() + "大成功~休息 $timeLength 分钟吧！")
                target.mute(timeLength * 60)
                return
            } else {
                event.group.sendMessage(target.at() + "你已经被投 ${buffer.voters.size} 票，15分钟集齐3票即可获得10~40分钟随机口球一份！")
                return
            }
        }
    }
}
