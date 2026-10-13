package com.example.schedule_tdmu.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.schedule_tdmu.MainActivity;
import com.example.schedule_tdmu.R;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "tdmu_schedule_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        if (title == null || title.isEmpty()) {
            title = "Nhắc nhở Lịch học EduSchedule";
        }
        if (message == null || message.isEmpty()) {
            message = "Bạn có lịch học hoặc lịch thi sắp diễn ra. Hãy kiểm tra ứng dụng.";
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thông báo Lịch học TDMU",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Kênh nhận thông báo tự động nhắc nhở lịch học và lịch thi sinh viên TDMU");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_active)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }

        // Tu dong lap lich cho lan hoc ke tiep
        com.example.schedule_tdmu.utils.NotificationHelper.syncAllScheduleReminders(context);
    }
}
