package com.example.hagoitaandroid.senser
// kotlin
import android.hardware.SensorEventListener
import android.hardware.SensorEvent
import android.hardware.Sensor

class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        // 判定ロジックをここに
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
