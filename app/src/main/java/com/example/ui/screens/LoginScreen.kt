package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusAbsentRedBg
import com.example.ui.theme.VidyutBluePrimary
import com.example.viewmodel.AppViewModel

@Composable
fun LoginScreen(viewModel: AppViewModel) {
    val selectedRole by viewModel.loginRole.collectAsState()
    val loginId by viewModel.loginIdInput.collectAsState()
    val password by viewModel.passwordInput.collectAsState()
    val isLoading by viewModel.isLoginLoading.collectAsState()
    val errorMessage by viewModel.loginErrorMessage.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val roles = listOf(UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT)
    val selectedTabIndex = roles.indexOf(selectedRole)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Header with Back button and Institute Branding
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(VidyutBluePrimary)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("login_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ElectricGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = VidyutBluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Vidyut Gyan Kendra",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Electrical Institute Authentication",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sign in as ${selectedRole.displayName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = when (selectedRole) {
                        UserRole.ADMIN -> "Access institute master controls, user credentials, and academic sessions."
                        UserRole.TEACHER -> "Access class attendance rosters, assignments, and student submissions."
                        UserRole.STUDENT -> "Access your academic profile, attendance logs, and assignment submissions."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD6E3FF),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }

        // Role Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFF0D2550),
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = ElectricGold,
                    height = 3.dp
                )
            }
        ) {
            roles.forEachIndexed { index, role ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { viewModel.selectRoleForLogin(role) },
                    text = {
                        Text(
                            text = role.displayName.replace(" / Teacher", ""),
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) ElectricGold else Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = when (role) {
                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                UserRole.TEACHER -> Icons.Default.SupervisorAccount
                                UserRole.STUDENT -> Icons.Default.School
                            },
                            contentDescription = null,
                            tint = if (selectedTabIndex == index) ElectricGold else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.testTag("login_tab_${role.name.lowercase()}")
                )
            }
        }

        // Form Body
        Column(modifier = Modifier.padding(20.dp)) {
            // Error Message Banner
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusAbsentRedBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = StatusAbsentRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusAbsentRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Quick Fill Chips for Testing
            Text(
                text = "Quick Demo Auto-Fill:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { viewModel.selectRoleForLogin(UserRole.ADMIN) },
                    label = { Text("Admin (ADM001)", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (selectedRole == UserRole.ADMIN) ElectricGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("quick_fill_admin")
                )
                AssistChip(
                    onClick = { viewModel.selectRoleForLogin(UserRole.TEACHER) },
                    label = { Text("Teacher (TCH101)", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (selectedRole == UserRole.TEACHER) ElectricGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("quick_fill_teacher")
                )
                AssistChip(
                    onClick = { viewModel.selectRoleForLogin(UserRole.STUDENT) },
                    label = { Text("Student", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (selectedRole == UserRole.STUDENT) ElectricGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("quick_fill_student")
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Login ID
            OutlinedTextField(
                value = loginId,
                onValueChange = { viewModel.loginIdInput.value = it },
                label = { Text("Assigned Login ID") },
                placeholder = {
                    Text(
                        when (selectedRole) {
                            UserRole.ADMIN -> "e.g., ADM001"
                            UserRole.TEACHER -> "e.g., TCH101"
                            UserRole.STUDENT -> "e.g., STU202401"
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = VidyutBluePrimary
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_id_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VidyutBluePrimary,
                    focusedLabelColor = VidyutBluePrimary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.passwordInput.value = it },
                label = { Text("Password") },
                placeholder = { Text("Enter your password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = VidyutBluePrimary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { viewModel.performLogin() }),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VidyutBluePrimary,
                    focusedLabelColor = VidyutBluePrimary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In Button
            Button(
                onClick = { viewModel.performLogin() },
                enabled = !isLoading && loginId.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VidyutBluePrimary,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign In to ${selectedRole.displayName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Help info
            Text(
                text = "Forgot password or credentials? Contact Institute Academic Administrator for temporary password reset.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
