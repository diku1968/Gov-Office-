package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusInProgress
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusUrgent

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (priority.lowercase()) {
        "urgent" -> StatusUrgent.copy(alpha = 0.15f) to StatusUrgent
        "high" -> Color(0xFFEA580C).copy(alpha = 0.15f) to Color(0xFFEA580C)
        "medium" -> StatusPending.copy(alpha = 0.15f) to Color(0xFFD97706)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = priority.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "completed", "approved", "closed" -> StatusCompleted.copy(alpha = 0.15f) to StatusCompleted
        "in progress", "under process", "forwarded" -> StatusInProgress.copy(alpha = 0.15f) to StatusInProgress
        "pending", "received" -> StatusPending.copy(alpha = 0.15f) to Color(0xFFD97706)
        "rejected", "cancelled" -> StatusUrgent.copy(alpha = 0.15f) to StatusUrgent
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
