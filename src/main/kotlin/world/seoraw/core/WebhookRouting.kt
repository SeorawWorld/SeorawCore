package world.seoraw.core

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.nio.charset.StandardCharsets

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
        get("/") { call.respondText(":)") }
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