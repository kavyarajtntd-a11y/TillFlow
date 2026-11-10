package com.kavyaraj.tillflow.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId

class DatesTest {

    private val sale = 1_731_259_800_000L // 10 Nov 2024, 17:30 UTC

    @Test
    fun `formats in the given time zone`() {
        assertEquals("10 Nov 2024, 17:30", formatOrderTime(sale, ZoneId.of("Europe/London")))
        assertEquals("10 Nov 2024, 21:30", formatOrderTime(sale, ZoneId.of("Asia/Dubai")))
    }
}
