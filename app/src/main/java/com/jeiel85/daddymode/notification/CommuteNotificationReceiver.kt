package com.jeiel85.daddymode.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jeiel85.daddymode.MainActivity

class CommuteNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "dad_mode_commute_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "퇴근 전환 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "퇴근 시간에 3분 호흡 및 아빠모드 전환을 상기시켜주는 알림입니다."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messages = listOf(
            "오늘 회사에서 고생 많으셨습니다. 아내와 아이들을 보기 전, 3분간 가볍게 호흡하며 아빠모드로 전환해보세요.",
            "하루 종일 무거웠던 회사 일은 퇴근길에 흘려보내고, 사랑하는 가족에게 온전히 따뜻한 아빠가 될 시간입니다.",
            "수고하셨습니다 아빠! 문 앞으로 가기 전, 머릿속을 정돈하는 3분 호흡을 함께 해요.",
            "퇴근 완료! 스트레스 주머니는 밖에 비워 두고, 집에 들어갈 땐 웃는 얼굴로 문을 열어줄까요?"
        )
        val selectedMessage = messages.random()

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback icon compatible with any SDK
            .setContentTitle("퇴근길, 아빠모드를 켤 시간입니다 👨‍👦")
            .setContentText(selectedMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(selectedMessage))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(4882, notification)
    }
}
