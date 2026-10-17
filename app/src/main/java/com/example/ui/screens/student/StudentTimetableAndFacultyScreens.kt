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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.theme.CircuitCyan
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.VidyutBluePrimary
import com.example.viewmodel.AppViewModel

@Composable
fun StudentTimetableScreen(viewModel: AppViewModel) {
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Semester 5 Timetable (EEE - Section A)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        val hardcodedSlots = listOf(
            Triple("Monday", "09:00 - 10:00 AM", Pair("Power Systems Analysis (EE-501)", "Prof. Rajesh Sharma • LH-101")),
            Triple("Monday", "10:00 - 12:00 PM", Pair("Electrical Machines II Lab", "Prof. Rajesh Sharma • Machine Lab 1")),
            Triple("Tuesday", "09:00 - 10:00 AM", Pair("Control Systems Engineering (EE-503)", "Dr. Sunita Verma • LH-101")),
            Triple("Tuesday", "11:00 - 12:00 PM", Pair("Microprocessors & Interfacing (EE-504)", "Dr. Sunita Verma • LH-102")),
            Triple("Wednesday", "10:00 - 11:00 AM", Pair("Electromagnetic Fields (EE-505)", "Prof. Rajesh Sharma • LH-103")),
            Triple("Wednesday", "02:00 - 04:00 PM", Pair("Control Systems Simulation Lab", "Dr. Sunita Verma • Simulation Lab")),
            Triple("Thursday", "09:00 - 10:00 AM", Pair("Power Systems Analysis (EE-501)", "Prof. Rajesh Sharma • LH-101")),
            Triple("Thursday", "11:00 - 12:00 PM", Pair("Electrical Machines II (EE-502)", "Prof. Rajesh Sharma • LH-102")),
            Triple("Friday", "10:00 - 11:00 AM", Pair("Microprocessors Tutorial", "Dr. Sunita Verma • LH-101"))
        )

        daysOfWeek.forEach { day ->
            val daySlots = hardcodedSlots.filter { it.first == day }
            if (daySlots.isNotEmpty()) {
                item {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = VidyutBluePrimary
                    )
                }

                items(daySlots) { (_, time, info) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = VidyutBluePrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = time,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = VidyutBluePrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = info.first,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = info.second,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun StudentContactTeacherScreen(viewModel: AppViewModel) {
    var messageTargetFaculty by remember { mutableStateOf<String?>(null) }

    val facultyList = listOf(
        Pair("Prof. Rajesh Sharma", "Associate Professor & HOD • Power Systems (EE-501), Machines II (EE-502)"),
        Pair("Dr. Sunita Verma", "Assistant Professor • Control Systems (EE-503), Microprocessors (EE-504)")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Assigned Faculty Directory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Connect with your course instructors for academic doubts and lab clarifications.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(facultyList) { (name, details) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CircuitCyan.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupervisorAccount,
                                contentDescription = null,
                                tint = CircuitCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = details,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { messageTargetFaculty = name },
                            colors = ButtonDefaults.buttonColors(containerColor = CircuitCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("send_doubt_btn")
                        ) {
                            Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Academic Inquiry", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    messageTargetFaculty?.let { facultyName ->
        var queryText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { messageTargetFaculty = null },
            title = { Text("Inquiry for $facultyName") },
            text = {
                Column {
                    Text("Submit your doubt or question regarding lecture topics or lab assignments.")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        label = { Text("Your Question / Message") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        messageTargetFaculty = null
                    },
                    enabled = queryText.isNotBlank()
                ) {
                    Text("Send Message")
                }
            },
            dismissButton = {
                TextButton(onClick = { messageTargetFaculty = null }) { Text("Cancel") }
            }
        )
    }
}
