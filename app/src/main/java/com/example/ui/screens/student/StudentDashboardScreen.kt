package com.example.ui.screens.student

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.example.data.model.AttendanceStatus
import com.example.data.model.SubmissionStatus
import com.example.ui.common.AttendanceStatusChip
import com.example.ui.common.MetricCard
import com.example.ui.common.SubmissionStatusChip
import com.example.ui.common.VidyutAppBar
import com.example.ui.navigation.AppScreen
import com.example.ui.navigation.StudentTab
import com.example.ui.theme.CircuitCyan
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusAbsentRedBg
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.VidyutBluePrimary
import com.example.viewmodel.AppViewModel
import java.util.Locale

@Composable
fun StudentRootScreen(viewModel: AppViewModel) {
    val currentTab by viewModel.studentTab.collectAsState()
    val unreadCount by viewModel.unreadNotifCount.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val student by viewModel.currentStudent.collectAsState()

    Scaffold(
        topBar = {
            VidyutAppBar(
                title = "Student Portal",
                subtitle = user?.name ?: "Student Dashboard",
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
                Pair(StudentTab.OVERVIEW, "Overview"),
                Pair(StudentTab.ATTENDANCE, "Attendance"),
                Pair(StudentTab.ASSIGNMENTS, "Assignments"),
                Pair(StudentTab.TIMETABLE, "Timetable"),
                Pair(StudentTab.FACULTY, "Faculty")
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
                        onClick = { viewModel.studentTab.value = tab },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.testTag("student_tab_${tab.name.lowercase()}")
                    )
                }
            }

            when (currentTab) {
                StudentTab.OVERVIEW -> StudentOverviewScreen(viewModel)
                StudentTab.ATTENDANCE -> StudentAttendanceScreen(viewModel)
                StudentTab.ASSIGNMENTS -> StudentAssignmentScreen(viewModel)
                StudentTab.TIMETABLE -> StudentTimetableScreen(viewModel)
                StudentTab.FACULTY -> StudentContactTeacherScreen(viewModel)
            }
        }
    }
}

@Composable
fun StudentOverviewScreen(viewModel: AppViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val student by viewModel.currentStudent.collectAsState()
    val subjectStats by viewModel.studentSubjectStats.collectAsState()
    val assignments by viewModel.studentAssignments.collectAsState()

    val totalConducted = subjectStats.sumOf { it.totalConducted }.let { if (it > 0) it else 24 }
    val totalAttended = subjectStats.sumOf { it.attendedCount }.let { if (it > 0) it else 21 }
    val overallPercentage = if (totalConducted > 0) (totalAttended.toFloat() / totalConducted) * 100f else 87.5f
    val isLowAttendance = overallPercentage < 75.0f

    val pendingAssignmentsCount = assignments.count { it.currentStudentSubmission == null }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Student Profile Header Card
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
                            text = "Electrical & Electronics Engineering",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricGold,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = ElectricGold,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Sem ${student?.semester ?: 5} - Sec ${student?.section ?: "A"}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = user?.name ?: "Aarav Patel",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Roll Number: ${student?.rollNumber ?: "EE-2024-001"} | Login ID: ${user?.loginId ?: "STU202401"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD6E3FF)
                    )
                }
            }
        }

        // Attendance Alert Banner (Prominent Warning if < 75%)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isLowAttendance) StatusAbsentRedBg else Color(0xFFEFF6FF)
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isLowAttendance) StatusAbsentRed else Color(0xFFBFDBFE)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLowAttendance) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isLowAttendance) StatusAbsentRed else StatusPresentGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isLowAttendance) "Low Attendance Warning (<75%)" else "Attendance Compliance Status",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowAttendance) StatusAbsentRed else VidyutBluePrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isLowAttendance)
                                "Your overall attendance is ${String.format(Locale.US, "%.1f", overallPercentage)}%. Maintain at least 75% to remain eligible for semester examinations."
                            else
                                "Your overall attendance is ${String.format(Locale.US, "%.1f", overallPercentage)}%. You satisfy institutional examination eligibility rules.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isLowAttendance) Color(0xFF991B1B) else Color(0xFF1E3A8A),
                            lineHeight = 16.sp
                        )
                    }
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
                    title = "Overall Attendance",
                    value = "${String.format(Locale.US, "%.1f", overallPercentage)}%",
                    icon = Icons.Default.CoPresent,
                    iconTint = if (isLowAttendance) StatusAbsentRed else StatusPresentGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Classes Attended",
                    value = "$totalAttended / $totalConducted",
                    icon = Icons.Default.School,
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
                    title = "Pending Assignments",
                    value = "$pendingAssignmentsCount",
                    icon = Icons.Default.Assignment,
                    iconTint = if (pendingAssignmentsCount > 0) Color(0xFFD97706) else StatusPresentGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Today's Lectures",
                    value = "2 Scheduled",
                    icon = Icons.Default.EventNote,
                    iconTint = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Upcoming Assignments Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Class Assignments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = VidyutBluePrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.studentTab.value = StudentTab.ASSIGNMENTS }
                )
            }
        }

        items(assignments.take(3)) { asg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.studentTab.value = StudentTab.ASSIGNMENTS },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = asg.subjectCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VidyutBluePrimary
                        )
                        SubmissionStatusChip(
                            status = asg.currentStudentSubmission?.status ?: SubmissionStatus.PENDING
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = asg.assignment.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Due: ${asg.assignment.deadline} • Faculty: ${asg.teacherName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
