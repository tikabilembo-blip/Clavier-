package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.R
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardThemeType

class ThemeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_CHANGE_THEME) {
            val themeKey = intent.getStringExtra(EXTRA_THEME_KEY)
            if (themeKey != null) {
                val theme = KeyboardThemeType.entries.find { it.key == themeKey }
                if (theme != null) {
                    val prefs = KeyboardPreferences.getInstance(context)
                    prefs.setTheme(theme)

                    // Refresh widget views
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val componentName = ComponentName(context, ThemeWidgetProvider::class.java)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                    onUpdate(context, appWidgetManager, appWidgetIds)
                }
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = KeyboardPreferences.getInstance(context)
        val currentTheme = prefs.settings.value.themeType

        val views = RemoteViews(context.packageName, R.layout.widget_theme_control)
        views.setTextViewText(R.id.widget_title, "Clavier: ${currentTheme.title}")

        // Pending Intents for theme buttons
        views.setOnClickPendingIntent(R.id.btn_theme_fleurs, createThemeIntent(context, KeyboardThemeType.FLEURS))
        views.setOnClickPendingIntent(R.id.btn_theme_eau, createThemeIntent(context, KeyboardThemeType.EAU))
        views.setOnClickPendingIntent(R.id.btn_theme_foret, createThemeIntent(context, KeyboardThemeType.FORET))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createThemeIntent(context: Context, theme: KeyboardThemeType): PendingIntent {
        val intent = Intent(context, ThemeWidgetProvider::class.java).apply {
            action = ACTION_CHANGE_THEME
            putExtra(EXTRA_THEME_KEY, theme.key)
        }
        return PendingIntent.getBroadcast(
            context,
            theme.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_CHANGE_THEME = "com.example.ACTION_CHANGE_THEME"
        const val EXTRA_THEME_KEY = "extra_theme_key"
    }
}
