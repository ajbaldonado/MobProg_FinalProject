package com.example.finalprojectdraft

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object MediaApiHelper {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingRunnable: Runnable? = null
    private var latestRequestId = 0

    fun searchSuggestions(categoryName: String, query: String, onResult: (List<String>) -> Unit) {
        pendingRunnable?.let { mainHandler.removeCallbacks(it) }

        val cleanQuery = query.trim()
        if (cleanQuery.length < 3) {
            onResult(emptyList())
            return
        }

        val currentRequestId = ++latestRequestId

        // Wait 450ms after user stops typing before hitting the API (prevents 429 rate limits)
        val runnable = Runnable {
            Thread {
                val results = mutableListOf<String>()
                try {
                    val encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8")
                    val categoryLower = categoryName.lowercase()

                    val urlString = when {
                        categoryLower.contains("anime") ->
                            "https://api.jikan.moe/v4/anime?q=$encodedQuery&limit=5"
                        categoryLower.contains("manga") ->
                            "https://api.jikan.moe/v4/manga?q=$encodedQuery&limit=5"
                        categoryLower.contains("book") || categoryLower.contains("novel") ->
                            "https://www.googleapis.com/books/v1/volumes?q=$encodedQuery&maxResults=5"
                        categoryLower.contains("tv") || categoryLower.contains("show") || categoryLower.contains("series") ->
                            "https://itunes.apple.com/search?term=$encodedQuery&entity=tvSeason&limit=5"
                        else ->
                            "https://itunes.apple.com/search?term=$encodedQuery&entity=movie&limit=5"
                    }

                    val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("User-Agent", "OmnitainmentApp/1.0")
                        connectTimeout = 5000
                        readTimeout = 5000
                    }

                    val code = conn.responseCode
                    if (code == 200) {
                        val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(responseText)

                        when {
                            categoryLower.contains("anime") || categoryLower.contains("manga") -> {
                                val dataArr = json.optJSONArray("data")
                                if (dataArr != null) {
                                    for (i in 0 until dataArr.length()) {
                                        val item = dataArr.getJSONObject(i)
                                        // Prefer English title if available so AutoCompleteTextView matches user input
                                        val englishTitle = item.optString("title_english", "")
                                        val defaultTitle = item.optString("title", "")
                                        val chosenTitle = if (englishTitle.isNotBlank() && englishTitle != "null") englishTitle else defaultTitle
                                        if (chosenTitle.isNotBlank()) results.add(chosenTitle)
                                    }
                                }
                            }
                            categoryLower.contains("book") || categoryLower.contains("novel") -> {
                                val itemsArr = json.optJSONArray("items")
                                if (itemsArr != null) {
                                    for (i in 0 until itemsArr.length()) {
                                        val title = itemsArr.getJSONObject(i)
                                            .optJSONObject("volumeInfo")
                                            ?.optString("title", "")
                                        if (!title.isNullOrBlank()) results.add(title)
                                    }
                                }
                            }
                            categoryLower.contains("tv") || categoryLower.contains("show") || categoryLower.contains("series") -> {
                                val resultsArr = json.optJSONArray("results")
                                if (resultsArr != null) {
                                    for (i in 0 until resultsArr.length()) {
                                        val showName = resultsArr.getJSONObject(i).optString("collectionName", "")
                                        if (showName.isNotBlank()) results.add(showName)
                                    }
                                }
                            }
                            else -> {
                                val resultsArr = json.optJSONArray("results")
                                if (resultsArr != null) {
                                    for (i in 0 until resultsArr.length()) {
                                        val movieName = resultsArr.getJSONObject(i).optString("trackName", "")
                                        if (movieName.isNotBlank()) results.add(movieName)
                                    }
                                }
                            }
                        }
                    } else {
                        Log.e("MediaApiHelper", "HTTP Error $code for URL: $urlString")
                    }
                } catch (e: Exception) {
                    Log.e("MediaApiHelper", "API Request failed: ${e.message}", e)
                }

                // Only update UI if this is still the newest search request
                if (currentRequestId == latestRequestId) {
                    mainHandler.post {
                        onResult(results.distinct())
                    }
                }
            }.start()
        }

        pendingRunnable = runnable
        mainHandler.postDelayed(runnable, 450)
    }
}