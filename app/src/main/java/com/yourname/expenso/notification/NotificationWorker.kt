package com.yourname.expenso.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationManager: ExpenseNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            
            // Only send notifications between 8 AM and 10 PM
            if (currentHour in 8..22) {
                notificationManager.sendExpenseReminder()
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

class NotificationScheduler @AssistedInject constructor(
    @Assisted private val context: Context
) {
    
    fun scheduleNotifications() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(
            3, TimeUnit.HOURS,
            30, TimeUnit.MINUTES // 3.5 hours = 3h 30m
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "expense_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWork
        )
    }
    
    fun cancelNotifications() {
        WorkManager.getInstance(context).cancelUniqueWork("expense_notifications")
    }
}