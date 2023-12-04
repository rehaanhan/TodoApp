package com.dicoding.todoapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.todoapp.R
import com.dicoding.todoapp.data.Task
import com.dicoding.todoapp.data.TaskRepository
import com.dicoding.todoapp.ui.detail.DetailTaskActivity
import com.dicoding.todoapp.utils.NOTIFICATION_CHANNEL_ID
import com.dicoding.todoapp.utils.TASK_ID

class NotificationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val channelName = inputData.getString(NOTIFICATION_CHANNEL_ID)

    private fun getPendingIntent(task: Task): PendingIntent? {
        val intent = Intent(applicationContext, DetailTaskActivity::class.java).apply {
            putExtra(TASK_ID, task.id)
        }
        return TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }
    }

    override fun doWork(): Result {
        Log.d("Check", "Started Worker")
        //TODO 14 : If notification preference on, get nearest active task from repository and show notification with pending intent - Done
        val notifPreference = applicationContext.getString(R.string.pref_key_notify)
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val isEnabled = preferences.getBoolean(notifPreference, false)

        if (isEnabled){
            val repository = TaskRepository.getInstance(applicationContext)
            val nearestActiveTask = repository.getNearestActiveTask()

            nearestActiveTask?.let { task ->
                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val pendingIntent = getPendingIntent(task)

                val notificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(task.title)
                    .setContentText(task.description)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName ?: "TaskNotif", NotificationManager.IMPORTANCE_DEFAULT)
                    notificationManager.createNotificationChannel(channel)
                }

                notificationManager.notify(task.id, notificationBuilder.build())
            }
        }

        return Result.success()
    }

}
