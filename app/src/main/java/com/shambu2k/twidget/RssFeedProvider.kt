package com.shambu2k.twidget

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.URL

class RssFeedProvider {
    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    suspend fun parse(url: String): List<Tweet> = withContext(Dispatchers.IO) {
        val inputStream = URL(url).openConnection().getInputStream()
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return@use readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): List<Tweet> {
        val tweets = mutableListOf<Tweet>()
        parser.require(XmlPullParser.START_TAG, ns, "rss")
        parser.next()
        parser.next()
        parser.next()
        while (parser.next() != XmlPullParser.END_TAG || parser.name != "channel") {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the item tag
            if (parser.name == "item") {
                tweets.add(readTweetItem(parser))
            } else {
                skip(parser)
            }
        }
        return tweets
    }

    // Reads a particular tweet item
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readTweetItem(parser: XmlPullParser): Tweet {
        parser.require(XmlPullParser.START_TAG, ns, "item")
        lateinit var title: String
        lateinit var date: String
        lateinit var link: String
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readTag(parser, "title")
                "pubDate" -> date = readTag(parser, "date")
                "link" -> link = readTag(parser, "link")
                else -> skip(parser)
            }
        }
        return Tweet(title, date, link)
    }

    // Reads child text of a tag
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTag(parser: XmlPullParser, tag: String): String {
        if(parser.name == null) parser.next()
        var tagText = ""
        if(parser.name == tag) tagText = readText(parser)
        return tagText
    }

    // Extracts text values in tags.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var text = ""
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.text
            parser.nextTag()
        }
        return text
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}