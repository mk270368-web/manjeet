package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminDashboardSummary
import com.example.ui.common.MetricCard
import com.example.ui.common.VidyutAppBar
import com.example.ui.navigation.AdminTab
import com.example.ui.navigation.AppScreen
import com.example.ui.theme.CircuitCyan
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.VidyutBluePrimary
import com.example.viewmodel.AppViewModel
import java.util.Locale

@Composable
fun AdminRootScreen(viewModel: AppViewModel) {
    val currentTab by viewModel.adminTab.collectAsState()
    val unreadCount by viewModel.unreadNotifCount.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            VidyutAppBar(
                title = "Admin Dashboard",
                subtitle = user?.name ?: "Institute Administration",
                unreadCount = unreadCount,
                onNotificationsClick = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) },
                onProfileClick = { viewModel.navigateTo(AppScreen.PROFILE_PASSWORD) },
                onLogoutClick = { viewModel.logout() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Admin Sub Tabs
            val tabs = listOf(
                Pair(AdminTab.DASHBOARD, "Overview"),
                Pair(AdminTab.USERS, "Users"),
                Pair(AdminTab.ACADEMICS, "Academics"),
                Pair(AdminTab.REPORTS, "Reports"),
                Pair(AdminTab.AUDIT, "Audit")
            )
            val selectedIndex = tabs.indexOfFirst { it.first == currentTab }.coerceAtLeast(0)

            TabRow(
                selectedTabIndex = selectedIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = VidyutBluePrimary
            ) {
                tabs.forEachIndexed { index, (tab, label) ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { viewModel.adminTab.value = tab },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}")
                    )
                }
            }

            // Render selected tab content
            when (currentTab) {
                AdminTab.DASHBOARD -> AdminDashboardOverview(viewModel)
                AdminTab.USERS -> AdminUserManagementScreen(viewModel)
                AdminTab.ACADEMICS -> AdminAcademicScreen(viewModel)
                AdminTab.REPORTS -> AdminReportsScreen(viewModel)
                AdminTab.AUDIT -> AdminAuditLogsScreen(viewModel)
            }
        }
    }
}

@Composable
fun AdminDashboardOverview(viewModel: AppViewModel) {
    val summary by viewModel.adminDashboardSummary.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and session banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VidyutBluePrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Electrical Engineering Wing",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricGold,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Session: 2024-2025",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Vidyut Gyan Kendra Administration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manage registered cohorts, faculty allocations, attendance logging, and academic evaluation policies.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD6E3FF)
                    )
                }
            }
        }

        // Summary Metric Cards 2x2
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total Students",
                        value = "${summary?.totalStudents ?: 4}",
                        icon = Icons.Default.School,
                        iconTint = VidyutBluePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total Faculty",
                        value = "${summary?.totalTeachers ?: 2}",
                        icon = Icons.Default.SupervisorAccount,
                        iconTint = CircuitCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Classes & Batches",
                        value = "${summary?.totalClasses ?: 3}",
                        icon = Icons.Default.Class,
                        iconTint = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Core Subjects",
                        value = "${summary?.totalSubjects ?: 5}",
                        icon = Icons.Default.MenuBook,
                        iconTint = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Today's Attendance Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Attendance Rate",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${summary?.todayAttendanceSessionsCount ?: 1} lectures recorded today",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${String.format(Locale.US, "%.1f", summary?.todayPresentPercentage ?: 92.5f)}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = StatusPresentGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { ((summary?.todayPresentPercentage ?: 92.5f) / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = StatusPresentGreen,
                        trackColor = Color(0xFFDCFCE7)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Campus attendance is within standard compliance (>75%).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Quick Administrative Actions
        item {
            Text(
                text = "Quick Administrative Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminActionTile(
                    title = "Manage Student & Faculty Accounts",
                    subtitle = "Create accounts, issue Login IDs & temporary passwords, activate/deactivate.",
                    icon = Icons.Default.PersonAdd,
                    accentColor = VidyutBluePrimary,
                    onClick = { viewModel.adminTab.value = AdminTab.USERS }
                )
                AdminActionTile(
                    title = "Class & Subject Curriculum",
                    subtitle = "View classes, assign faculty to subjects, and manage timetable.",
                    icon = Icons.Default.EventNote,
                    accentColor = CircuitCyan,
                    onClick = { viewModel.adminTab.value = AdminTab.ACADEMICS }
                )
                AdminActionTile(
                    title = "Consolidated Attendance & Export",
                    subtitle = "Generate class-wise attendance reports and export CSV data.",
                    icon = Icons.Default.QueryStats,
                    accentColor = Color(0xFF10B981),
                    onClick = { viewModel.adminTab.value = AdminTab.REPORTS }
                )
                AdminActionTile(
                    title = "Broadcast Institute Announcement",
                    subtitle = "Send official notices to students, faculty, or all campus users.",
                    icon = Icons.Default.Campaign,
                    accentColor = Color(0xFFF59E0B),
                    onClick = { viewModel.navigateTo(AppScreen.ANNOUNCEMENTS) }
                )
                AdminActionTile(
                    title = "Security & Audit Trail",
                    subtitle = "Inspect authenticated system activities, password changes, and logs.",
                    icon = Icons.Default.Security,
                    accentColor = Color(0xFF6366F1),
                    onClick = { viewModel.adminTab.value = AdminTab.AUDIT }
                )
            }
        }
    }
}

@Composable
fun AdminActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
