package dev.melncat.est.util

import com.google.gson.JsonObject
import com.google.gson.JsonElement
import xyz.xenondevs.nova.util.data.getString
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture

private val client = HttpClient.newHttpClient()

fun translate(query: String, to: String): String {
	val req =
		HttpRequest.newBuilder(URI.create("https://api.mymemory.translated.net/get?q=${
			URLEncoder.encode(query, "UTF-8")
		}&langpair=${URLEncoder.encode("en|$to", "UTF-8")}&de=${URLEncoder.encode("borneliushubert@gmail.com", "UTF-8")}"))
			.GET()
			.build()

	val t = client.send(req, HttpResponse.BodyHandlers.ofString())
	try {
		val json = gson.fromJson(t.body(), JsonElement::class.java)
		if (!json.isJsonObject) {
			println("err $json is ${gson.toJson(json)}")
			return query
		}
		return json.asJsonObject.getAsJsonObject("responseData").getString("translatedText") ?: query
	} catch (e: Exception) {
		println("err ${t.body()}")
		return query
	}


}

