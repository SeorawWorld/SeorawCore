package world.seoraw.core

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

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
        logger.info("SeorawCore 已就绪")
        server.pluginManager.registerEvents(SeorawGame, this)
        server.scheduler.runTaskAsynchronously(this, Runnable {
            logger.info("GitHub WebHook listen on *:${conf.getInt("listen")}")
            embeddedServer(Netty, port = conf.getInt("listen"), host = "0.0.0.0") { configureRouting() }.start(wait = true)
        })
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

    companion object {

        lateinit var instance: SeorawCore
            private set
    }
}