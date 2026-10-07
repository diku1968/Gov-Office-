package com.example.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun MoreScreen(
    onNavigateToNotes: () -> Unit,
    onNavigateToVoiceNotes: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Executive Tools & Workspace",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                MoreMenuCard(
                    title = stringResource(R.string.notes_title),
                    subtitle = "Quick memos, draft notices and reference logs",
                    icon = Icons.Default.Note,
                    iconColor = Color(0xFFB45309),
                    tag = "menu_notes",
                    onClick = onNavigateToNotes
                )
            }

            item {
                MoreMenuCard(
                    title = stringResource(R.string.voice_notes_title),
                    subtitle = "Record audio memos & convert to tasks or notes",
                    icon = Icons.Default.Mic,
                    iconColor = MaterialTheme.colorScheme.primary,
                    tag = "menu_voice_notes",
                    onClick = onNavigateToVoiceNotes
                )
            }

            item {
                MoreMenuCard(
                    title = stringResource(R.string.reports_title),
                    subtitle = "Generate and export official Daily, Weekly & Monthly PDF reports",
                    icon = Icons.Default.Assessment,
                    iconColor = Color(0xFF0F766E),
                    tag = "menu_reports",
                    onClick = onNavigateToReports
                )
            }

            item {
                MoreMenuCard(
                    title = stringResource(R.string.settings_title),
                    subtitle = "Profile, language (English/Gujarati/Hindi), app lock PIN & backup",
                    icon = Icons.Default.Settings,
                    iconColor = Color(0xFF64748B),
                    tag = "menu_settings",
                    onClick = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
fun MoreMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
