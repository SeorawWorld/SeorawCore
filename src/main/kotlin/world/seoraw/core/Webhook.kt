package world.seoraw.core

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.io.File
import java.util.*

open class Payload(source: String) {

    val payload = JsonParser.parseString(source).asJsonObject!!

    fun getWorkflow(): Workflow? {
        if (payload.has("repository") && payload.has("action") && payload.get("action").asString == "completed") {
            return Workflow(payload.getAsJsonObject("workflow_run") ?: return null)
        }
        return null
    }
}

open class Workflow(payload: JsonObject) {

    val headBranch = payload.get("head_branch").asString!!

    val repositoryName = payload.getAsJsonObject("repository").get("name").asString!!
    val repositoryOwner = payload.getAsJsonObject("repository").getAsJsonObject("owner").get("login").asString!!

    val event = payload.get("event").asString!!
    val status = payload.get("status").asString!!
    val conclusion = payload.get("conclusion").asString!!

    val artifactsUrl = payload.get("artifacts_url").asString!!

    val headCommitMessage = payload.getAsJsonObject("head_commit").get("message").asString!!
    val headCommitAuthorName = payload.getAsJsonObject("head_commit").getAsJsonObject("author").get("name").asString!!

    open fun isMainBranch(): Boolean {
        return headBranch == "main" || headBranch == "master"
    }

    open fun isConclusionSuccess(): Boolean {
        return status == "completed" && conclusion == "success"
    }

    open fun checkUpdate() {
        if (repositoryOwner == "SeorawWorld" && (repositoryName == "SeorawPlugin" || repositoryName == "SeorawCore")) {
            SeorawCore.instance.logger.info("§c$repositoryName 收到新的推送，正在获取详细信息。")
            Bukkit.getScheduler().runTaskAsynchronously(SeorawCore.instance, Runnable {
                runBlocking {
                    HttpClient(CIO).use { client ->
                        var response: HttpResponse = client.get(artifactsUrl) {
                            headers["Authorization"] = "token ${SeorawCore.instance.conf.getString("github-token")}"
                        }
                        val artifacts = JsonParser.parseString(response.receive<ByteArray>().decodeToString()).asJsonObject
                        val artifact = artifacts.getAsJsonArray("artifacts")[0].asJsonObject
                        val archiveDownloadUrl = artifact.get("archive_download_url").asString
                        SeorawCore.instance.logger.info("§c正在下载...")
                        response = client.get(archiveDownloadUrl) {
                            headers["Authorization"] = "token ${SeorawCore.instance.conf.getString("github-token")}"
                        }
                        val file = newFile("plugins/update/${UUID.randomUUID()}.zip")
                        try {
                            file.writeBytes(response.readBytes())
                            SeorawCore.instance.logger.info("§c下载完成，开始部署。")
                            val unzipFile = newFile("plugins/update/${UUID.randomUUID()}", folder = true)
                            file.unzip(unzipFile)
                            val listFiles = unzipFile.listFiles()
                            if (listFiles != null && listFiles.isNotEmpty()) {
                                val newFile = File("plugins/update/$repositoryName.jar")
                                newFile.delete()
                                listFiles[0].copyTo(newFile)
                                if (newFile.exists()) {
                                    Bukkit.broadcast(Component.text("§e$repositoryName 收到新的有效推送, 服务器即将重新启动。"))
                                    Bukkit.broadcast(Component.text("§e来自: §7$headCommitAuthorName"))
                                    Bukkit.broadcast(Component.text("§e更新内容:"))
                                    headCommitMessage.lines().forEach { message ->
                                        Bukkit.broadcast(Component.text("§7 - $message"))
                                    }
                                    Bukkit.getScheduler().runTaskLater(SeorawCore.instance, Runnable {
                                        Bukkit.getOnlinePlayers().toList().forEach { player ->
                                            player.kickPlayer("§e$repositoryName 收到新的有效推送, 服务器即将重新启动。")
                                        }
                                    }, 100)
                                    Bukkit.getScheduler().runTaskLater(SeorawCore.instance, Runnable {
                                        Bukkit.shutdown()
                                    }, 200)
                                }
                            }
                            unzipFile.deepDelete()
                        } catch (ex: Throwable) {
                            ex.printStackTrace()
                        }
                        file.delete()
                    }
                }
            })
        }
    }
}
