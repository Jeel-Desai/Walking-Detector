package com.ui.demomovementdetection

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.core.view.WindowCompat
import com.ui.demomovementdetection.sensor.WalkingDetector
import com.ui.demomovementdetection.ui.theme.DemoMovementDetectionTheme
import com.ui.demomovementdetection.ui.theme.WalkingDetectorScreen

class MainActivity : ComponentActivity() {
    private val walkingDetector by lazy { WalkingDetector(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            enableEdgeToEdge()
            DemoMovementDetectionTheme {
                Scaffold { paddingValues ->
                    WalkingDetectorScreen(paddingValues)
                }
            }
        }
        startWalkingDetector()
    }

    private fun startWalkingDetector() {
        Log.d("MainActivity", "Starting WalkingDetector")
        walkingDetector.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "Stopping WalkingDetector")
        walkingDetector.stopListening()
    }

}
