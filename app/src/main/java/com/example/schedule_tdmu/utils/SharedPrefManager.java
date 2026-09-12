package com.example.schedule_tdmu.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String PREF_NAME = "TDMU_USER_SESSION";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_STUDENT_EMAIL = "student_email";
    private static final String KEY_STUDENT_NAME = "student_name";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveUserSession(String email, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_STUDENT_EMAIL, email);
        editor.putString(KEY_STUDENT_NAME, name);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getStudentEmail() {
        return pref.getString(KEY_STUDENT_EMAIL, "21111000@student.tdmu.edu.vn");
    }

    public String getStudentName() {
        return pref.getString(KEY_STUDENT_NAME, "Sinh viên TDMU");
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
