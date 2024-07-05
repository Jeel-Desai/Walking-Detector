package com.ui.demomovementdetection.sensor

data class SensorData(
    val accelerometerData: FloatArray = FloatArray(3),
    val gyroscopeData: FloatArray = FloatArray(3),
    val linearAccelerationData: FloatArray = FloatArray(3),
    val accelMagnitude: Float = 0f,
    val gyroMagnitude: Float = 0f,
    val linearAccelMagnitude: Float = 0f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorData

        if (!accelerometerData.contentEquals(other.accelerometerData)) return false
        if (!gyroscopeData.contentEquals(other.gyroscopeData)) return false
        if (!linearAccelerationData.contentEquals(other.linearAccelerationData)) return false
        if (accelMagnitude != other.accelMagnitude) return false
        if (gyroMagnitude != other.gyroMagnitude) return false
        if (linearAccelMagnitude != other.linearAccelMagnitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = accelerometerData.contentHashCode()
        result = 31 * result + gyroscopeData.contentHashCode()
        result = 31 * result + linearAccelerationData.contentHashCode()
        result = 31 * result + accelMagnitude.hashCode()
        result = 31 * result + gyroMagnitude.hashCode()
        result = 31 * result + linearAccelMagnitude.hashCode()
        return result
    }
}