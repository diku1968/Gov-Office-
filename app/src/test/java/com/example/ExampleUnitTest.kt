package com.example

import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testDateUtils_isToday() {
        val now = System.currentTimeMillis()
        assertTrue(DateUtils.isToday(now))
    }

    @Test
    fun testDateUtils_formatDate() {
        val formatted = DateUtils.formatDate(System.currentTimeMillis())
        assertTrue(formatted.contains("/"))
    }

    @Test
    fun testDateUtils_isOverdue() {
        val pastTime = System.currentTimeMillis() - 86400000L * 5 // 5 days ago
        assertTrue(DateUtils.isOverdue(pastTime))

        val futureTime = System.currentTimeMillis() + 86400000L * 5 // 5 days ahead
        assertFalse(DateUtils.isOverdue(futureTime))
    }
}
