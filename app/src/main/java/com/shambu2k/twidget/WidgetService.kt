package com.shambu2k.twidget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import kotlinx.coroutines.runBlocking

class WidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return ListRemoteViewsFactory(applicationContext, intent?.extras?.get("RSS_LINK") as String)
    }
}

class ListRemoteViewsFactory(
    private val context: Context,
    private val url: String
) : RemoteViewsService.RemoteViewsFactory {

    private lateinit var tweets: List<Tweet>

    override fun onCreate() {
        val rssFeedProvider = RssFeedProvider()
        runBlocking {
            tweets = rssFeedProvider.parse(url)
        }
    }

    override fun onDataSetChanged() {

    }

    override fun onDestroy() {

    }

    override fun getCount(): Int {
        return tweets.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        return RemoteViews(context.packageName, R.layout.list_item).apply {
            setTextViewText(R.id.tweet_text, tweets[position].tweet_text)
            val fillInIntent = Intent().apply {
                Bundle().also { extras ->
                    extras.putString(TWEET_LINK, tweets[position].link)
                    putExtras(extras)
                }
            }
            setOnClickFillInIntent(R.id.tweet_text, fillInIntent)
        }
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
