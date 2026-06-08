package com.example.ui.components

import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale

object JalaliCalendar {
    fun g2j(gy: Int, gm: Int, gd: Int): IntArray {
        val gDaysInM = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 335)
        val gy2 = if (gm > 2) gy + 1 else gy
        var gDays = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gDaysInM[gm - 1] + gd
        var jy = -1595 + (33 * (gDays / 12053))
        gDays %= 12053
        jy += 4 * (gDays / 1461)
        gDays %= 1461
        if (gDays > 365) {
            jy += ((gDays - 1) / 365)
            gDays = (gDays - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (gDays < 186) {
            jm = 1 + (gDays / 31)
            jd = 1 + (gDays % 31)
        } else {
            jm = 7 + ((gDays - 186) / 30)
            jd = 1 + ((gDays - 186) % 30)
        }
        return intArrayOf(jy, jm, jd)
    }
}

fun convertDigitsToPersian(text: String): String {
    val persianDigits = arrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return text.map { char ->
        if (char in '0'..'9') persianDigits[char - '0'] else char
    }.joinToString("")
}

fun formatCurrency(amount: Double): String {
    val formatter = DecimalFormat("#,###")
    val formatted = formatter.format(amount)
    return convertDigitsToPersian(formatted) + " تومان"
}

fun formatJalaliDate(timestamp: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    val gy = cal.get(Calendar.YEAR)
    val gm = cal.get(Calendar.MONTH) + 1
    val gd = cal.get(Calendar.DAY_OF_MONTH)
    val jalali = JalaliCalendar.g2j(gy, gm, gd)
    return convertDigitsToPersian(String.format(Locale.US, "%04d/%02d/%02d", jalali[0], jalali[1], jalali[2]))
}

fun getRelativeDateString(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val startOfTarget = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val diffMs = startOfTarget - startOfToday
    val diffDays = (diffMs / (24 * 60 * 60 * 1000)).toInt()

    return when (diffDays) {
        0 -> "امروز"
        1 -> "فردا"
        2 -> "پس‌فردا"
        -1 -> "دیروز"
        -2 -> "پریروز"
        else -> {
            if (diffDays > 2) {
                convertDigitsToPersian("$diffDays روز دیگر")
            } else {
                convertDigitsToPersian("${-diffDays} روز پیش")
            }
        }
    }
}
