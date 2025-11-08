package com.example.android_project_msd.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android_project_msd.view.createprofile.CreateProfileRoute
import com.example.android_project_msd.view.frontpage.FrontPage
import com.example.android_project_msd.view.groups.groupdetail.GroupDetailRoute
import com.example.android_project_msd.view.groups.grouplist.GroupsRoute
import com.example.android_project_msd.view.groups.creategroup.CreateGroupFullRoute
import com.example.android_project_msd.view.home.HomeScreen
import com.example.android_project_msd.view.login.LoginScreen
import com.example.android_project_msd.view.navigation.Routes
import com.example.android_project_msd.view.profile.ProfileScreen


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
                            onSignIn = {
                                navController.navigate(Routes.Home) {
                                    popUpTo(Routes.FrontPage) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Routes.CreateProfile) {
                        CreateProfileRoute(
                            onDone = {
                                navController.popBackStack()
                                navController.navigate(Routes.Groups)
                            }
                        )
                    }

                    composable(Routes.Groups) {
                        GroupsRoute(
                            onBack = { navController.popBackStack() },
                            onOpenGroup = { id -> navController.navigate(Routes.groupDetail(id)) }
                        )
                    }

                    composable(Routes.Home) {
                        HomeScreen(
                            onProfile = { navController.navigate(Routes.Profile) },
                            onCreateGroup = { navController.navigate(Routes.CreateGroup) },
                            onMyGroups = { navController.navigate(Routes.Groups) }
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

                    composable(Routes.Profile) { ProfileScreen() }

                    composable(Routes.CreateGroup) {
                        CreateGroupFullRoute(
                            onDone = {
                                navController.popBackStack()
                                navController.navigate(Routes.Groups)
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

