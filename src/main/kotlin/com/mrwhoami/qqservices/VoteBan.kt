package com.mrwhoami.qqservices

import mu.KotlinLogging
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.content
import java.time.Instant

private val logger = KotlinLogging.logger {}

class VoteBan {
    data class VoteBuffer (
            var voters : HashSet<Long> = hashSetOf<Long>(),
            var lastTime : Instant? = null
    )

    private var grp2Buffer = hashMapOf<Pair<Long, Long>, VoteBuffer>()

    suspend fun recvGrpMsg(event: GroupMessageEvent) {
        // Check if this is a ban vote message.
        val msg = event.message
        if (!msg.content.contains("口他")) {
            return
        }
        logger.info { msg.contentToString() }
        // Get the target.
        // Check if target already exists
        // Check the buffer timeout. If so, clear it first.
        // Check the voter
        // If vote count meet criteria, ban the user, output a message and clear the buffer.
    }
}