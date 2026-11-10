package com.kavyaraj.tillflow.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val orderTimeFormat = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.UK)

/** 1_731_259_800_000 → "10 Nov 2024, 17:30" in the device's time zone. */
fun formatOrderTime(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    orderTimeFormat.format(Instant.ofEpochMilli(epochMillis).atZone(zone))
