package world.seoraw.core

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import org.apache.commons.lang.time.DateFormatUtils
import org.bukkit.Bukkit
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow


fun Application.configureRouting() {
    routing {
        post("/github_webhook") {
            val payload = Payload(call.receiveText().toByteArray(StandardCharsets.ISO_8859_1).toString(StandardCharsets.UTF_8))
            val workflow = payload.getWorkflow()
            if (workflow != null && workflow.isMainBranch() && workflow.isConclusionSuccess()) {
                workflow.checkUpdate()
            }
            call.respondJson { addProperty("message", "done") }
        }
        get("/backup") {
            val parameters = call.parameters
            val file = parameters["file"]
            if (file != null && file.isNotEmpty()) {
                if (File("backup", file).canonicalFile.parentFile.canonicalPath != File("backup").canonicalPath) {
                    call.respondText("Access denied")
                } else {
                    call.respondFile(File("backup", file))
                }
            } else {
                call.respondHtml {
                    head {
                        title = "Seoraw's World Backups"
                    }
                    body {
                        h1 { +"Seoraw's World Backups" }
                        val backup = File("backup").listFiles()
                        if (backup != null && backup.isNotEmpty()) {
                            table {
                                tr {
                                    td { +"World" }
                                    td { +"Last Modified" }
                                    td { +"Size" }
                                }
                                backup.forEach { world ->
                                    tr {
                                        td { a("backup?file=" + world.name) { +world.name } }
                                        td { +DateFormatUtils.format(world.lastModified(), "yyyy/MM/dd HH:mm:ss") }
                                        td { +readableFileSize(world.length()) }
                                    }
                                }
                            }
                        } else {
                            p { +"No Backups" }
                        }
                    }
                }
            }
        }
        get("/") {
            call.respondJson {
                add("players", JsonArray().also {
                    Bukkit.getOnlinePlayers().forEach { player ->
                        it.add(JsonObject().also { p ->
                            p.addProperty("name", player.name)
                            p.addProperty("health", player.health)
                            p.addProperty("food_level", player.foodLevel)
                            p.addProperty("level", player.level)
                            p.addProperty("exp", player.exp)
                            p.addProperty("ping", player.ping)
                            p.addProperty("allow_flight",  player.allowFlight)
                            p.addProperty("is_flying", player.isFlying)
                            p.addProperty("is_op", player.isOp)
                            p.addProperty("client_brand_name", player.clientBrandName)
                        })
                    }
                })
                add("plugins", JsonArray().also {
                    Bukkit.getPluginManager().plugins.forEach { plugin ->
                        it.add(JsonObject().also { p ->
                            p.addProperty("id", plugin.name)
                            p.addProperty("main", plugin.description.main)
                            p.addProperty("version", plugin.description.version)
                            p.add("authors", JsonArray().also { a ->
                                plugin.description.authors.forEach { author -> a.add(author) }
                            })
                        })
                    }
                })
            }
        }
    }
}

suspend fun ApplicationCall.respondJson(json: JsonObject.() -> Unit) {
    respondText(buildJson(json))
}

val gson = GsonBuilder().setPrettyPrinting().create()!!

fun buildJson(json: JsonObject.() -> Unit): String {
    return gson.toJson(JsonObject().apply(json))
}

fun buildJsonArray(vararg array: JsonObject): JsonArray {
    return JsonArray().also { json -> array.forEach { json.add(it) } }
}

fun buildJsonObject(json: JsonObject.() -> Unit): JsonObject {
    return JsonObject().apply(json)
}

fun readableFileSize(size: Long): String {
    if (size <= 0) return "0"
    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
}
