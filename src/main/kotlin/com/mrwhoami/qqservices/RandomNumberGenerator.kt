package com.mrwhoami.qqservices

import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.at
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.message.data.isPlain
import kotlin.random.Random


class RandomNumberGenerator {
    init {
        BotHelper.registerFunctions(
            "随机数生成器",
            listOf("#RNG <start> <end>", "#RNG <end>", "#RNG")
        )
    }

    suspend fun onGrpMsg(event: GroupMessageEvent) {
        // Check if this is text message
        val msg = event.message
        if (!msg.all { block -> block.isContentEmpty() || block.isPlain() }) {
            return
        }
        // Check if this is an order
        val msgContent = msg.content
        if (!msgContent.startsWith("#RNG")) {
            return
        }
        val customer = event.sender
        // Parse args and check.
        val args = msgContent.split(" ")
        val num_args = args.filter { arg -> arg.toIntOrNull() != null }.map { arg -> arg.toInt() }
        // Check args number and give default value
        val start = if (num_args.size > 1) num_args[0] else 0
        val end = when(num_args.size) {
            0 -> 100
            1 -> num_args[0]
            else -> num_args[1]
        }
        if (end <= start) {
            event.group.sendMessage(customer.at() + "End must larger than start.")
        } else {
            val result = Random.nextInt(start, end)
            event.group.sendMessage(customer.at() + "RNG [$start,$end) -> $result")
        }
    }
}