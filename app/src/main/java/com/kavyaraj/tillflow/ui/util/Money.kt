package com.kavyaraj.tillflow.ui.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/** 1250 → "£12.50". BigDecimal keeps the conversion exact; no floating point involved. */
fun formatPence(pence: Long, locale: Locale = Locale.UK): String =
    NumberFormat.getCurrencyInstance(locale).format(BigDecimal.valueOf(pence, 2))
