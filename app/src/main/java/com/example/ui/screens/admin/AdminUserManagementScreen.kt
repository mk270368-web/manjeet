package com.example.ui.screens.admin

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupervisorAccount
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountStatus
import com.example.data.model.StudentWithDetails
import com.example.data.model.TeacherWithDetails
import com.example.ui.common.StatusPill
import com.example.ui.theme.CircuitCyan
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.VidyutBluePrimary
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(viewModel: AppViewModel) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Students, 1: Teachers
    var searchQuery by remember { mutableStateOf("") }

    var showCreateStudentDialog by remember { mutableStateOf(false) }
    var showCreateTeacherDialog by remember { mutableStateOf(false) }
    var showCsvImportDialog by remember { mutableStateOf(false) }
    var resetPassTargetUser by remember { mutableStateOf<Pair<String, String>?>(null) } // (userId, loginId)

    val students by viewModel.allStudents.collectAsState()
    val teachers by viewModel.allTeachers.collectAsState()
    val classes by viewModel.allClasses.collectAsState()

    Scaffold(
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedSubTab == 0) {
                    OutlinedButton(
                        onClick = { showCsvImportDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.testTag("admin_import_csv_btn")
                    ) {
                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import CSV", fontSize = 12.sp)
                    }
                    ExtendedFloatingActionButton(
                        onClick = { showCreateStudentDialog = true },
                        containerColor = VidyutBluePrimary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("admin_add_student_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Student")
                    }
                } else {
                    ExtendedFloatingActionButton(
                        onClick = { showCreateTeacherDialog = true },
                        containerColor = CircuitCyan,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("admin_add_teacher_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Faculty")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs: Students vs Faculty
            SecondaryTabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("Students (${students.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("subtab_students")
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("Faculty / Teachers (${teachers.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("subtab_faculty")
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, roll number, or Login ID...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(10.dp)
            )

            // Content List
            if (selectedSubTab == 0) {
                val filtered = students.filter {
                    it.user.name.contains(searchQuery, ignoreCase = true) ||
                            it.student.rollNumber.contains(searchQuery, ignoreCase = true) ||
                            it.user.loginId.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered) { item ->
                        StudentAdminCard(
                            item = item,
                            onToggleStatus = { viewModel.toggleUserStatus(item.user.userId) },
                            onResetPassword = { resetPassTargetUser = Pair(item.user.userId, item.user.loginId) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            } else {
                val filtered = teachers.filter {
                    it.user.name.contains(searchQuery, ignoreCase = true) ||
                            it.teacher.employeeCode.contains(searchQuery, ignoreCase = true) ||
                            it.user.loginId.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered) { item ->
                        TeacherAdminCard(
                            item = item,
                            onToggleStatus = { viewModel.toggleUserStatus(item.user.userId) },
                            onResetPassword = { resetPassTargetUser = Pair(item.user.userId, item.user.loginId) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    // Dialog: Create Student
    if (showCreateStudentDialog) {
        CreateStudentDialog(
            classes = classes,
            onDismiss = { showCreateStudentDialog = false },
            onCreate = { name, email, phone, roll, classId, branch, sem, sec, tempPass ->
                viewModel.createStudent(name, email, phone, roll, classId, branch, sem, sec, tempPass)
                showCreateStudentDialog = false
            }
        )
    }

    // Dialog: Create Teacher
    if (showCreateTeacherDialog) {
        CreateTeacherDialog(
            onDismiss = { showCreateTeacherDialog = false },
            onCreate = { name, email, phone, empCode, dept, desig, qual, tempPass ->
                viewModel.createTeacher(name, email, phone, empCode, dept, desig, qual, tempPass)
                showCreateTeacherDialog = false
            }
        )
    }

    // Dialog: CSV Import
    if (showCsvImportDialog) {
        CsvImportDialog(
            classes = classes,
            onDismiss = { showCsvImportDialog = false },
            onImport = { csv, classId ->
                viewModel.importStudentsCsv(csv, classId)
                showCsvImportDialog = false
            }
        )
    }

    // Dialog: Reset Password
    resetPassTargetUser?.let { (userId, loginId) ->
        var newPassInput by remember { mutableStateOf("temp${(1000..9999).random()}") }
        AlertDialog(
            onDismissRequest = { resetPassTargetUser = null },
            title = { Text("Reset User Password") },
            text = {
                Column {
                    Text("Assign a temporary password for user $loginId. The user will be required to change it upon first login.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPassInput,
                        onValueChange = { newPassInput = it },
                        label = { Text("Temporary Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.adminResetPassword(userId, newPassInput)
                    resetPassTargetUser = null
                }) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { resetPassTargetUser = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StudentAdminCard(
    item: StudentWithDetails,
    onToggleStatus: () -> Unit,
    onResetPassword: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(VidyutBluePrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.user.name.take(1),
                            fontWeight = FontWeight.Bold,
                            color = VidyutBluePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Roll: ${item.student.rollNumber} | ID: ${item.user.loginId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusPill(status = item.user.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Class: ${item.classEntity?.name ?: item.student.classId} (${item.student.section}) • Sem ${item.student.semester}",
                style = MaterialTheme.typography.bodySmall,
                color = VidyutBluePrimary,
                fontWeight = FontWeight.Medium
            )

            if (item.user.tempPassword != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Temp Pass: ${item.user.tempPassword}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD97706),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onResetPassword) {
                    Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Pass", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(
                    onClick = onToggleStatus,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (item.user.status == AccountStatus.ACTIVE) StatusAbsentRed else StatusPresentGreen
                    )
                ) {
                    Text(
                        text = if (item.user.status == AccountStatus.ACTIVE) "Deactivate" else "Activate",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TeacherAdminCard(
    item: TeacherWithDetails,
    onToggleStatus: () -> Unit,
    onResetPassword: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CircuitCyan.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.user.name.take(1),
                            fontWeight = FontWeight.Bold,
                            color = CircuitCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Code: ${item.teacher.employeeCode} | ID: ${item.user.loginId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusPill(status = item.user.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${item.teacher.designation} • ${item.teacher.department}",
                style = MaterialTheme.typography.bodySmall,
                color = CircuitCyan,
                fontWeight = FontWeight.Medium
            )

            if (item.assignedSubjectNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Subjects: ${item.assignedSubjectNames.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.user.tempPassword != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Temp Pass: ${item.user.tempPassword}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD97706),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onResetPassword) {
                    Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Pass", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(
                    onClick = onToggleStatus,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (item.user.status == AccountStatus.ACTIVE) StatusAbsentRed else StatusPresentGreen
                    )
                ) {
                    Text(
                        text = if (item.user.status == AccountStatus.ACTIVE) "Deactivate" else "Activate",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStudentDialog(
    classes: List<com.example.data.model.ClassEntity>,
    onDismiss: () -> Unit,
    onCreate: (name: String, email: String, phone: String, roll: String, classId: String, branch: String, sem: Int, sec: String, tempPass: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var tempPassword by remember { mutableStateOf("pass123") }

    var selectedClass by remember { mutableStateOf(classes.firstOrNull()) }
    var classExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Student Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Login ID will be automatically generated by the system (e.g. STU2026xxx).", style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rollNumber,
                    onValueChange = { rollNumber = it },
                    label = { Text("Roll Number * (e.g. EE-2024-005)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Class Dropdown
                ExposedDropdownMenuBox(
                    expanded = classExpanded,
                    onExpandedChange = { classExpanded = !classExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedClass?.let { "${it.name} (${it.section})" } ?: "Select Class",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign Class *") },
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
                                text = { Text("${c.name} - Section ${c.section} (Sem ${c.semester})") },
                                onClick = {
                                    selectedClass = c
                                    classExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tempPassword,
                    onValueChange = { tempPassword = it },
                    label = { Text("Temporary Password *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val c = selectedClass ?: return@Button
                    if (name.isNotBlank() && rollNumber.isNotBlank()) {
                        onCreate(
                            name,
                            if (email.isBlank()) "${rollNumber.lowercase()}@gk.edu.in" else email,
                            phone,
                            rollNumber,
                            c.classId,
                            c.branch,
                            c.semester,
                            c.section,
                            tempPassword
                        )
                    }
                },
                enabled = name.isNotBlank() && rollNumber.isNotBlank() && selectedClass != null
            ) {
                Text("Create Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateTeacherDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, email: String, phone: String, empCode: String, dept: String, desig: String, qual: String, tempPass: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var empCode by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Electrical Engineering") }
    var designation by remember { mutableStateOf("Assistant Professor") }
    var qualification by remember { mutableStateOf("M.Tech, Ph.D") }
    var tempPassword by remember { mutableStateOf("pass123") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Faculty Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Login ID will be generated by the institute system (e.g. TCH103).", style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Faculty Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = empCode,
                    onValueChange = { empCode = it },
                    label = { Text("Employee Code * (e.g. EMP-EE-103)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Designation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tempPassword,
                    onValueChange = { tempPassword = it },
                    label = { Text("Temporary Password *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && empCode.isNotBlank()) {
                        onCreate(
                            name,
                            if (email.isBlank()) "${empCode.lowercase()}@gk.edu.in" else email,
                            phone,
                            empCode,
                            department,
                            designation,
                            qualification,
                            tempPassword
                        )
                    }
                },
                enabled = name.isNotBlank() && empCode.isNotBlank()
            ) {
                Text("Create Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvImportDialog(
    classes: List<com.example.data.model.ClassEntity>,
    onDismiss: () -> Unit,
    onImport: (csvContent: String, classId: String) -> Unit
) {
    var csvText by remember {
        mutableStateOf(
            "Name,RollNumber,Email,Phone\n" +
                    "Vikram Singh,EE-2024-005,vikram.s@gk.edu.in,+91 99000 55555\n" +
                    "Meera Joshi,EE-2024-006,meera.j@gk.edu.in,+91 99000 66666\n" +
                    "Karan Malhotra,EE-2024-007,karan.m@gk.edu.in,+91 99000 77777"
        )
    }
    var selectedClass by remember { mutableStateOf(classes.firstOrNull()) }
    var classExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Students from CSV") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Paste comma-separated rows. Columns: Name, RollNumber, Email, Phone. Login IDs and temporary passwords will be automatically provisioned.",
                    style = MaterialTheme.typography.bodySmall
                )

                ExposedDropdownMenuBox(
                    expanded = classExpanded,
                    onExpandedChange = { classExpanded = !classExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedClass?.let { "${it.name} (${it.section})" } ?: "Select Class",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign to Class") },
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

                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    label = { Text("CSV Records") },
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val c = selectedClass ?: return@Button
                    if (csvText.isNotBlank()) onImport(csvText, c.classId)
                },
                enabled = selectedClass != null && csvText.isNotBlank()
            ) {
                Text("Import Records")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
