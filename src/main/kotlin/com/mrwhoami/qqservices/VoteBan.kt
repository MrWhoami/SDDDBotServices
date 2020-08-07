package com.mrwhoami.qqservices

import mu.KotlinLogging
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.at
import net.mamoe.mirai.message.data.content
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class VoteBan {
    data class VoteBuffer (
            var voters : HashSet<Long> = hashSetOf<Long>(),
            var lastTime : Instant = Instant.now()
    )

    private var grp2Buffer = hashMapOf<Pair<Long, Long>, VoteBuffer>()

    suspend fun recvGrpMsg(event: GroupMessageEvent) {
        // Check if this is a ban vote message.
        val groupId = event.group.id
        val voter = event.sender.id

        val msg = event.message
        if (!msg.content.contains("口水母")) {
            return
        }
        // Get the target.
        val target : Pair<Long, Long>
        target = if (msg.content.contains("口水母")) {
            Pair(groupId, 1260775699L)
        } else {
            Pair(groupId, 1260775699L)
        }
        // Check if target already exists
        if (!grp2Buffer.containsKey(target)) {
            grp2Buffer[target] = VoteBuffer(hashSetOf(voter), Instant.now())
            event.group.sendMessage(event.group.get(target.second).at() + "你已经被投 1 票，15分钟集齐3票即可获得1~40分钟随机口球一份！")
            return
        } else {
            val buffer = grp2Buffer[target]!!
            // Check the buffer timeout. If so, clear it first.
            val now = Instant.now()
            val duration = Duration.between(buffer.lastTime, now)
            if (duration.toMinutes() > 15L) {
                buffer.voters.clear()
                buffer.lastTime = now
            }
            // Check the voter. This is actually conflict with previous condition.
            if (buffer.voters.contains(voter)) {
                event.group.sendMessage(event.sender.at() + "你在15分钟内投过票了。")
                return
            }
            // If vote count meet criteria, ban the user, output a message and clear the buffer.
            buffer.voters.add(voter)
            buffer.lastTime = now
            if (buffer.voters.size >= 3) {
                buffer.voters.clear()
                val timeLength = Random.nextInt(1, 40)
                event.group.sendMessage(event.group.get(target.second).at() + "大成功~休息 $timeLength 分钟吧！")
                event.group.get(target.second).mute(timeLength * 60)
                return
            } else {
                event.group.sendMessage(event.group.get(target.second).at() + "你已经被投 ${buffer.voters.size} 票，15分钟集齐3票即可获得1~40分钟随机口球一份！")
                return
            }
        }
    }
}