package com.github.hoshikurama.tmdiscord

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command

class ReloadCommand : Command("tmdiscord") {
    override fun execute(sender: CommandSender, args: Array<String>) {
        if (args.getOrNull(0)?.equals("reload") != true) return
        if (!sender.hasPermission("tmdiscord")) return

    }
}