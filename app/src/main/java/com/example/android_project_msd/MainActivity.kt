package com.example.android_project_msd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android_project_msd.createprofile.CreateProfileRoute
import com.example.android_project_msd.frontpage.FrontPage
import com.example.android_project_msd.groups.groupdetail.GroupDetailRoute
import com.example.android_project_msd.groups.grouplist.GroupsRoute
import com.example.android_project_msd.login.LoginScreen
import com.example.android_project_msd.navigation.Routes
import com.example.android_project_msd.profile.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            MaterialTheme {
                NavHost(
                    navController = navController,
                    startDestination = Routes.FrontPage
                ) {
                    composable(Routes.FrontPage) {
                        FrontPage(
                            onGetStarted = { navController.navigate(Routes.Login) }
                        )
                    }

                    composable(Routes.Login) {
                        LoginScreen(
                            onCreateAccountClick = { navController.navigate(Routes.CreateProfile) },
                            onSignIn = { navController.navigate(Routes.Groups) }
                        )
                    }

                    composable(Routes.CreateProfile) {
                        CreateProfileRoute(onDone = { navController.popBackStack() })
                    }

                    composable(Routes.Groups) {
                        GroupsRoute(
                            onBack = { navController.popBackStack() },
                            onOpenGroup = { id -> navController.navigate(Routes.groupDetail(id)) }
                        )
                    }

                    composable(
                        route = Routes.GroupDetail,
                        arguments = listOf(navArgument(Routes.GroupDetailArg) { type = NavType.StringType })
                    ) { backStackEntry ->
                        val groupId = backStackEntry.arguments?.getString(Routes.GroupDetailArg).orEmpty()
                        GroupDetailRoute(
                            groupId = groupId,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.Profile) {
                        ProfileScreen()
                    }
                }
            }
        }
    }
}

