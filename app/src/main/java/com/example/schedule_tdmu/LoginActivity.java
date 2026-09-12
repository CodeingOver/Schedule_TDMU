package com.example.schedule_tdmu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.schedule_tdmu.utils.SharedPrefManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnSkipLogin;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager = new SharedPrefManager(this);

        // Nếu đã đăng nhập trước đó -> Vào thẳng MainActivity
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
            prefManager.saveUserSession("khach@student.tdmu.edu.vn", "Sinh viên Khách", "student");
            Toast.makeText(this, "Đã truy cập với chế độ Khách", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Email và Mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("@student.tdmu.edu.vn") && !email.endsWith("@tdmu.edu.vn")) {
            Toast.makeText(this, "Vui lòng sử dụng Email trường cấp (@student.tdmu.edu.vn hoặc @tdmu.edu.vn)!", Toast.LENGTH_LONG).show();
            return;
        }

        // Xác định vai trò
        String role = "student";
        if (email.equalsIgnoreCase("admin@tdmu.edu.vn") || (!email.contains("student") && email.endsWith("@tdmu.edu.vn"))) {
            role = "admin";
        }

        String username = email.split("@")[0];
        String displayName = role.equals("admin") ? "Quản trị viên (" + username + ")" : "Sinh viên " + username;

        prefManager.saveUserSession(email, displayName, role);
        Toast.makeText(this, "Đăng nhập thành công với vai trò " + (role.equals("admin") ? "ADMIN" : "SINH VIÊN"), Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
