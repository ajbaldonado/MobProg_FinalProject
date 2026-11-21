package com.example.finalprojectdraft

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object MediaApiHelper {

    fun searchSuggestions(categoryName: String, query: String, onResult: (List<String>) -> Unit) {
        if (query.trim().length < 2) {
            onResult(emptyList())
            return
        }

        Thread {
            val results = mutableListOf<String>()
            try {
                val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
                val categoryLower = categoryName.lowercase()

                val urlString = when {
                    categoryLower.contains("anime") ->
                        "https://api.jikan.moe/v4/anime?q=$encodedQuery&limit=5"
                    categoryLower.contains("manga") ->
                        "https://api.jikan.moe/v4/manga?q=$encodedQuery&limit=5"
                    categoryLower.contains("book") ->
                        "https://www.googleapis.com/books/v1/volumes?q=$encodedQuery&maxResults=5"
                    else ->
                        "https://itunes.apple.com/search?term=$encodedQuery&entity=movie&limit=5"
                }

                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 4000
                conn.readTimeout = 4000

                if (conn.responseCode == 200) {
                    val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)

                    if (categoryLower.contains("anime") || categoryLower.contains("manga")) {
                        if (json.has("data")) {
                            val dataArr = json.getJSONArray("data")
                            for (i in 0 until dataArr.length()) {
                                val item = dataArr.getJSONObject(i)
                                if (item.has("title")) {
                                    results.add(item.getString("title"))
                                }
                            }
                        }
                    } else if (categoryLower.contains("book")) {
                        if (json.has("items")) {
                            val itemsArr = json.getJSONArray("items")
                            for (i in 0 until itemsArr.length()) {
                                val volumeInfo = itemsArr.getJSONObject(i).optJSONObject("volumeInfo")
                                if (volumeInfo != null && volumeInfo.has("title")) {
                                    results.add(volumeInfo.getString("title"))
                                }
                            }
                        }
                    } else {
                        if (json.has("results")) {
                            val resultsArr = json.getJSONArray("results")
                            for (i in 0 until resultsArr.length()) {
                                val item = resultsArr.getJSONObject(i)
                                if (item.has("trackName")) {
                                    results.add(item.getString("trackName"))
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Handler(Looper.getMainLooper()).post {
                onResult(results.distinct())
            }
        }.start()
    }
}
