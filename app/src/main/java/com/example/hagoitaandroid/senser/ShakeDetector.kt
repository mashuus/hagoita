package com.example.hagoitaandroid.senser
// kotlin
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

class ShakeDetector(
    private val context: Context,
    private val onShake: (Long) -> Unit,
    private val threshold: Float = 12f,       // 加速度合成の閾値 (m/s^2)
    private val minIntervalMs: Long = 300L    // 連続検出防止
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accel: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastEventTime = 0L

    fun start() {
        accel?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val now = System.currentTimeMillis()
        if (now - lastEventTime < minIntervalMs) return

        val ax = event.values.getOrNull(0) ?: 0f
        val ay = event.values.getOrNull(1) ?: 0f
        val az = event.values.getOrNull(2) ?: 0f
        val magnitude = sqrt(ax * ax + ay * ay + az * az)

        // Logcat に記録（必要なら BuildConfig.DEBUG で制御）
        Log.d("ShakeDetector", "sensor t=$now mag=$magnitude ax=$ax ay=$ay az=$az")

        if (magnitude >= threshold) {
            lastEventTime = now
            // ViewModel に通知（タイムスタンプはここで決める）
            onShake(now)
            // 必要なら内部ファイルへ保存する処理を追加可能
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* no-op */ }
}
