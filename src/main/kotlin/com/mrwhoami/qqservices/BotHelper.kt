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
        private var moduleFunctionList : MutableMap<String, List<String>> = mutableMapOf()
        private var groupDisabledModules : MutableMap<Long, List<String>> = mutableMapOf()

        fun loadConfig() {
            val ownerConfigFile = File("res/owner.txt")
            if (!ownerConfigFile.exists()) return
            botOwner = ownerConfigFile.readText().trim().toLong()
            logger.info { "Bot admin set to $botOwner" }
        }

        fun memberIsAdmin(member : Member) : Boolean {
            return member.isAdministrator() || member.isOwner() || member.id == botOwner
        }

        fun memberIsBotOwner(member : Member) : Boolean {
            return member.id == botOwner
        }

        fun registerFunctions (moduleName : String, functionNames : List<String>) {
            moduleFunctionList[moduleName] = functionNames
        }

        fun functionsToString (grpId : Long) : String {
            var functionsString = "<当前功能列表>\n"
            val disabledModules : List<String> = if (groupDisabledModules.containsKey(grpId)) {
                groupDisabledModules[grpId]!!
            } else {
                emptyList()
            }
            for ((moduleName, functionList) in moduleFunctionList) {
                functionsString += "[模块：$moduleName]"
                functionsString += if (disabledModules.contains(moduleName)) "(模块已禁用)\n"  else "\n"
                for (function in functionList) {
                    functionsString += "$function\n"
                }
            }
            return functionsString
        }
    }
}