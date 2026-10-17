package com.example.ui.screens.teacher

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssignmentWithDetails
import com.example.data.model.ClassEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.SubmissionStatus
import com.example.data.model.SubmissionWithStudent
import com.example.ui.common.SubmissionStatusChip
import com.example.ui.theme.CircuitCyan
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.VidyutBluePrimary
import com.example.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TeacherAssignmentScreen(viewModel: AppViewModel) {
    val teacherAssignments by viewModel.teacherAssignmentsList.collectAsState()
    val submissions by viewModel.teacherSubmissionsList.collectAsState()
    val classes by viewModel.allClasses.collectAsState()
    val subjects by viewModel.allSubjects.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedAssignmentForSubmissions by remember { mutableStateOf<AssignmentWithDetails?>(null) }
    var evaluateTargetSubmission by remember { mutableStateOf<SubmissionWithStudent?>(null) }

    Scaffold(
        floatingActionButton = {
            if (selectedAssignmentForSubmissions == null) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = VidyutBluePrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("teacher_create_assignment_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publish Assignment")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (selectedAssignmentForSubmissions == null) {
                // Assignments List View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Published Class Assignments (${teacherAssignments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(teacherAssignments) { asg ->
                        TeacherAssignmentCard(
                            asg = asg,
                            onClick = {
                                selectedAssignmentForSubmissions = asg
                                viewModel.loadSubmissionsForAssignment(asg.assignment.assignmentId)
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            } else {
                // Submissions View for Selected Assignment
                val selectedAsg = selectedAssignmentForSubmissions!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { selectedAssignmentForSubmissions = null },
                            modifier = Modifier.testTag("back_to_assignments_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = selectedAsg.assignment.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${selectedAsg.subjectCode} • ${selectedAsg.className} • Max Marks: ${selectedAsg.assignment.maxMarks}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Student Submissions (${submissions.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(submissions) { item ->
                            SubmissionReviewCard(
                                item = item,
                                maxMarks = selectedAsg.assignment.maxMarks,
                                onEvaluate = { evaluateTargetSubmission = item }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }

    // Dialog: Create Assignment
    if (showCreateDialog) {
        CreateAssignmentDialog(
            classes = classes,
            subjects = subjects,
            onDismiss = { showCreateDialog = false },
            onCreate = { title, desc, subId, clsId, deadline, marks, instructions, attName ->
                viewModel.createAssignment(title, desc, subId, clsId, deadline, marks, instructions, attName)
                showCreateDialog = false
            }
        )
    }

    // Dialog: Evaluate Submission
    evaluateTargetSubmission?.let { item ->
        var marksInput by remember { mutableStateOf(item.submission.marksObtained?.toString() ?: "45") }
        var feedbackInput by remember { mutableStateOf(item.submission.feedback ?: "Good analytical approach and step-by-step solution.") }

        AlertDialog(
            onDismissRequest = { evaluateTargetSubmission = null },
            title = { Text("Grade & Evaluate Submission", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Student: ${item.user.name} (Roll: ${item.student.rollNumber})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Attached File: ${item.submission.fileName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CircuitCyan
                    )

                    OutlinedTextField(
                        value = marksInput,
                        onValueChange = { marksInput = it },
                        label = { Text("Marks Obtained (Max: ${selectedAssignmentForSubmissions?.assignment?.maxMarks ?: 50})") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = feedbackInput,
                        onValueChange = { feedbackInput = it },
                        label = { Text("Teacher Feedback / Comments") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val marks = marksInput.toIntOrNull() ?: 0
                        viewModel.evaluateSubmission(item.submission.submissionId, marks, feedbackInput)
                        evaluateTargetSubmission = null
                    }
                ) {
                    Text("Publish Marks")
                }
            },
            dismissButton = {
                TextButton(onClick = { evaluateTargetSubmission = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TeacherAssignmentCard(
    asg: AssignmentWithDetails,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

                Text(
                    text = "Deadline: ${asg.assignment.deadline}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFB45309),
                    fontWeight = FontWeight.Bold
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Submissions & Evaluations stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Submissions: ${asg.submissionCount} | Graded: ${asg.evaluatedCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CircuitCyan,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Max Marks: ${asg.assignment.maxMarks}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SubmissionReviewCard(
    item: SubmissionWithStudent,
    maxMarks: Int,
    onEvaluate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Column {
                    Text(
                        text = item.user.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Roll: ${item.student.rollNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SubmissionStatusChip(status = item.submission.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp), tint = CircuitCyan)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.submission.fileName,
                    style = MaterialTheme.typography.bodySmall,
                    color = CircuitCyan,
                    fontWeight = FontWeight.Medium
                )
            }

            if (item.submission.comments.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${item.submission.comments}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            if (item.submission.status == SubmissionStatus.EVALUATED) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Score: ${item.submission.marksObtained ?: 0} / $maxMarks",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = StatusPresentGreen
                    )
                    if (item.submission.feedback != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${item.submission.feedback})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onEvaluate,
                    colors = ButtonDefaults.buttonColors(containerColor = CircuitCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (item.submission.status == SubmissionStatus.EVALUATED) "Re-Grade" else "Evaluate & Grade", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssignmentDialog(
    classes: List<ClassEntity>,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, subId: String, clsId: String, deadline: String, marks: Int, instructions: String, attName: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf(classes.firstOrNull()) }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()) }
    var deadline by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() + 604800000L))) }
    var maxMarks by remember { mutableIntStateOf(50) }
    var instructions by remember { mutableStateOf("Submit solution step-by-step in PDF format.") }
    var attachmentName by remember { mutableStateOf("Problem_Sheet_EE501.pdf") }

    var classExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish New Assignment", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Assignment Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Class Selector
                ExposedDropdownMenuBox(
                    expanded = classExpanded,
                    onExpandedChange = { classExpanded = !classExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedClass?.let { "${it.name} (${it.section})" } ?: "Select Class",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Class") },
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
                                    selectedClass = c
                                    classExpanded = false
                                }
                            )
                        }
                    }
                }

                // Subject Selector
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedSubject?.name ?: "Select Subject",
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
                                    selectedSubject = s
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Deadline (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Problem Statement") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = attachmentName,
                    onValueChange = { attachmentName = it },
                    label = { Text("Attached Learning Material Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val c = selectedClass ?: return@Button
                    val s = selectedSubject ?: return@Button
                    if (title.isNotBlank()) {
                        onCreate(title, description, s.subjectId, c.classId, deadline, maxMarks, instructions, attachmentName)
                    }
                },
                enabled = title.isNotBlank() && selectedClass != null && selectedSubject != null
            ) {
                Text("Publish Assignment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
