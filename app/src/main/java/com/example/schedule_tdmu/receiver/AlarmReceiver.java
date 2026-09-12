package com.example.schedule_tdmu.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.schedule_tdmu.R;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "tdmu_schedule_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        if (title == null || title.isEmpty()) {
            title = "Nhắc nhở Lịch học TDMU 🔔";
        }
        if (message == null || message.isEmpty()) {
            message = "Bạn có lịch học/thi sắp diễn ra. Hãy kiểm tra ứng dụng nhé!";
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
