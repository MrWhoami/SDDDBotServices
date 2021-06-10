package com.mrwhoami.qqservices

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*

class Repeater {
    data class RepeaterBuffer (
            var lastMsg : String? = null,
            var repeated: Boolean = false,
            var usrIdSet : HashSet<Long> = hashSetOf()
    )
    private var grp2Buffer = hashMapOf<Long, RepeaterBuffer>()

    init {
        BotHelper.registerFunctions("复读机", emptyList())
    }

    private fun message2MiraiCode(message: MessageChain): String? {
        var buffer = ""
        for (msg in message) {
            if (msg.isContentEmpty()) continue
            else if (msg is CodableMessage) buffer += msg.serializeToMiraiCode()
            else return null
        }
        if (buffer.isEmpty()) {
            return null
        }
        return buffer
    }

    suspend fun onGrpMsg(event : GroupMessageEvent) {
        val grpId = event.group.id
        val usrId = event.sender.id
        val msg = event.message

        // Haven't receive any message from the group. Initialize it.
        if (!grp2Buffer.containsKey(grpId)) {
            grp2Buffer[grpId] = RepeaterBuffer()
        }

        val buffer = grp2Buffer[grpId]!!
        // Check if the message is repeatable
        val miraiMsg = message2MiraiCode(msg)
        if (miraiMsg == null) {
            buffer.lastMsg = null
            buffer.repeated = false
            buffer.usrIdSet.clear()
            return
        }
        // The message is different from the previous message.
        if (miraiMsg != buffer.lastMsg) {
            buffer.lastMsg = miraiMsg
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
            event.group.sendMessage(buffer.lastMsg!!.deserializeMiraiCode())
            return
        }
        return
    }
}