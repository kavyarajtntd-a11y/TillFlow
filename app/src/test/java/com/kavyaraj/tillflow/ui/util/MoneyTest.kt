package com.kavyaraj.tillflow.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {

    @Test
    fun `formats pence as pounds`() {
        assertEquals("£0.00", formatPence(0))
        assertEquals("£0.05", formatPence(5))
        assertEquals("£12.50", formatPence(1250))
    }

    @Test
    fun `adds thousands separators`() {
        assertEquals("£1,234.56", formatPence(123_456))
    }
}
