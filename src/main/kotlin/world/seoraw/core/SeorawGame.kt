package world.seoraw.core

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.server.ServerCommandEvent

object SeorawGame : Listener {

    init {
        Bukkit.getOperators().forEach { it.isOp = false }
    }

    @EventHandler
    fun e(e: ServerCommandEvent) {
        e.isCancelled = true
        e.sender.sendMessage("§c无法在控制台运行命令: ${e.command}")
    }

    @EventHandler
    fun e(e: PlayerGameModeChangeEvent) {
        if (e.newGameMode != GameMode.SURVIVAL) {
            e.isCancelled = true
            e.player.sendMessage("§c你不可以将游戏模式调整至生存模式之外。")
        }
    }
}