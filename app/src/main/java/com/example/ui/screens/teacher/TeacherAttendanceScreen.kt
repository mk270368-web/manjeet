package com.example.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.AttendanceStudentItem
import com.example.ui.common.AttendanceStatusChip
import com.example.ui.theme.CircuitCyan
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusLateAmber
import com.example.ui.theme.StatusLeavePurple
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.VidyutBluePrimary
import com.example.viewmodel.AppViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceScreen(viewModel: AppViewModel) {
    val classes by viewModel.allClasses.collectAsState()
    val subjects by viewModel.allSubjects.collectAsState()
    val studentList by viewModel.attendanceStudentList.collectAsState()
    val isSaving by viewModel.isAttendanceSaving.collectAsState()

    val selectedClassId by viewModel.selectedClassForAttendance.collectAsState()
    val selectedSubjectId by viewModel.selectedSubjectForAttendance.collectAsState()
    val dateInput by viewModel.attendanceDateInput.collectAsState()
    val slotInput by viewModel.attendanceSlotInput.collectAsState()

    var classExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }

    val currentClass = classes.find { it.classId == selectedClassId }
    val currentSubject = subjects.find { it.subjectId == selectedSubjectId }

    val presentCount = studentList.count { it.status == AttendanceStatus.PRESENT }
    val absentCount = studentList.count { it.status == AttendanceStatus.ABSENT }
    val lateCount = studentList.count { it.status == AttendanceStatus.LATE }
    val leaveCount = studentList.count { it.status == AttendanceStatus.ON_LEAVE }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Class & Subject Selectors Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Class selector
            ExposedDropdownMenuBox(
                expanded = classExpanded,
                onExpandedChange = { classExpanded = !classExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = currentClass?.let { "${it.name} (${it.section})" } ?: "Select Class",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Class") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = classExpanded,
                    onDismissRequest = { classExpanded = false }
                ) {
                    classes.forEach { c ->
                        DropdownMenuItem(
                            text = { Text("${c.name} (${c.section})") },
                            onClick = {
                                viewModel.loadStudentsForAttendance(c.classId)
                                classExpanded = false
                            }
                        )
                    }
                }
            }

            // Subject selector
            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = !subjectExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = currentSubject?.code ?: "Select Subject",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Subject") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = subjectExpanded,
                    onDismissRequest = { subjectExpanded = false }
                ) {
                    subjects.forEach { s ->
                        DropdownMenuItem(
                            text = { Text("${s.code}: ${s.name}") },
                            onClick = {
                                viewModel.selectedSubjectForAttendance.value = s.subjectId
                                subjectExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Date and Slot Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = dateInput,
                onValueChange = { viewModel.attendanceDateInput.value = it },
                label = { Text("Date (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = slotInput,
                onValueChange = { viewModel.attendanceSlotInput.value = it },
                label = { Text("Lecture Slot") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Duplicate prevention & Stats summary bar
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("P: $presentCount", color = StatusPresentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("A: $absentCount", color = StatusAbsentRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("L: $lateCount", color = StatusLateAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("OL: $leaveCount", color = StatusLeavePurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.markAllAttendancePresent() },
                    modifier = Modifier.testTag("teacher_mark_all_present_btn")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark All Present", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Student Roster
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(studentList) { student ->
                AttendanceStudentRow(
                    student = student,
                    onStatusChange = { newStatus ->
                        viewModel.updateStudentAttendanceStatus(student.studentId, newStatus)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save Attendance Button
        Button(
            onClick = { viewModel.saveAttendanceSession() },
            enabled = !isSaving && studentList.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("teacher_save_attendance_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = CircuitCyan, contentColor = Color.White),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Attendance Session", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AttendanceStudentRow(
    student: AttendanceStudentItem,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Roll: ${student.rollNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status Toggle Buttons: P, A, L, OL
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusToggleButton(
                    label = "P",
                    color = StatusPresentGreen,
                    isSelected = student.status == AttendanceStatus.PRESENT,
                    onClick = { onStatusChange(AttendanceStatus.PRESENT) }
                )
                StatusToggleButton(
                    label = "A",
                    color = StatusAbsentRed,
                    isSelected = student.status == AttendanceStatus.ABSENT,
                    onClick = { onStatusChange(AttendanceStatus.ABSENT) }
                )
                StatusToggleButton(
                    label = "L",
                    color = StatusLateAmber,
                    isSelected = student.status == AttendanceStatus.LATE,
                    onClick = { onStatusChange(AttendanceStatus.LATE) }
                )
                StatusToggleButton(
                    label = "OL",
                    color = StatusLeavePurple,
                    isSelected = student.status == AttendanceStatus.ON_LEAVE,
                    onClick = { onStatusChange(AttendanceStatus.ON_LEAVE) }
                )
            }
        }
    }
}

@Composable
fun StatusToggleButton(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color else color.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (isSelected) Color.White else color
        )
    }
}
