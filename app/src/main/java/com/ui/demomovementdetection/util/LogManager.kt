package com.ui.demomovementdetection.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.ui.demomovementdetection.sensor.SensorData
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class LogManager(private val context: Context) {
    private val logHandler = Handler(Looper.getMainLooper())
    private var isLogging = false

    private val logFile: File by lazy {
        File(context.filesDir, "walking_detector_log.txt").also {
            if (!it.exists()) {
                try {
                    it.createNewFile()
                    Log.d(TAG, "Log file created at ${it.absolutePath}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating log file: ${e.message}")
                }
            }
        }
        File(context.getExternalFilesDir(null), "walking_detector_log.txt")
    }

    private val logRunnable = object : Runnable {
        override fun run() {
            if (isLogging) {
                logData(SensorData(), false) // Log dummy data to test file writing
                logHandler.postDelayed(this, LOG_INTERVAL)
            }
        }
    }

    fun startLogging() {
        isLogging = true
        logHandler.post(logRunnable)
        Log.d(TAG, "Logging started")
    }

    fun stopLogging() {
        isLogging = false
        logHandler.removeCallbacks(logRunnable)
        Log.d(TAG, "Logging stopped")
    }

    fun saveLogFile(): Uri? {
        stopLogging()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "walking_log_$timestamp.txt"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/logs")
        }

        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                logFile.inputStream().use { input ->
                    input.copyTo(outputStream)
                }
            }
        }

        return uri
    }

    fun logData(sensorData: SensorData, isWalking: Boolean) {
        val logEntry = formatLogEntry(sensorData, isWalking)
        appendToLogFile(logEntry)
    }

    private fun formatLogEntry(sensorData: SensorData, isWalking: Boolean): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        val timestamp = sdf.format(Date())
        return "$timestamp,$isWalking," +
                "${sensorData.accelerometerData[0]},${sensorData.accelerometerData[1]},${sensorData.accelerometerData[2]}," +
                "${sensorData.gyroscopeData[0]},${sensorData.gyroscopeData[1]},${sensorData.gyroscopeData[2]}," +
                "${sensorData.linearAccelerationData[0]},${sensorData.linearAccelerationData[1]},${sensorData.linearAccelerationData[2]}," +
                "${sensorData.accelMagnitude},${sensorData.gyroMagnitude},${sensorData.linearAccelMagnitude}\n"
    }

    private fun appendToLogFile(logString: String) {
        try {
            FileWriter(logFile, true).use {
                it.append(logString)
                Log.d(TAG, "Data appended to log file")
            }
            FileWriter(logFile, true).use { it.append(logString) }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to log file: ${e.message}")
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "LogManager"
        private const val LOG_INTERVAL = 50L // 500ms for twice per second
    }
}

