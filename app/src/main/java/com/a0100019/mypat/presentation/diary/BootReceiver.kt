package com.a0100019.mypat.presentation.diary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            val prefs =
                context.getSharedPreferences("diary_alarm", Context.MODE_PRIVATE)

            val timeString = prefs.getString("alarm_time", null)

            //  저장된 알람 시간이 있으면 복구
            if (!timeString.isNullOrEmpty()) {
                scheduleDiaryAlarm(context, timeString)
            }
        }
    }
}
