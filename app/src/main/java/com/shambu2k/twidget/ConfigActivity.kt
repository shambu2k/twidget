package com.shambu2k.twidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.webkit.URLUtil
import android.widget.RemoteViews
import com.google.android.material.snackbar.Snackbar
import com.shambu2k.twidget.databinding.ActivityConfigBinding
import kotlinx.coroutines.runBlocking

class ConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigBinding
    private lateinit var sharedPrefUtil: SharedPrefUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        binding = ActivityConfigBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        sharedPrefUtil = SharedPrefUtil(this)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val context = this
        binding.saveBtn.setOnClickListener {
            if(TextUtils.isEmpty(binding.widgetTitleIp.text.toString()) || TextUtils.isEmpty(binding.rssLinkIp.text.toString()) || !URLUtil.isValidUrl(binding.rssLinkIp.text.toString())) {
                Snackbar.make(binding.configLay, "Make sure both fields are not empty and url is valid.", Snackbar.LENGTH_SHORT).show()
            } else configureWidget(appWidgetId, context, sanitizeString(binding.widgetTitleIp.text.toString()), sanitizeString(binding.rssLinkIp.text.toString()))
        }
    }

    private fun configureWidget(appWidgetId: Int, context: Context, title: String, url: String) {

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val views = RemoteViews(context.packageName, R.layout.twitter_profile_feed)
        views.setCharSequence(R.id.profile_name, "setText", title)
        ProfileFeedWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId, title, url)
        runBlocking {
            sharedPrefUtil.addWidget(appWidgetId, title, url)
        }
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    private fun sanitizeString(link: String): String {
        var trimmedLink = link.trimEnd()
        trimmedLink = trimmedLink.trimStart()
        return trimmedLink
    }
}