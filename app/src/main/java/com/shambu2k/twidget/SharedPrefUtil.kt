package com.shambu2k.twidget

import android.content.Context
import android.content.SharedPreferences

class SharedPrefUtil(context: Context) {
    private val WIDGET_DETAILS = "WidgetsDetails"

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        WIDGET_DETAILS,
        Context.MODE_PRIVATE
    )

    suspend fun addWidget(appWidgetId: Int, title: String, url: String) {
        sharedPreferences.edit().putString(appWidgetId.toString(), "$title | $url").apply()
    }

    suspend fun removeWidget(appWidgetId: Int) {
        sharedPreferences.edit().remove(appWidgetId.toString()).apply()
    }

    fun getTitleUrl(appWidgetId: Int): List<String> {
        val titleUrl = sharedPreferences.getString(appWidgetId.toString(), "")
        return titleUrl?.split("|")!!
    }
}