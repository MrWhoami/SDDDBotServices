package com.mrwhoami.qqservices

import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.isNotPlain

class Repeater {
    data class RepeaterBuffer (
            var lastMsg : String? = null,
            var repeated: Boolean = false,
            var usrIdSet : HashSet<Long> = hashSetOf<Long>()
    )
    private var grp2Buffer = hashMapOf<Long, RepeaterBuffer>()

    suspend fun onGrpMsg(event : GroupMessageEvent) {
        val grpId = event.group.id
        val usrId = event.sender.id
        val msg = event.message

        // Haven't receive any message from the group. Initialize it.
        if (!grp2Buffer.containsKey(grpId)) {
            grp2Buffer[grpId] = RepeaterBuffer()
        }

        val buffer = grp2Buffer[grpId]!!
        // If the message is not plain text
        if (msg.isNotPlain()) {
            buffer.lastMsg = null
            buffer.repeated = false
            buffer.usrIdSet.clear()
            return
        }
        // The message is different from the previous message.
        if (msg.content != buffer.lastMsg) {
            buffer.lastMsg = msg.content
            buffer.repeated = false
            buffer.usrIdSet.clear()
            buffer.usrIdSet.add(usrId)
            return
        }
        // The message is already repeated
        if (buffer.repeated) {
            return
        }
        // The message is sent by a user who has sent it before.
        if (buffer.usrIdSet.contains(usrId)) {
            return
        }
        // Add the user to repeater list
        buffer.usrIdSet.add(usrId)
        // If it has been repeated three times, bot repeat it.
        if (buffer.usrIdSet.size >= 3) {
            buffer.repeated = true
            event.group.sendMessage(buffer.lastMsg!!)
            return
        }
        return
    }
}