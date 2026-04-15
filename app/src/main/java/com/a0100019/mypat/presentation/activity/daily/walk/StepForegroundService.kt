package com.a0100019.mypat.presentation.activity.daily.walk

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.a0100019.mypat.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class StepForegroundService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private var wakeLock: PowerManager.WakeLock? = null

    private val channelId = "step_counter_channel"

    @SuppressLint("WakelockTimeout")
    override fun onCreate() {
        super.onCreate()

        //  WAKE_LOCK 획득 (절전 모드에서도 유지)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HaruVillage:StepWakeLock"
        )
        wakeLock?.acquire() // 화면 꺼져도 CPU 살아있음

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        createNotificationChannel()
        startForeground(1, createNotification("걸음 수 측정 중..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()

        sensorManager.unregisterListener(this)

        //  WAKE_LOCK 해제
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.cancel(1)
    }


    override fun onBind(intent: Intent?): IBinder? = null



    // ================================================================
    //  걸음 수 저장 로직 (너가 작성한 것 그대로 둠)
    // ================================================================
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {

            val prefs = getSharedPreferences("step_prefs", Context.MODE_PRIVATE)

            // 누적 걸음 저장
            val saveSteps = prefs.getInt("saveSteps", 0)
            var updatedSteps = saveSteps + 1
            if (updatedSteps % 10 == 0) {
                updatedSteps += 1
            }

            prefs.edit().putInt("saveSteps", updatedSteps).apply()

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

            val stepsRaw = prefs.getString("stepsRaw", "$today.1") ?: "$today.1"
            val items = stepsRaw.split("/").toMutableList()

            var updated = false

            for (i in items.indices) {
                val parts = items[i].split(".")
                if (parts.size == 2) {
                    val date = parts[0]
                    val count = parts[1].toInt()

                    if (date == today) {
                        var newCount = count + 1
                        if (newCount % 10 == 0) {
                            newCount += 1
                        }
                        items[i] = "$today.$newCount"
                        updated = true
                        updateNotification("$newCount 걸음")
                        break
                    }
                }
            }

            if (!updated) {
                items.add("$today.1")
                updateNotification("1 걸음")
            }

            prefs.edit()
                .putString("stepsRaw", items.joinToString("/"))
                .apply()
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}


    // ================================================================
    // 🔔 알림 설정
    // ================================================================
    @SuppressLint("ObsoleteSdkInt")
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "걸음 수 측정",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("만보기")
            .setContentText(text)
            .setSmallIcon(R.drawable.pet)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("NotificationPermission")
    private fun updateNotification(text: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("만보기")
            .setContentText(text)
            .setSmallIcon(R.drawable.pet)
            .setOngoing(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }
}
