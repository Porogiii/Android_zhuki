package com.example.zhuki.utils

import java.util.Calendar

object ZodiacUtils {

    fun getZodiacSign(day: Int, month: Int): String {
        return when (month) {
            0 -> if (day >= 20) "Водолей" else "Козерог" // Январь
            1 -> if (day >= 19) "Рыбы" else "Водолей"    // Февраль
            2 -> if (day >= 21) "Овен" else "Рыбы"       // Март
            3 -> if (day >= 20) "Телец" else "Овен"      // Апрель
            4 -> if (day >= 21) "Близнецы" else "Телец"  // Май
            5 -> if (day >= 21) "Рак" else "Близнецы"    // Июнь
            6 -> if (day >= 23) "Лев" else "Рак"         // Июль
            7 -> if (day >= 23) "Дева" else "Лев"        // Август
            8 -> if (day >= 23) "Весы" else "Дева"       // Сентябрь
            9 -> if (day >= 23) "Скорпион" else "Весы"   // Октябрь
            10 -> if (day >= 22) "Стрелец" else "Скорпион" // Ноябрь
            11 -> if (day >= 22) "Козерог" else "Стрелец"  // Декабрь
            else -> "Неизвестно"
        }
    }

    fun getZodiacDrawableResource(zodiacSign: String): String {
        return when (zodiacSign) {
            "Овен" -> "aries"
            "Телец" -> "taurus"
            "Близнецы" -> "gemini"
            "Рак" -> "cancer"
            "Лев" -> "leo"
            "Дева" -> "virgo"
            "Весы" -> "libra"
            "Скорпион" -> "scorpio"
            "Стрелец" -> "sagittarius"
            "Козерог" -> "capricorn"
            "Водолей" -> "aquarius"
            "Рыбы" -> "pisces"
            else -> "unknown"
        }
    }
}