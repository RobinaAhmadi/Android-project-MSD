package com.example.android_project_msd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
// Importer din nye skærm
import com.example.android_project_msd.profile.ProfileScreen
// Du skal måske også importere dit tema
import com.example.android_project_msd.ui.theme.AndroidprojectmsdTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Sæt dit tema
            AndroidprojectmsdTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Kald din nye ProfileScreen Composable her!
                    ProfileScreen()
                }
            }
        }
    }
}