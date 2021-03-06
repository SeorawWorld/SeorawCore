package world.seoraw.core

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.time.LocalDateTime

class SeorawCore : JavaPlugin() {

    lateinit var conf: FileConfiguration
        private set

    init {
        instance = this
    }

    override fun onLoad() {
        initConf()
    }

    override fun onEnable() {
        initListeners()
        logger.info("SeorawCore 已就绪")
        server.scheduler.runTaskAsynchronously(this, Runnable {
            logger.info("GitHub WebHook listen on *:${conf.getInt("listen")}")
            embeddedServer(Netty, port = conf.getInt("listen"), host = "0.0.0.0") { configureRouting() }.start(wait = true)
        })
        server.scheduler.runTaskTimerAsynchronously(this, Runnable {
            val now = LocalDateTime.now()
            if (now.hour == 4 && now.minute == 0 && now.second in 0..5) {
                SafelyShutdown.shutdown(60)
            }
        }, 20, 20)
    }

    fun initConf() {
        val file = File(dataFolder, "conf.yml")
        if (file.exists()) {
            conf = YamlConfiguration()
            conf.loadFromString(file.readText())
        } else {
            saveResource("conf.yml", true)
            initConf()
        }
    }

    fun initListeners() {
        server.pluginManager.registerEvents(SeorawGame, this)
        server.pluginManager.registerEvents(SafelyShutdown, this)
    }

    companion object {

        lateinit var instance: SeorawCore
            private set
    }
}