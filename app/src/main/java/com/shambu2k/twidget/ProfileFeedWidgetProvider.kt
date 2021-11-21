package com.shambu2k.twidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

const val TWEET_CLICK = "TWEET_CLICK"
const val TWEET_LINK = "TWEET_LINK"

class ProfileFeedWidgetProvider : AppWidgetProvider() {
    private lateinit var sharedPrefUtil: SharedPrefUtil
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        sharedPrefUtil = SharedPrefUtil(context)
        for (appWidgetId in appWidgetIds) {
            val titleUrl = sharedPrefUtil.getTitleUrl(appWidgetId)
            if(titleUrl.size > 1) updateAppWidget(context, appWidgetManager, appWidgetId, titleUrl[0], titleUrl[1])
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TWEET_CLICK) {
            val tweetLink = intent.getStringExtra(TWEET_LINK)
            val twitterIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tweetLink))
            twitterIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(twitterIntent)
        }
        super.onReceive(context, intent)
    }

    companion object {
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            title: String,
            url: String
        ) {
            val intent = Intent(context, WidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra("RSS_LINK", url)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            val views = RemoteViews(context.packageName, R.layout.twitter_profile_feed).apply {
                setRemoteAdapter(R.id.tweets_list, intent)
                setCharSequence(R.id.profile_name, "setText", title)
                setEmptyView(R.id.tweets_list, R.id.empty_tv)
            }

            val toastPendingIntent: PendingIntent = Intent(
                context,
                ProfileFeedWidgetProvider::class.java
            ).run {
                action = TWEET_CLICK
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))

                PendingIntent.getBroadcast(context, 0, this, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            views.setPendingIntentTemplate(R.id.tweets_list, toastPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

