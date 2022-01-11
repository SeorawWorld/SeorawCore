package world.seoraw.core

import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable

object SafelyShutdown : Listener {

    val bar by lazy { Bukkit.createBossBar("", BarColor.YELLOW, BarStyle.SOLID) }
    var delay = 0
    var isStarting = false

    fun shutdown(value: Int) {
        if (isStarting) {
            return
        }
        isStarting = true
        delay = value
        object : BukkitRunnable() {

            override fun run() {
                if (delay-- > 0) {
                    bar.progress = 1 - (delay / value.toDouble())
                    bar.setTitle("§e距离服务器重新启动还有 $delay 秒 ...")
                    SeorawCore.instance.logger.info("$delay")
                    Bukkit.getOnlinePlayers().filter { it !in bar.players }.forEach { bar.addPlayer(it) }
                } else {
                    Bukkit.shutdown()
                    cancel()
                }
            }
        }.runTaskTimer(SeorawCore.instance, 0, 20)
    }
}