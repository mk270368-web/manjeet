package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.AppScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WelcomeRoleScreen
import com.example.ui.screens.admin.AdminRootScreen
import com.example.ui.screens.common.AnnouncementsScreen
import com.example.ui.screens.common.NotificationsScreen
import com.example.ui.screens.common.ProfilePasswordScreen
import com.example.ui.screens.student.StudentRootScreen
import com.example.ui.screens.teacher.TeacherRootScreen
import com.example.ui.theme.VidyutGyanTheme
import com.example.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VidyutGyanTheme {
                val appViewModel: AppViewModel = viewModel()
                VidyutGyanApp(viewModel = appViewModel)
            }
        }
    }
}

@Composable
fun VidyutGyanApp(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackBarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // System BackHandler
    BackHandler(enabled = currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.WELCOME_ROLES) {
        if (!viewModel.navigateBack()) {
            // If already at root of a role portal, navigate to Welcome Role screen
            viewModel.navigateTo(AppScreen.WELCOME_ROLES)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.SPLASH -> SplashScreen(viewModel)
                AppScreen.WELCOME_ROLES -> WelcomeRoleScreen(viewModel)
                AppScreen.LOGIN -> LoginScreen(viewModel)
                AppScreen.ADMIN_ROOT -> AdminRootScreen(viewModel)
                AppScreen.TEACHER_ROOT -> TeacherRootScreen(viewModel)
                AppScreen.STUDENT_ROOT -> StudentRootScreen(viewModel)
                AppScreen.NOTIFICATIONS -> NotificationsScreen(viewModel)
                AppScreen.ANNOUNCEMENTS -> AnnouncementsScreen(viewModel)
                AppScreen.PROFILE_PASSWORD -> ProfilePasswordScreen(viewModel)
            }
        }
    }
}
