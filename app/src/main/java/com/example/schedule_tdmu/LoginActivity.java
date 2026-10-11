package com.example.schedule_tdmu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.schedule_tdmu.utils.SharedPrefManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnSkipLogin;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        prefManager = new SharedPrefManager(this);

        if (prefManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSkipLogin = findViewById(R.id.btn_skip_login);

        btnLogin.setOnClickListener(v -> performLogin());

        btnSkipLogin.setOnClickListener(v -> {
            prefManager.saveUserSession("21020485@student.tdmu.edu.vn", "Nguyễn Minh Anh", "student");
            prefManager.setStudentMssv("21020485");
            Toast.makeText(this, "Đã đăng nhập với hồ sơ Nguyễn Minh Anh", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ Email và Mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("@student.tdmu.edu.vn") && !email.endsWith("@tdmu.edu.vn")) {
            Toast.makeText(this, "Vui lòng sử dụng Email trường cấp (@student.tdmu.edu.vn hoặc @tdmu.edu.vn)!", Toast.LENGTH_LONG).show();
            return;
        }

        String role = "student";
        if (email.equalsIgnoreCase("admin@tdmu.edu.vn") || (!email.contains("student") && email.endsWith("@tdmu.edu.vn"))) {
            role = "admin";
        }

        String username = email.split("@")[0];
        String displayName;
        if (role.equals("admin")) {
            displayName = "Quản trị viên (" + username + ")";
        } else if ("21020485".equals(username) || "21111000".equals(username)) {
            displayName = "Nguyễn Minh Anh";
        } else {
            displayName = "Sinh viên " + username;
        }

        prefManager.saveUserSession(email, displayName, role);
        Toast.makeText(this, "Đăng nhập thành công (" + (role.equals("admin") ? "Quản trị viên" : "Sinh viên") + ")", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
