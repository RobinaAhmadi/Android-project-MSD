package com.example.android_project_msd.groups.creategroup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class CreateGroupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    CreateGroupFullRoute(
                        onDone = { finish() },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }
}