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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssignmentWithDetails
import com.example.data.model.SubmissionStatus
import com.example.ui.common.SubmissionStatusChip
import com.example.ui.theme.CircuitCyan
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.StatusPresentGreenBg
import com.example.ui.theme.VidyutBluePrimary
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAssignmentScreen(viewModel: AppViewModel) {
    val assignments by viewModel.studentAssignments.collectAsState()
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Pending, 1: Submitted & Evaluated
    var targetAssignmentForSubmission by remember { mutableStateOf<AssignmentWithDetails?>(null) }

    val pendingList = assignments.filter { it.currentStudentSubmission == null }
    val submittedList = assignments.filter { it.currentStudentSubmission != null }

    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Active & Due (${pendingList.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Submitted & Graded (${submittedList.size})", fontWeight = FontWeight.Bold) }
            )
        }

        val displayList = if (selectedSubTab == 0) pendingList else submittedList

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (displayList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedSubTab == 0) "No pending assignments due! Great work." else "No submissions recorded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(displayList) { asg ->
                StudentAssignmentCard(
                    asg = asg,
                    onSubmitClick = { targetAssignmentForSubmission = asg }
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Submit Assignment Dialog
    targetAssignmentForSubmission?.let { asg ->
        var fileNameInput by remember { mutableStateOf("EE501_Solution_Aarav.pdf") }
        var commentsInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { targetAssignmentForSubmission = null },
            title = { Text("Submit Assignment Solution", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = asg.assignment.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Due: ${asg.assignment.deadline} | Max Marks: ${asg.assignment.maxMarks}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB45309)
                    )

                    OutlinedTextField(
                        value = fileNameInput,
                        onValueChange = { fileNameInput = it },
                        label = { Text("Solution File Name (PDF / Doc)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = commentsInput,
                        onValueChange = { commentsInput = it },
                        label = { Text("Student Comments (Optional)") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fileNameInput.isNotBlank()) {
                            viewModel.submitStudentAssignment(
                                asg.assignment.assignmentId,
                                fileNameInput,
                                commentsInput
                            )
                            targetAssignmentForSubmission = null
                        }
                    },
                    enabled = fileNameInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = VidyutBluePrimary)
                ) {
                    Text("Confirm Upload")
                }
            },
            dismissButton = {
                TextButton(onClick = { targetAssignmentForSubmission = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun StudentAssignmentCard(
    asg: AssignmentWithDetails,
    onSubmitClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = VidyutBluePrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = asg.subjectCode,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VidyutBluePrimary
                    )
                }

                SubmissionStatusChip(
                    status = asg.currentStudentSubmission?.status ?: SubmissionStatus.PENDING
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = asg.assignment.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = asg.assignment.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (asg.assignment.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Instructions: ${asg.assignment.instructions}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (asg.assignment.attachmentName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = asg.assignment.attachmentName,
                        style = MaterialTheme.typography.labelSmall,
                        color = CircuitCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Evaluation / Marks section if graded
            if (asg.currentStudentSubmission?.status == SubmissionStatus.EVALUATED) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusPresentGreenBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grade Released",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusPresentGreen
                            )
                            Text(
                                text = "${asg.currentStudentSubmission.marksObtained ?: 0} / ${asg.assignment.maxMarks}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusPresentGreen
                            )
                        }
                        if (asg.currentStudentSubmission.feedback != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Feedback: ${asg.currentStudentSubmission.feedback}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF14532D)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Deadline: ${asg.assignment.deadline}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB45309),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Faculty: ${asg.teacherName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (asg.currentStudentSubmission == null) {
                    Button(
                        onClick = onSubmitClick,
                        colors = ButtonDefaults.buttonColors(containerColor = VidyutBluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("submit_assignment_btn_${asg.assignment.assignmentId}")
                    ) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Submit PDF", fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onSubmitClick,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Re-Submit", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
