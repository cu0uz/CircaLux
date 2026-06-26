package com.example.circalux.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrientationManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _heading = MutableStateFlow(0f)
    val heading = _heading.asStateFlow()

    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)

    fun startListening() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) gravity = event.values
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) geomagnetic = event.values

        val r = FloatArray(9)
        val i = FloatArray(9)
        if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(r, orientation)
            val azimuthRadians = orientation[0]
            var azimuthDegrees = Math.toDegrees(azimuthRadians.toDouble()).toFloat()
            if (azimuthDegrees < 0) azimuthDegrees += 360f
            _heading.value = azimuthDegrees
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
