package com.example.schedule_tdmu.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.schedule_tdmu.DatabaseHelper;
import com.example.schedule_tdmu.adapter.LichHocAdapter;
import com.example.schedule_tdmu.model.LichHoc;
import com.example.schedule_tdmu.model.LichThi;
import com.example.schedule_tdmu.receiver.AlarmReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationHelper {

    public static void scheduleNotification(Context context, String title, String message, long triggerTimeMillis, int notificationId) {
        if (context == null) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
            }
        } catch (SecurityException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
            }
        }
    }

    public static void cancelNotification(Context context, int notificationId) {
        if (context == null) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static boolean canScheduleExactAlarms(Context context) {
        if (context == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    /**
     * Đồng bộ tự động toàn bộ lịch học (LichHoc) và lịch thi (LichThi) trong SQLite với AlarmManager.
     * Trả về tổng số nhắc hẹn đã được lập lịch.
     */
    public static int syncAllScheduleReminders(Context context) {
        if (context == null) return 0;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SharedPrefManager prefManager = new SharedPrefManager(context);

        int reminderLeadMinutes = prefManager.getReminderMinutes();
        boolean alarmPeriod1 = prefManager.isAlarmPeriod1Enabled();
        long nowMillis = System.currentTimeMillis();
        int scheduledCount = 0;

        // 1. Đặt lịch nhắc trước giờ vào học & báo thức Tiết 1
        List<LichHoc> allClasses = dbHelper.getAllLichHocWithMonHoc();
        for (LichHoc lh : allClasses) {
            int tietStart = lh.getTietBatDau();
            int tietEnd = tietStart + lh.getSoTiet() - 1;
            int startMinutesOfDay = LichHocAdapter.getPeriodStartMinutes(tietStart);
            String timeRange = LichHocAdapter.getTdmuPeriodTime(tietStart, tietEnd);

            long targetClassStartMillis = computeNextOccurrenceMillis(lh.getThu(), startMinutesOfDay / 60, startMinutesOfDay % 60);

            // Tìm buổi học tiếp theo mà môn học này thực sự diễn ra (occursOnDate)
            boolean foundOccurrence = false;
            for (int w = 0; w < 20; w++) {
                if (lh.occursOnDate(new Date(targetClassStartMillis))) {
                    foundOccurrence = true;
                    break;
                }
                targetClassStartMillis += 7L * 24L * 60L * 60L * 1000L;
            }

            if (!foundOccurrence) {
                // Môn học này đã kết thúc hoặc không còn buổi nào trong tương lai
                cancelNotification(context, 10000 + lh.getId());
                cancelNotification(context, 20000 + lh.getId());
                continue;
            }

            long reminderTriggerMillis = targetClassStartMillis - (reminderLeadMinutes * 60L * 1000L);
            if (reminderTriggerMillis <= nowMillis) {
                // Nếu buổi học đó là hôm nay và chưa bắt đầu (nowMillis < targetClassStartMillis)
                // nhưng đã qua mốc nhắc trước (ví dụ còn 30p mà lead time là 45p):
                if (targetClassStartMillis > nowMillis) {
                    reminderTriggerMillis = nowMillis + 2000L; // Nhắc ngay sau 2s
                } else {
                    // Nếu buổi học đã qua, chuyển sang tuần tiếp theo
                    long nextWeekStartMillis = targetClassStartMillis + 7L * 24L * 60L * 60L * 1000L;
                    boolean nextFound = false;
                    for (int w = 0; w < 20; w++) {
                        if (lh.occursOnDate(new Date(nextWeekStartMillis))) {
                            nextFound = true;
                            break;
                        }
                        nextWeekStartMillis += 7L * 24L * 60L * 60L * 1000L;
                    }
                    if (!nextFound) {
                        cancelNotification(context, 10000 + lh.getId());
                        cancelNotification(context, 20000 + lh.getId());
                        continue;
                    }
                    targetClassStartMillis = nextWeekStartMillis;
                    reminderTriggerMillis = targetClassStartMillis - (reminderLeadMinutes * 60L * 1000L);
                }
            }

            String title = "Sắp vào lớp (" + reminderLeadMinutes + " phút nữa): " + lh.getTenMon();
            String msg = "Phòng " + lh.getPhongHoc() + " • " + timeRange + " (Tiết " + tietStart + "-" + tietEnd + ")";
            scheduleNotification(context, title, msg, reminderTriggerMillis, 10000 + lh.getId());
            scheduledCount++;

            if (tietStart == 1) {
                if (alarmPeriod1) {
                    Calendar calAlarm = Calendar.getInstance();
                    calAlarm.setTimeInMillis(targetClassStartMillis);
                    calAlarm.set(Calendar.HOUR_OF_DAY, 6);
                    calAlarm.set(Calendar.MINUTE, 15);
                    calAlarm.set(Calendar.SECOND, 0);
                    calAlarm.set(Calendar.MILLISECOND, 0);

                    long alarm615Millis = calAlarm.getTimeInMillis();
                    if (alarm615Millis > nowMillis) {
                        String alarmTitle = "Báo thức Tiết 1 (06:15): " + lh.getTenMon();
                        String alarmMsg = "Chuẩn bị đi học tại Phòng " + lh.getPhongHoc() + " (Vào lớp lúc 07:00)";
                        scheduleNotification(context, alarmTitle, alarmMsg, alarm615Millis, 20000 + lh.getId());
                        scheduledCount++;
                    }
                } else {
                    cancelNotification(context, 20000 + lh.getId());
                }
            }
        }

        // 2. Đặt lịch nhắc trước giờ thi 30 phút
        List<LichThi> allExams = dbHelper.getAllLichThiWithMonHoc();
        SimpleDateFormat sdfExam = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        for (LichThi lt : allExams) {
            try {
                Date examDate = sdfExam.parse(lt.getNgayGioThi());
                if (examDate != null) {
                    long triggerMillis = examDate.getTime() - (30L * 60L * 1000L);
                    if (triggerMillis > nowMillis) {
                        String examTitle = "Nhắc lịch thi (30 phút nữa): " + lt.getTenMon();
                        String examMsg = "Phòng thi " + lt.getPhongThi() + " • SBD: " + lt.getSbd() + " • Giờ thi: " + lt.getNgayGioThi();
                        scheduleNotification(context, examTitle, examMsg, triggerMillis, 30000 + lt.getId());
                        scheduledCount++;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return scheduledCount;
    }

    private static long computeNextOccurrenceMillis(int thu, int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        int targetCalendarDow = (thu == 8) ? Calendar.SUNDAY : thu;

        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);

        int currentDow = cal.get(Calendar.DAY_OF_WEEK);
        int daysUntil = (targetCalendarDow - currentDow + 7) % 7;
        cal.add(Calendar.DAY_OF_YEAR, daysUntil);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 7);
        }
        return cal.getTimeInMillis();
    }
}
