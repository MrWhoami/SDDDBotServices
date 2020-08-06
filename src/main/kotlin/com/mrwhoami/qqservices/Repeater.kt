package com.mrwhoami.qqservices

class Repeater() {
    data class RepeaterBuffer (
            var lastMsg : String? = null,
            var repeated: Boolean = false,
            var usrIdSet : HashSet<Long> = hashSetOf<Long>()
    )
    private var grp2Buffer = hashMapOf<Long, RepeaterBuffer>()

    fun recvGrpMsg(grpId: Long, usrId : Long, msg: String): String? {
        // Haven't receive any message from the group. Initialize it.
        if (!grp2Buffer.containsKey(grpId)) {
            grp2Buffer[grpId] = RepeaterBuffer()
        }

        var buffer = grp2Buffer[grpId]!!
        // The message is different from the previous message.
        if (msg != buffer.lastMsg) {
            buffer.lastMsg = msg
            buffer.repeated = false
            buffer.usrIdSet.clear()
            buffer.usrIdSet.add(usrId)
            return null
        }
        // The message is already repeated
        if (buffer.repeated) {
            return null
        }
        // The message is sent by a user who has sent it before.
        if (buffer.usrIdSet.contains(usrId)) {
            return null
        }
        // Add the user to repeater list
        buffer.usrIdSet.add(usrId)
        // If it has been repeated three times, bot repeat it.
        if (buffer.usrIdSet.size >= 3) {
            buffer.repeated = true
            return msg
        }
        return null
    }
}