package com.ui.demomovementdetection.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.ui.demomovementdetection.util.LogManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt

class WalkingDetector(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData.asStateFlow()

    private val _isWalking = MutableStateFlow(false)
    val isWalking: StateFlow<Boolean> = _isWalking.asStateFlow()

    private var lastAccelValues = FloatArray(3)
    private var lastLinearAccelValues = FloatArray(3)
    private var stepCount = 0
    private var lastStepTime = 0L
    private val stepTimings = ArrayDeque<Long>()
    private var verticalMovementSum = 0f
    private var consistentMovementCounter = 0

    private val logManager = LogManager(context)



    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> processAccelerometerData(event.values)
                Sensor.TYPE_GYROSCOPE -> processGyroscopeData(event.values)
                Sensor.TYPE_LINEAR_ACCELERATION -> processLinearAccelerationData(event.values)
            }
            logManager.logData(_sensorData.value, _isWalking.value)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }


    fun startListening() {
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(listener, gyroscope, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(listener, linearAcceleration, SensorManager.SENSOR_DELAY_GAME)
        logManager.startLogging()
    }

    fun stopListening() {
        sensorManager.unregisterListener(listener)
        logManager.stopLogging()

    }

    private fun processAccelerometerData(values: FloatArray) {
        val magnitude = calculateMagnitude(values)
        _sensorData.value = _sensorData.value.copy(accelerometerData = values.clone(), accelMagnitude = magnitude)

        updateVerticalMovement(values[1] - SensorManager.GRAVITY_EARTH)
        val isStep = detectStep(values, lastAccelValues, ACCEL_THRESHOLD)
        updateWalkingState(isStep)
        lastAccelValues = values.clone()
    }

    private fun processGyroscopeData(values: FloatArray) {
        val magnitude = calculateMagnitude(values)
        _sensorData.value = _sensorData.value.copy(gyroscopeData = values.clone(), gyroMagnitude = magnitude)
    }

    private fun processLinearAccelerationData(values: FloatArray) {
        val magnitude = calculateMagnitude(values)
        _sensorData.value = _sensorData.value.copy(linearAccelerationData = values.clone(), linearAccelMagnitude = magnitude)

        val isStep = detectStep(values, lastLinearAccelValues, LINEAR_ACCEL_THRESHOLD)
        updateWalkingState(isStep)
        lastLinearAccelValues = values.clone()
    }

    private fun calculateMagnitude(values: FloatArray): Float {
        return sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
    }

    private fun detectStep(currentValues: FloatArray, lastValues: FloatArray, threshold: Float): Boolean {
        val currentTime = System.currentTimeMillis()
        var stepDetected = false

        for (i in currentValues.indices) {
            val delta = abs(currentValues[i] - lastValues[i])
            if (delta > threshold && (currentTime - lastStepTime) > MIN_STEP_INTERVAL) {
                stepTimings.addLast(currentTime)
                if (stepTimings.size > STEP_TIMING_WINDOW) {
                    stepTimings.removeFirst()
                }
                stepCount++
                lastStepTime = currentTime
                stepDetected = true
                break
            }
        }

        return stepDetected
    }

    private fun updateVerticalMovement(verticalAcceleration: Float) {
        verticalMovementSum += abs(verticalAcceleration)
        if (verticalMovementSum > VERTICAL_MOVEMENT_THRESHOLD) {
            consistentMovementCounter++
            verticalMovementSum = 0f
        } else {
            consistentMovementCounter = 0
        }
    }

    private fun updateWalkingState(isStep: Boolean) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastStep = currentTime - lastStepTime

        if (isStep && isStepTimingConsistent() && consistentMovementCounter >= CONSISTENT_MOVEMENT_THRESHOLD) {
            if (stepCount >= MIN_STEPS_FOR_WALKING) {
                _isWalking.value = true
            }
        } else {
            if (_isWalking.value && timeSinceLastStep > QUICK_STOP_INTERVAL) {
                _isWalking.value = false
                stepCount = 0
                stepTimings.clear()
                consistentMovementCounter = 0
            }
        }

        // Reset step count if no steps for a while
        if (timeSinceLastStep > RESET_INTERVAL) {
            stepCount = 0
            stepTimings.clear()
            consistentMovementCounter = 0
        }
    }

    private fun isStepTimingConsistent(): Boolean {
        if (stepTimings.size < STEP_TIMING_WINDOW) return false
        val intervals = stepTimings.zipWithNext { a, b -> b - a }
        val avgInterval = intervals.average()
        return intervals.all { abs(it - avgInterval) < MAX_INTERVAL_DEVIATION }
    }


    fun startLogging() {
        logManager.startLogging()
    }

    fun stopLogging(): File {
        return File(logManager.saveLogFile()!!.path!!)
    }


    companion object {
        private const val ACCEL_THRESHOLD = 0.3f
        private const val LINEAR_ACCEL_THRESHOLD = 0.2f
        private const val MIN_STEP_INTERVAL = 250L // milliseconds
        private const val MIN_STEPS_FOR_WALKING = 1.5
        private const val QUICK_STOP_INTERVAL = 500L // milliseconds
        private const val RESET_INTERVAL = 1000L // milliseconds
        private const val STEP_TIMING_WINDOW = 5
        private const val MAX_INTERVAL_DEVIATION = 100L // milliseconds
        private const val VERTICAL_MOVEMENT_THRESHOLD = 0.9f
        private const val CONSISTENT_MOVEMENT_THRESHOLD = 3
    }
}


