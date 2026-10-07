package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.GovWorkApplication
import com.example.R
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.MainViewModel
import com.example.util.BackupRestoreManager
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun SettingsScreen(
    mainViewModel: MainViewModel,
    profile: UserProfileEntity?,
    onEditProfile: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appContainer = (context.applicationContext as GovWorkApplication).container

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // JSON file picker for Restore
    val restoreFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.use { it.readText() }
                pendingRestoreJson = jsonString
                showRestoreConfirmDialog = true
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read backup file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Employee Profile Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditProfile() }
                        .testTag("settings_profile_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile?.employeeName?.ifBlank { "Employee Profile" } ?: "Employee Profile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${profile?.designation ?: ""} • ${profile?.department ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Section 2: Preferences
            item {
                Text(
                    text = stringResource(R.string.settings_section_general),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column {
                        // Language
                        SettingsRow(
                            icon = Icons.Default.Language,
                            title = stringResource(R.string.settings_language),
                            subtitle = when (profile?.languageCode) {
                                "gu" -> "ગુજરાતી (Gujarati)"
                                "hi" -> "हिंदी (Hindi)"
                                else -> "English"
                            },
                            onClick = { showLanguageDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // Theme
                        SettingsRow(
                            icon = Icons.Default.DarkMode,
                            title = stringResource(R.string.settings_theme),
                            subtitle = when (profile?.themeMode) {
                                "light" -> stringResource(R.string.theme_light)
                                "dark" -> stringResource(R.string.theme_dark)
                                else -> stringResource(R.string.theme_system)
                            },
                            onClick = { showThemeDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // Notifications toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_notifications),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Receive task and meeting reminders",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = profile?.notificationsEnabled ?: true,
                                onCheckedChange = { mainViewModel.setNotificationsEnabled(it) }
                            )
                        }
                    }
                }
            }

            // Section 3: Security & Backup
            item {
                Text(
                    text = stringResource(R.string.settings_section_security),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column {
                        // App Lock
                        SettingsRow(
                            icon = Icons.Default.Lock,
                            title = stringResource(R.string.settings_app_lock),
                            subtitle = if (profile?.isAppLockEnabled == true) stringResource(R.string.app_lock_enabled) else stringResource(R.string.app_lock_disabled),
                            onClick = { showPinDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // Export Backup (JSON)
                        SettingsRow(
                            icon = Icons.Default.Backup,
                            title = stringResource(R.string.settings_backup),
                            subtitle = "Export encrypted/structured JSON backup",
                            onClick = {
                                coroutineScope.launch {
                                    val file = BackupRestoreManager.createJsonBackup(
                                        context = context,
                                        userProfileRepo = appContainer.userProfileRepository,
                                        taskRepo = appContainer.taskRepository,
                                        fileRepo = appContainer.fileRepository,
                                        meetingRepo = appContainer.meetingRepository,
                                        noteRepo = appContainer.noteRepository
                                    )
                                    if (file != null) {
                                        try {
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/json"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Save GovWork Backup"))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Backup saved to: ${file.name}", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // Restore Data
                        SettingsRow(
                            icon = Icons.Default.Restore,
                            title = stringResource(R.string.settings_restore),
                            subtitle = "Restore database from JSON backup file",
                            onClick = {
                                restoreFilePicker.launch(arrayOf("application/json", "*/*"))
                            }
                        )
                    }
                }
            }

            // Section 4: About
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAboutDialog = true },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.settings_about),
                        subtitle = stringResource(R.string.version_label),
                        onClick = { showAboutDialog = true }
                    )
                }
            }
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                Column {
                    listOf(
                        "en" to "English",
                        "gu" to "ગુજરાતી (Gujarati)",
                        "hi" to "हिंदी (Hindi)"
                    ).forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mainViewModel.setLanguage(code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.settings_theme)) },
            text = {
                Column {
                    listOf(
                        "system" to stringResource(R.string.theme_system),
                        "light" to stringResource(R.string.theme_light),
                        "dark" to stringResource(R.string.theme_dark)
                    ).forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mainViewModel.setTheme(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // App Lock PIN Configuration Dialog
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(stringResource(R.string.settings_app_lock)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (profile?.isAppLockEnabled == true) {
                        Text("App Lock is currently enabled.")
                        Button(
                            onClick = {
                                mainViewModel.setAppLock(null, false)
                                showPinDialog = false
                                Toast.makeText(context, "App lock disabled", Toast.LENGTH_SHORT).show()
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Disable App Lock")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Or update your 4-digit PIN:")
                    } else {
                        Text("Set a 4-digit security PIN to protect sensitive official data:")
                    }

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        label = { Text("4-Digit PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_new_pin")
                    )

                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 4) confirmPinInput = it },
                        label = { Text(stringResource(R.string.app_lock_confirm_pin)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_confirm_pin")
                    )

                    if (pinError != null) {
                        Text(text = pinError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length != 4) {
                            pinError = "PIN must be exactly 4 digits"
                        } else if (pinInput != confirmPinInput) {
                            pinError = "PINs do not match"
                        } else {
                            mainViewModel.setAppLock(pinInput, true)
                            showPinDialog = false
                            Toast.makeText(context, "App Lock enabled", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(stringResource(R.string.btn_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // Restore Confirmation Dialog
    if (showRestoreConfirmDialog && pendingRestoreJson != null) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingRestoreJson = null
            },
            title = { Text(stringResource(R.string.settings_restore)) },
            text = { Text(stringResource(R.string.dialog_restore_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = BackupRestoreManager.restoreFromJson(
                                jsonString = pendingRestoreJson!!,
                                userProfileRepo = appContainer.userProfileRepository,
                                taskRepo = appContainer.taskRepository,
                                fileRepo = appContainer.fileRepository,
                                meetingRepo = appContainer.meetingRepository,
                                noteRepo = appContainer.noteRepository
                            )
                            if (success) {
                                Toast.makeText(context, context.getString(R.string.toast_restore_success), Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Failed to restore backup", Toast.LENGTH_SHORT).show()
                            }
                            showRestoreConfirmDialog = false
                            pendingRestoreJson = null
                        }
                    }
                ) {
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmDialog = false
                        pendingRestoreJson = null
                    }
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.settings_about)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.about_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Security & Privacy: 100% Offline. All records are stored locally on your device in a private SQLite Room database.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "• Disclaimer: Personal utility app for individual official productivity. Not affiliated with or endorsed by any government entity.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.btn_dismiss))
                }
            }
        )
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}
