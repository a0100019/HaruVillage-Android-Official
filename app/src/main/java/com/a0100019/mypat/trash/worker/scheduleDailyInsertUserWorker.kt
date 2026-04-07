package com.a0100019.mypat.trash.worker//package com.a0100019.mypat.domain.worker
//
//import android.content.Context
//import android.util.Log
//import androidx.work.Constraints
//import androidx.work.ExistingPeriodicWorkPolicy
//import androidx.work.NetworkType
//import androidx.work.PeriodicWorkRequestBuilder
//import androidx.work.WorkManager
//import java.time.Duration
//import java.time.LocalDateTime
//import java.util.concurrent.TimeUnit
//
//fun scheduleDailyInsertUserWorker(context: Context) {
//    val workManager = WorkManager.getInstance(context)
//
//    // 매일 실행할 시간 (예: 오전 3시)
//    val targetHour = 0
//    val now = LocalDateTime.now()
//    val targetTime = now.withHour(targetHour).withMinute(0).withSecond(0)
//
//    // 현재 시간이 targetHour 이후라면 다음 날로 설정
//    val initialDelay = if (now.isAfter(targetTime)) {
//        Duration.between(now, targetTime.plusDays(1)).toMillis()
//    } else {
//        Duration.between(now, targetTime).toMillis()
//    }
//
//    // WorkRequest 설정 (네트워크 제약 제거)
//    val workRequest = PeriodicWorkRequestBuilder<InsertUserWorker>(1, TimeUnit.DAYS)
//        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)  // 특정 시간에 실행
//        .build()
//
//    Log.d("WorkManager", "⏳ WorkManager 예약됨: ${targetHour}시에 실행 예정")
//
//    // ✅ 기존 작업이 있으면 업데이트 (중복 예약 방지)
//    workManager.enqueueUniquePeriodicWork(
//        "DailyInsertUserWorker",  // 고유한 작업 이름
//        ExistingPeriodicWorkPolicy.UPDATE,  // 기존 작업이 있으면 업데이트
//        workRequest
//    )
//}
//
