package com.example.schedule_tdmu.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String PREF_NAME = "TDMU_USER_SESSION";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_STUDENT_EMAIL = "student_email";
    private static final String KEY_STUDENT_NAME = "student_name";
    private static final String KEY_USER_ROLE = "user_role"; // "admin" hoặc "student"
    private static final String KEY_STUDENT_MSSV = "student_mssv";
    private static final String KEY_STUDENT_CLASS = "student_class";
    private static final String KEY_STUDENT_FACULTY = "student_faculty";
    private static final String KEY_STUDENT_COHORT = "student_cohort";
    private static final String KEY_STUDENT_GPA = "student_gpa";
    private static final String KEY_STUDENT_CREDITS = "student_credits";
    private static final String KEY_STUDENT_DRL = "student_drl";

    private static final String KEY_REMINDER_MINUTES = "reminder_minutes";
    private static final String KEY_ALARM_PERIOD_1 = "alarm_period_1";
    private static final String KEY_SILENT_IN_CLASS = "silent_in_class";
    private static final String KEY_GOOGLE_CAL_SYNC = "google_cal_sync";
    private static final String KEY_APPLE_CAL_SYNC = "apple_cal_sync";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveUserSession(String email, String name, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_STUDENT_EMAIL, email);
        editor.putString(KEY_STUDENT_NAME, name);
        editor.putString(KEY_USER_ROLE, role);
        if (email != null && email.contains("@")) {
            String prefix = email.split("@")[0];
            if (prefix.matches("[0-9]+")) {
                editor.putString(KEY_STUDENT_MSSV, prefix);
            }
        }
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getStudentEmail() {
        return pref.getString(KEY_STUDENT_EMAIL, "student@tdmu.edu.vn");
    }

    public String getStudentName() {
        return pref.getString(KEY_STUDENT_NAME, "Sinh viên TDMU");
    }

    public void setStudentName(String name) {
        editor.putString(KEY_STUDENT_NAME, name).apply();
    }

    public String getStudentMssv() {
        String saved = pref.getString(KEY_STUDENT_MSSV, "");
        if (saved != null && !saved.isEmpty()) return saved;
        String email = getStudentEmail();
        if (email.contains("@")) {
            String prefix = email.split("@")[0];
            if (prefix.matches("[0-9]+")) return prefix;
        }
        return "Chưa cập nhật";
    }

    public void setStudentMssv(String mssv) {
        editor.putString(KEY_STUDENT_MSSV, mssv).apply();
    }

    public String getStudentClass() {
        return pref.getString(KEY_STUDENT_CLASS, "Chưa cập nhật lớp");
    }

    public void setStudentClass(String studentClass) {
        editor.putString(KEY_STUDENT_CLASS, studentClass).apply();
    }

    public String getStudentFaculty() {
        return pref.getString(KEY_STUDENT_FACULTY, "Viện Kỹ thuật - Công nghệ");
    }

    public String getStudentCohort() {
        return pref.getString(KEY_STUDENT_COHORT, "Chính quy");
    }

    public float getStudentGpa() {
        return pref.getFloat(KEY_STUDENT_GPA, 0.0f);
    }

    public void setStudentGpa(float gpa) {
        editor.putFloat(KEY_STUDENT_GPA, gpa).apply();
    }

    public int getAccumulatedCredits() {
        return pref.getInt(KEY_STUDENT_CREDITS, 0);
    }

    public void setAccumulatedCredits(int credits) {
        editor.putInt(KEY_STUDENT_CREDITS, credits).apply();
    }

    public int getStudentDrl() {
        return pref.getInt(KEY_STUDENT_DRL, 0);
    }

    public void setStudentDrl(int drl) {
        editor.putInt(KEY_STUDENT_DRL, drl).apply();
    }

    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, "student");
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(getUserRole());
    }

    public int getReminderMinutes() {
        return pref.getInt(KEY_REMINDER_MINUTES, 15);
    }

    public void setReminderMinutes(int minutes) {
        editor.putInt(KEY_REMINDER_MINUTES, minutes).apply();
    }

    public boolean isAlarmPeriod1Enabled() {
        return pref.getBoolean(KEY_ALARM_PERIOD_1, true);
    }

    public void setAlarmPeriod1Enabled(boolean enabled) {
        editor.putBoolean(KEY_ALARM_PERIOD_1, enabled).apply();
    }

    public boolean isSilentInClassEnabled() {
        return pref.getBoolean(KEY_SILENT_IN_CLASS, true);
    }

    public void setSilentInClassEnabled(boolean enabled) {
        editor.putBoolean(KEY_SILENT_IN_CLASS, enabled).apply();
    }

    public boolean isGoogleCalSynced() {
        return pref.getBoolean(KEY_GOOGLE_CAL_SYNC, true);
    }

    public void setGoogleCalSynced(boolean synced) {
        editor.putBoolean(KEY_GOOGLE_CAL_SYNC, synced).apply();
    }

    public boolean isAppleCalSynced() {
        return pref.getBoolean(KEY_APPLE_CAL_SYNC, false);
    }

    public void setAppleCalSynced(boolean synced) {
        editor.putBoolean(KEY_APPLE_CAL_SYNC, synced).apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
