package com.topten.news

import java.net.URLEncoder

// Google News RSS, India English edition. No API key needed.
private const val EDITION = "hl=en-IN&gl=IN&ceid=IN:en"

private fun topic(name: String) =
    "https://news.google.com/rss/headlines/section/topic/$name?$EDITION"

private fun search(query: String) =
    "https://news.google.com/rss/search?q=" + URLEncoder.encode(query, "UTF-8") + "&$EDITION"

/** Edit the queries here to tune what each tab shows. */
enum class Category(val label: String, val feedUrl: String) {
    BUSINESS("Business", topic("BUSINESS")),
    ECONOMICS("Economics", search("India economy OR inflation OR GDP OR RBI when:2d")),
    POLITICAL("Political", search("India politics when:1d")),
    GEOGRAPHICAL("Geographical", search("India climate OR weather OR monsoon OR environment OR earthquake when:2d")),
    NATIONAL("National", topic("NATION")),
    FINANCE("Finance", search("Sensex OR Nifty OR \"stock market\" OR \"mutual funds\" OR \"personal finance\" when:1d")),
    TECH("Tech", topic("TECHNOLOGY")),
    TAMIL_NADU("Tamil Nadu", search("\"Tamil Nadu\" when:1d")),
    CHENNAI("Chennai", search("Chennai when:1d")),
    TRICHY("Trichy", search("Trichy OR Tiruchirappalli when:3d")),
}
