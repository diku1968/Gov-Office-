package com.example.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.entity.UserProfileEntity

@Composable
fun ProfileSetupScreen(
    currentProfile: UserProfileEntity?,
    onSaveProfile: (name: String, department: String, designation: String, office: String, empId: String) -> Unit,
    onSkip: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(currentProfile?.employeeName ?: "") }
    var department by remember { mutableStateOf(currentProfile?.department ?: "") }
    var designation by remember { mutableStateOf(currentProfile?.designation ?: "") }
    var office by remember { mutableStateOf(currentProfile?.office ?: "") }
    var empId by remember { mutableStateOf(currentProfile?.employeeId ?: "") }

    var nameError by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(72.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.profile_setup_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.profile_setup_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text(stringResource(R.string.profile_name) + " *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = nameError,
                supportingText = { if (nameError) Text("Name is required") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_profile_name")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text(stringResource(R.string.profile_department)) },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_profile_department")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = designation,
                onValueChange = { designation = it },
                label = { Text(stringResource(R.string.profile_designation)) },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_profile_designation")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = office,
                onValueChange = { office = it },
                label = { Text(stringResource(R.string.profile_office)) },
                leadingIcon = { Icon(Icons.Default.CorporateFare, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_profile_office")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = empId,
                onValueChange = { empId = it },
                label = { Text(stringResource(R.string.profile_emp_id)) },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_profile_emp_id")
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                if (onSkip != null) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_skip_profile")
                    ) {
                        Text(stringResource(R.string.btn_skip_optional))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            nameError = true
                        } else {
                            onSaveProfile(name.trim(), department.trim(), designation.trim(), office.trim(), empId.trim())
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_save_profile")
                ) {
                    Text(stringResource(R.string.btn_save_profile))
                }
            }
        }
    }
}
