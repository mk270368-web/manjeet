package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountStatus
import com.example.data.model.AttendanceStatus
import com.example.data.model.SubmissionStatus
import com.example.data.model.UserRole
import com.example.ui.theme.CircuitCyan
import com.example.ui.theme.CircuitCyanContainer
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.ElectricGoldContainer
import com.example.ui.theme.OnCircuitCyanContainer
import com.example.ui.theme.OnElectricGoldContainer
import com.example.ui.theme.OnVidyutBlueContainer
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusAbsentRedBg
import com.example.ui.theme.StatusLateAmber
import com.example.ui.theme.StatusLateAmberBg
import com.example.ui.theme.StatusLeavePurple
import com.example.ui.theme.StatusLeavePurpleBg
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.StatusPresentGreenBg
import com.example.ui.theme.VidyutBlueContainer
import com.example.ui.theme.VidyutBluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VidyutAppBar(
    title: String,
    subtitle: String? = null,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    unreadCount: Int = 0,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = VidyutBluePrimary,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBackClick, modifier = Modifier.testTag("appbar_back_btn")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElectricGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Vidyut Logo",
                        tint = VidyutBluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Column(modifier = Modifier.padding(start = if (showBack) 0.dp else 8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricGold,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        },
        actions = {
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge(containerColor = ElectricGold, contentColor = Color.Black) {
                            Text(text = if (unreadCount > 9) "9+" else unreadCount.toString())
                        }
                    }
                },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("appbar_notifications_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White
                    )
                }
            }

            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.testTag("appbar_profile_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile and Security",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onLogoutClick,
                modifier = Modifier.testTag("appbar_logout_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color(0xFFFFB4AB)
                )
            }
        }
    )
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color = VidyutBluePrimary,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AttendanceStatusChip(status: AttendanceStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        AttendanceStatus.PRESENT -> Triple(StatusPresentGreenBg, StatusPresentGreen, "Present")
        AttendanceStatus.ABSENT -> Triple(StatusAbsentRedBg, StatusAbsentRed, "Absent")
        AttendanceStatus.LATE -> Triple(StatusLateAmberBg, StatusLateAmber, "Late")
        AttendanceStatus.ON_LEAVE -> Triple(StatusLeavePurpleBg, StatusLeavePurple, "On Leave")
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun SubmissionStatusChip(status: SubmissionStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        SubmissionStatus.PENDING -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), "Not Submitted")
        SubmissionStatus.SUBMITTED -> Triple(CircuitCyanContainer, OnCircuitCyanContainer, "Submitted")
        SubmissionStatus.LATE -> Triple(StatusLateAmberBg, StatusLateAmber, "Submitted Late")
        SubmissionStatus.EVALUATED -> Triple(StatusPresentGreenBg, StatusPresentGreen, "Graded")
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun RoleBadge(role: UserRole, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (role) {
        UserRole.ADMIN -> Pair(VidyutBlueContainer, OnVidyutBlueContainer)
        UserRole.TEACHER -> Pair(CircuitCyanContainer, OnCircuitCyanContainer)
        UserRole.STUDENT -> Pair(ElectricGoldContainer, OnElectricGoldContainer)
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = role.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun StatusPill(status: AccountStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        AccountStatus.ACTIVE -> Triple(StatusPresentGreenBg, StatusPresentGreen, "Active")
        AccountStatus.INACTIVE -> Triple(StatusAbsentRedBg, StatusAbsentRed, "Deactivated")
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
