package com.mrwhoami.qqservices

import mu.KotlinLogging
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOwner
import java.io.File

class BotHelper {
    companion object {
        private val logger = KotlinLogging.logger {}
        // Set up a bot owner using res/owner.txt
        private var botOwner : Long? = null

        fun loadConfig() {
            val configFile = File("res/owner.txt")
            if (!configFile.exists()) return
            botOwner = configFile.readText().trim().toLong()
            logger.info { "Bot admin set to $botOwner" }
        }

        fun memberIsAdmin(member : Member) : Boolean {
            return member.isAdministrator() || member.isOwner() || member.id == botOwner
        }

        fun memberIsBotOwner(member : Member) : Boolean {
            return member.id == botOwner
        }
    }
}