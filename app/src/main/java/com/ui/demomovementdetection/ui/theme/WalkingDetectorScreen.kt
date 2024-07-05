package com.ui.demomovementdetection.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ui.demomovementdetection.R
import com.ui.demomovementdetection.sensor.WalkingDetector

@Composable
fun WalkingDetectorScreen(paddingValues: PaddingValues) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val walkingDetector = remember { WalkingDetector(context) }
    val sensorData by walkingDetector.sensorData.collectAsState()
    val isWalking by walkingDetector.isWalking.collectAsState()
    var isLogging by remember { mutableStateOf(false) }
    var savedFilePath by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(Unit) {
        walkingDetector.startListening()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                walkingDetector.startListening()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                walkingDetector.stopListening()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                SensorDataCard(
                    "Accelerometer",
                    sensorData.accelerometerData,
                    sensorData.accelMagnitude
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                SensorDataCard("Gyroscope", sensorData.gyroscopeData, sensorData.gyroMagnitude)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                SensorDataCard(
                    "Linear Acceleration",
                    sensorData.linearAccelerationData,
                    sensorData.linearAccelMagnitude
                )
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                WalkingStatusCard(isWalking)
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                LoggingControl(walkingDetector)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

        }
    }
}

@Composable
fun WalkingStatusCard(isWalking: Boolean) {
    val scale by animateFloatAsState(if (isWalking) 1.1f else 1f, label = "")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isWalking) "Walking" else "Standing Still",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            AnimatedVisibility(
                visible = isWalking,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AnimatedPreloader(modifier = Modifier.size(36.dp))
            }
        }
    }
}

@Composable
fun SensorDataCard(sensorName: String, data: FloatArray, magnitude: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = sensorName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    SensorValue("X", data[0])
                    SensorValue("Y", data[1])
                    SensorValue("Z", data[2])
                }
                SensorValue("Magnitude", magnitude)
            }
        }
    }
}

@Composable
fun SensorValue(label: String, value: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "%.2f".format(value),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AnimatedPreloader(modifier: Modifier = Modifier) {
    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.walking
        )
    )

    val preloaderProgress by animateLottieCompositionAsState(
        preloaderLottieComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )


    LottieAnimation(
        composition = preloaderLottieComposition,
        progress = preloaderProgress,
        modifier = modifier
    )
}

@Composable
fun LoggingControl(walkingDetector: WalkingDetector) {
    var isLogging by remember { mutableStateOf(false) }
    var savedFilePath by remember { mutableStateOf<String?>(null) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(
            onClick = {
                if (isLogging) {
                    val savedFile = walkingDetector.stopLogging()
                    savedFilePath = savedFile.absolutePath
                    isLogging = false
                } else {
                    walkingDetector.startLogging()
                    isLogging = true
                    savedFilePath = null
                }
            }
        ) {
            Text(if (isLogging) "Save Logging" else "Start Logging")
        }

        Spacer(modifier = Modifier.width(8.dp))

        savedFilePath?.let { path ->
            Text("Log saved to: $path", style = MaterialTheme.typography.bodySmall)
        }
    }
}
