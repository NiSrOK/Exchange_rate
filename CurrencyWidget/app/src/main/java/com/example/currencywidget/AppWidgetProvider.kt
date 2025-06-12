package com.example.currencywidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.util.Xml
import android.widget.RemoteViews
import kotlinx.coroutines.*
import org.xmlpull.v1.XmlPullParser
import java.net.URL

class CurrencyWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            val rates = fetchRates() // выполняется в фоновом потоке

            withContext(Dispatchers.Main) {
                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_layout)

                    val usdRaw = rates["USD"]
                    val cnyRaw = rates["CNY"]

                    val usdRate = usdRaw?.toFloatOrNull()?.let { String.format("%.2f", it) } ?: "--"
                    val cnyRate = cnyRaw?.toFloatOrNull()?.let { String.format("%.2f", it) } ?: "--"

                    views.setTextViewText(R.id.usd_text, "USD: $usdRate")
                    views.setTextViewText(R.id.cny_text, "CNY: $cnyRate")

                    Log.d("CurrencyWidget", "Updating widget, USD=$usdRate, CNY=$cnyRate")

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    private fun fetchRates(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        try {
            val url = URL("https://www.cbr.ru/scripts/XML_daily.asp")
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(url.openStream(), "windows-1251")

            var eventType = parser.eventType
            var currentTag = ""
            var currentCode = ""
            var currentValue = ""
            var insideValute = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "Valute") {
                            currentCode = ""
                            currentValue = ""
                            insideValute = true
                        }
                    }

                    XmlPullParser.TEXT -> {
                        val text = parser.text
                        if (insideValute) {
                            when (currentTag) {
                                "CharCode" -> currentCode = text
                                "Value" -> currentValue = text
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if (parser.name == "Valute") {
                            if (currentCode == "USD" || currentCode == "CNY") {
                                result[currentCode] = currentValue.replace(',', '.')
                            }
                            insideValute = false
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            result["USD"] = "ERR"
            result["CNY"] = "ERR"
        }
        return result
    }
}