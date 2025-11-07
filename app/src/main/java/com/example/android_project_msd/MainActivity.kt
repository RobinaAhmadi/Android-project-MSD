package com.example.android_project_msd

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_project_msd.frontpage.FrontPage
import com.example.android_project_msd.createprofile.CreateProfileActivity
import com.example.android_project_msd.groups.grouplist.GroupsRoute
import com.example.android_project_msd.R
import com.example.android_project_msd.login.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showLogin by remember { mutableStateOf(false) }

            MaterialTheme {
                if (showLogin) {
                    // Show LoginScreen after Get Started is clicked
                    LoginScreen(
                        onCreateAccountClick = {
                            // When "Create account" clicked → open CreateProfileActivity
                            startActivity(Intent(this, CreateProfileActivity::class.java))
                        }
                    )
                } else {
                    // Show front page first
                    FrontPage(onGetStarted = { showLogin = true })
                }
            }
        }
    }
}

 
