package com.example.android_project_msd.groups.groupdetail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class GroupDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get group ID from intent
        val groupId = intent.getStringExtra("GROUP_ID") ?: ""

        setContent {
            MaterialTheme {
                Surface {
                    GroupDetailRoute(
                        groupId = groupId,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}