package com.example.ui.screens.teacher

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.MetricCard
import com.example.ui.common.VidyutAppBar
import com.example.ui.navigation.AppScreen
import com.example.ui.navigation.TeacherTab
import com.example.ui.screens.admin.AdminActionTile
import com.example.ui.theme.CircuitCyan
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.VidyutBluePrimary
import com.example.viewmodel.AppViewModel

@Composable
fun TeacherRootScreen(viewModel: AppViewModel) {
    val currentTab by viewModel.teacherTab.collectAsState()
    val unreadCount by viewModel.unreadNotifCount.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val teacher by viewModel.currentTeacher.collectAsState()

    Scaffold(
        topBar = {
            VidyutAppBar(
                title = "Faculty Portal",
                subtitle = user?.name ?: "Teacher Dashboard",
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
            val tabs = listOf(
                Pair(TeacherTab.OVERVIEW, "Overview"),
                Pair(TeacherTab.ATTENDANCE, "Attendance"),
                Pair(TeacherTab.ASSIGNMENTS, "Assignments"),
                Pair(TeacherTab.MATERIALS, "Notes & Notices")
            )
            val selectedIndex = tabs.indexOfFirst { it.first == currentTab }.coerceAtLeast(0)

            TabRow(
                selectedTabIndex = selectedIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CircuitCyan
            ) {
                tabs.forEachIndexed { index, (tab, label) ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { viewModel.teacherTab.value = tab },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.testTag("teacher_tab_${tab.name.lowercase()}")
                    )
                }
            }

            when (currentTab) {
                TeacherTab.OVERVIEW -> TeacherOverviewScreen(viewModel)
                TeacherTab.ATTENDANCE -> TeacherAttendanceScreen(viewModel)
                TeacherTab.ASSIGNMENTS -> TeacherAssignmentScreen(viewModel)
                TeacherTab.MATERIALS -> TeacherStudyMaterialScreen(viewModel)
                TeacherTab.REPORTS -> TeacherAttendanceScreen(viewModel)
            }
        }
    }
}

@Composable
fun TeacherOverviewScreen(viewModel: AppViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val teacher by viewModel.currentTeacher.collectAsState()
    val teacherAssignments by viewModel.teacherAssignmentsList.collectAsState()

    val pendingEvaluationsCount = teacherAssignments.sumOf { it.submissionCount - it.evaluatedCount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Faculty Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CircuitCyan),
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
                            text = "Department Faculty",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Code: ${teacher?.employeeCode ?: "EMP-EE-101"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = user?.name ?: "Prof. Rajesh Sharma",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${teacher?.designation ?: "Associate Professor"} • ${teacher?.department ?: "Electrical Engineering"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0F2FE)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Authorized for: EEE 3rd Year (Section A) • Power Systems Analysis (EE-501), Electrical Machines II (EE-502)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Metrics 2x2
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Assigned Classes",
                    value = "2",
                    icon = Icons.Default.Class,
                    iconTint = VidyutBluePrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Active Assignments",
                    value = "${teacherAssignments.size}",
                    icon = Icons.Default.Assignment,
                    iconTint = CircuitCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Submissions Awaiting Grading",
                    value = "$pendingEvaluationsCount",
                    icon = Icons.Default.PendingActions,
                    iconTint = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Today's Schedule",
                    value = "2 Lectures",
                    icon = Icons.Default.EventNote,
                    iconTint = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Navigation to Teacher Actions
        item {
            Text(
                text = "Academic Management Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminActionTile(
                    title = "Mark Class Attendance",
                    subtitle = "Select class, date, and mark Present/Absent/Late with duplicate prevention.",
                    icon = Icons.Default.CoPresent,
                    accentColor = CircuitCyan,
                    onClick = { viewModel.teacherTab.value = TeacherTab.ATTENDANCE }
                )
                AdminActionTile(
                    title = "Assignments & Grading",
                    subtitle = "Publish assignments, inspect student solutions, and enter marks & feedback.",
                    icon = Icons.Default.Assignment,
                    accentColor = VidyutBluePrimary,
                    onClick = { viewModel.teacherTab.value = TeacherTab.ASSIGNMENTS }
                )
                AdminActionTile(
                    title = "Study Notes & Materials",
                    subtitle = "Distribute lecture handouts, syllabus sheets, and lab assignment PDFs.",
                    icon = Icons.Default.FolderShared,
                    accentColor = Color(0xFF059669),
                    onClick = { viewModel.teacherTab.value = TeacherTab.MATERIALS }
                )
                AdminActionTile(
                    title = "Post Class Announcement",
                    subtitle = "Send notifications regarding exams, assignments, or lab schedules.",
                    icon = Icons.Default.Campaign,
                    accentColor = Color(0xFFF59E0B),
                    onClick = { viewModel.navigateTo(AppScreen.ANNOUNCEMENTS) }
                )
            }
        }
    }
}
