package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyUtils {
    private val arSymbols = DecimalFormatSymbols(Locale("es", "AR")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val arFormat = DecimalFormat("$#,##0", arSymbols)

    fun formatAr(amount: Double): String {
        return arFormat.format(amount)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "AR"))
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale("es", "AR"))
        return sdf.format(Date(timestamp))
    }

    fun isToday(timestamp: Long): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date(timestamp)) == sdf.format(Date())
    }
}
