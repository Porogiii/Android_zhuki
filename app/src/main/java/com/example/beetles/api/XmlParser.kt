package com.example.beetles.api

import android.util.Log
import com.example.beetles.data.GoldRate
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

object XmlParser {
    private const val TAG = "XmlParser"

    fun parseGoldRate(xmlString: String): GoldRate? {
        if (xmlString.isBlank()) {
            Log.e(TAG, "XML пустой")
            return null
        }

        return try {
            Log.d(TAG, "Начинаем парсинг XML")

            val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = false }
            val parser = factory.newPullParser().apply { setInput(StringReader(xmlString)) }

            var eventType = parser.eventType
            var currentDate = ""
            var currentBuy = ""
            var currentSell = ""
            var inRecord = false
            var inGoldRecord = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name.lowercase()) {
                            "record" -> {
                                inRecord = true
                                inGoldRecord = false
                                for (i in 0 until parser.attributeCount) {
                                    if (parser.getAttributeName(i).equals("Code", ignoreCase = true)) {
                                        val code = parser.getAttributeValue(i)
                                        if (code == "1") {
                                            inGoldRecord = true
                                            currentDate = parser.getAttributeValue(null, "Date") ?: ""
                                            Log.d(TAG, "Найдена запись золота: $currentDate")
                                        }
                                    }
                                }
                            }
                            "buy" -> if (inGoldRecord) currentBuy = parser.nextText()
                            "sell" -> if (inGoldRecord) currentSell = parser.nextText()
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("Record", ignoreCase = true) && inGoldRecord) {
                            val buy = currentBuy.replace(",", ".").replace(" ", "").toDoubleOrNull() ?: 0.0
                            val sell = currentSell.replace(",", ".").replace(" ", "").toDoubleOrNull() ?: 0.0
                            val rate = if (buy > 0 && sell > 0) (buy + sell) / 2 else (buy + sell)

                            if (rate > 0) {
                                Log.d(TAG, "УСПЕШНО! Курс золота: $rate ₽/г на $currentDate")
                                return GoldRate(rate = rate, date = currentDate, buy = buy, sell = sell)
                            }
                        }
                    }
                }
                eventType = parser.next()
            }

            Log.w(TAG, "Золото не найдено в новом формате XML")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка парсинга XML", e)
            null
        }
    }
}