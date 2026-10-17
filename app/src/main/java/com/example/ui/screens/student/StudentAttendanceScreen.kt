package com.example.ui.screens.student

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.SubjectAttendanceStat
import com.example.ui.common.AttendanceStatusChip
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusLateAmber
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.VidyutBluePrimary
import com.example.viewmodel.AppViewModel
import java.util.Locale

@Composable
fun StudentAttendanceScreen(viewModel: AppViewModel) {
    val subjectStats by viewModel.studentSubjectStats.collectAsState()
    val recentRecords by viewModel.studentRecentAttendance.collectAsState()

    // Default sample stats if dynamic seed flow is still populating
    val displayStats = if (subjectStats.isNotEmpty()) subjectStats else listOf(
        SubjectAttendanceStat("sub_ee501", "Power Systems Analysis", "EE-501", 12, 11, 1, 0, 91.7f),
        SubjectAttendanceStat("sub_ee502", "Electrical Machines II", "EE-502", 10, 8, 2, 1, 80.0f),
        SubjectAttendanceStat("sub_ee503", "Control Systems Engineering", "EE-503", 8, 7, 1, 0, 87.5f),
        SubjectAttendanceStat("sub_ee504", "Microprocessors & Interfacing", "EE-504", 6, 5, 1, 0, 83.3f)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Institutional Security Notice
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = VidyutBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Attendance records are officially verified by departmental faculty. Self-marking is prohibited.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "Subject-Wise Attendance Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(displayStats) { stat ->
            val color = when {
                stat.attendancePercentage >= 75.0f -> StatusPresentGreen
                stat.attendancePercentage >= 65.0f -> StatusLateAmber
                else -> StatusAbsentRed
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stat.subjectName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Code: ${stat.subjectCode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${String.format(Locale.US, "%.1f", stat.attendancePercentage)}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = color
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (stat.attendancePercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = color,
                        trackColor = color.copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Conducted: ${stat.totalConducted}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Attended: ${stat.attendedCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusPresentGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Missed: ${stat.missedCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusAbsentRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recent Lecture Attendance Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            val sampleLog = listOf(
                Triple("2026-10-16", "Power Systems Analysis (09:00 - 10:00 AM)", AttendanceStatus.PRESENT),
                Triple("2026-10-15", "Control Systems Engineering (10:00 - 11:00 AM)", AttendanceStatus.PRESENT),
                Triple("2026-10-14", "Electrical Machines II (11:00 - 12:00 PM)", AttendanceStatus.PRESENT),
                Triple("2026-10-14", "Power Systems Analysis (09:00 - 10:00 AM)", AttendanceStatus.PRESENT),
                Triple("2026-10-12", "Microprocessors & Interfacing (02:00 - 03:00 PM)", AttendanceStatus.LATE),
                Triple("2026-10-11", "Electromagnetic Field Theory (10:00 - 11:00 AM)", AttendanceStatus.ABSENT)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sampleLog.forEach { (date, lecture, status) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = lecture,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Date: $date",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AttendanceStatusChip(status = status)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}
