package com.kavyaraj.tillflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kavyaraj.tillflow.ui.navigation.TillFlowNavHost
import com.kavyaraj.tillflow.ui.theme.TillFlowTheme
import dagger.hilt.android.AndroidEntryPoint

/** Single-activity app: every screen is a composable inside [TillFlowNavHost]. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TillFlowTheme {
                TillFlowNavHost()
            }
        }
    }
}
