package com.topten.news

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

data class NewsItem(
    val title: String,
    val link: String,
    val source: String,
    val publishedAt: Long?,
)

object NewsRepository {

    suspend fun topTen(category: Category): List<NewsItem> = withContext(Dispatchers.IO) {
        val connection = URL(category.feedUrl).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) TopTenNews/1.0")
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            connection.inputStream.use { parseFeed(it) }
                .distinctBy { it.title.lowercase(Locale.ROOT) }
                .distinctBy { it.link }
                .take(10)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseFeed(input: InputStream): List<NewsItem> {
        val parser = Xml.newPullParser()
        parser.setInput(input, null)

        val items = mutableListOf<NewsItem>()
        var inItem = false
        var title = ""
        var link = ""
        var source = ""
        var pubDate = ""

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "item" -> {
                        inItem = true
                        title = ""; link = ""; source = ""; pubDate = ""
                    }
                    "title" -> if (inItem) title = parser.nextText()
                    "link" -> if (inItem) link = parser.nextText()
                    "source" -> if (inItem) source = parser.nextText()
                    "pubDate" -> if (inItem) pubDate = parser.nextText()
                }
                XmlPullParser.END_TAG -> if (parser.name == "item") {
                    inItem = false
                    if (title.isNotBlank() && link.isNotBlank()) {
                        items += NewsItem(
                            title = cleanTitle(title, source),
                            link = link.trim(),
                            source = source.trim(),
                            publishedAt = parseDate(pubDate),
                        )
                    }
                }
            }
            event = parser.next()
        }
        return items
    }

    // Google News titles end with " - Source Name"; the source is shown separately.
    private fun cleanTitle(raw: String, source: String): String {
        val title = raw.trim()
        val suffix = " - ${source.trim()}"
        return if (source.isNotBlank() && title.endsWith(suffix)) title.removeSuffix(suffix).trim() else title
    }

    private fun parseDate(value: String): Long? = try {
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH).parse(value.trim())?.time
    } catch (e: ParseException) {
        null
    }
}
