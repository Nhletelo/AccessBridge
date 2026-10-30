package com.example.accessbridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.accessbridge.ui.navigation.AppNavigation
import com.example.accessbridge.ui.theme.AccessBridgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccessBridgeTheme {
                AppNavigation()
            }
        }
    }
}
