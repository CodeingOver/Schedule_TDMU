package com.example.schedule_tdmu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.schedule_tdmu.model.TaiKhoan;
import com.example.schedule_tdmu.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialCheckBox cbRememberMe;
    private TextView tvForgotPassword;
    private MaterialButton btnLogin, btnRegister;

    private SharedPrefManager prefManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        prefManager = new SharedPrefManager(this);
        dbHelper = new DatabaseHelper(this);

        if (prefManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.til_login_email);
        tilPassword = findViewById(R.id.til_login_password);
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        if (prefManager.isRememberMe()) {
            String savedId = prefManager.getSavedIdentifier();
            if (savedId != null && !savedId.isEmpty()) {
                etEmail.setText(savedId);
                cbRememberMe.setChecked(true);
            }
        }

        btnLogin.setOnClickListener(v -> performLogin());
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        btnRegister.setOnClickListener(v -> showRegisterDialog());
    }

    private void performLogin() {
        if (tilEmail != null) tilEmail.setError(null);
        if (tilPassword != null) tilPassword.setError(null);

        String identifier = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (identifier.isEmpty()) {
            if (tilEmail != null) tilEmail.setError("Vui lòng nhập MSSV hoặc Email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            if (tilPassword != null) tilPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        TaiKhoan user = dbHelper.authenticateUser(identifier, password);
        if (user == null) {
            boolean exists = dbHelper.isUserExists(identifier);
            if (exists) {
                if (tilPassword != null) {
                    tilPassword.setError("Mật khẩu không chính xác. Vui lòng kiểm tra lại!");
                }
                etPassword.requestFocus();
            } else {
                if (tilEmail != null) {
                    tilEmail.setError("Tài khoản chưa tồn tại. Vui lòng chọn 'Đăng ký ngay'!");
                }
                etEmail.requestFocus();
            }
            return;
        }

        // Đăng nhập thành công -> Lưu session
        boolean rememberMe = cbRememberMe != null && cbRememberMe.isChecked();
        prefManager.saveUserSession(
                user.getEmail(),
                user.getHoTen(),
                user.getVaiTro(),
                user.getMssv(),
                user.getLopHoc(),
                user.getKhoaVien(),
                rememberMe
        );

        Toast.makeText(this, "Đăng nhập thành công! Xin chào " + user.getHoTen(), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showRegisterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register, null);

        TextInputLayout tilRegName = dialogView.findViewById(R.id.til_reg_name);
        TextInputLayout tilRegMssv = dialogView.findViewById(R.id.til_reg_mssv);
        TextInputLayout tilRegClass = dialogView.findViewById(R.id.til_reg_class);
        TextInputLayout tilRegPass = dialogView.findViewById(R.id.til_reg_password);
        TextInputLayout tilRegConfirm = dialogView.findViewById(R.id.til_reg_confirm_password);

        TextInputEditText etRegName = dialogView.findViewById(R.id.et_reg_name);
        TextInputEditText etRegMssv = dialogView.findViewById(R.id.et_reg_mssv);
        TextInputEditText etRegClass = dialogView.findViewById(R.id.et_reg_class);
        TextInputEditText etRegPass = dialogView.findViewById(R.id.et_reg_password);
        TextInputEditText etRegConfirm = dialogView.findViewById(R.id.et_reg_confirm_password);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Đăng ký", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (tilRegName != null) tilRegName.setError(null);
            if (tilRegMssv != null) tilRegMssv.setError(null);
            if (tilRegClass != null) tilRegClass.setError(null);
            if (tilRegPass != null) tilRegPass.setError(null);
            if (tilRegConfirm != null) tilRegConfirm.setError(null);

            String name = etRegName.getText() != null ? etRegName.getText().toString().trim() : "";
            String mssv = etRegMssv.getText() != null ? etRegMssv.getText().toString().trim() : "";
            String lopHoc = etRegClass.getText() != null ? etRegClass.getText().toString().trim() : "";
            String pass = etRegPass.getText() != null ? etRegPass.getText().toString().trim() : "";
            String confirmPass = etRegConfirm.getText() != null ? etRegConfirm.getText().toString().trim() : "";

            if (name.isEmpty()) {
                if (tilRegName != null) tilRegName.setError("Vui lòng nhập họ và tên");
                etRegName.requestFocus();
                return;
            }

            if (mssv.isEmpty()) {
                if (tilRegMssv != null) tilRegMssv.setError("Vui lòng nhập MSSV");
                etRegMssv.requestFocus();
                return;
            }

            if (pass.length() < 4) {
                if (tilRegPass != null) tilRegPass.setError("Mật khẩu cần ít nhất 4 ký tự");
                etRegPass.requestFocus();
                return;
            }

            if (!pass.equals(confirmPass)) {
                if (tilRegConfirm != null) tilRegConfirm.setError("Mật khẩu xác nhận không khớp");
                etRegConfirm.requestFocus();
                return;
            }

            String email = mssv.contains("@") ? mssv.toLowerCase(Locale.ROOT) : mssv.toLowerCase(Locale.ROOT) + "@student.tdmu.edu.vn";

            if (dbHelper.isUserExists(mssv) || dbHelper.isUserExists(email)) {
                if (tilRegMssv != null) tilRegMssv.setError("MSSV hoặc Email này đã tồn tại trên thiết bị");
                etRegMssv.requestFocus();
                return;
            }

            String ngayTao = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            TaiKhoan newAccount = new TaiKhoan(
                    mssv,
                    email,
                    pass,
                    name,
                    lopHoc.isEmpty() ? "Chưa cập nhật" : lopHoc,
                    "Viện Kỹ thuật - Công nghệ",
                    "student",
                    ngayTao
            );

            long rowId = dbHelper.insertTaiKhoan(newAccount);
            if (rowId > 0) {
                Toast.makeText(this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show();
                etEmail.setText(mssv);
                etPassword.setText("");
                etPassword.requestFocus();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Có lỗi xảy ra khi tạo tài khoản. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showForgotPasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);

        TextInputLayout tilForgotId = dialogView.findViewById(R.id.til_forgot_identifier);
        TextInputLayout tilForgotPass = dialogView.findViewById(R.id.til_forgot_new_password);
        TextInputLayout tilForgotConfirm = dialogView.findViewById(R.id.til_forgot_confirm_password);

        TextInputEditText etForgotId = dialogView.findViewById(R.id.et_forgot_identifier);
        TextInputEditText etForgotPass = dialogView.findViewById(R.id.et_forgot_new_password);
        TextInputEditText etForgotConfirm = dialogView.findViewById(R.id.et_forgot_confirm_password);

        // Điền sẵn MSSV/email từ ô đăng nhập nếu đã có
        String currentInput = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (!currentInput.isEmpty()) {
            etForgotId.setText(currentInput);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Xác nhận", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (tilForgotId != null) tilForgotId.setError(null);
            if (tilForgotPass != null) tilForgotPass.setError(null);
            if (tilForgotConfirm != null) tilForgotConfirm.setError(null);

            String identifier = etForgotId.getText() != null ? etForgotId.getText().toString().trim() : "";
            String newPass = etForgotPass.getText() != null ? etForgotPass.getText().toString().trim() : "";
            String confirmPass = etForgotConfirm.getText() != null ? etForgotConfirm.getText().toString().trim() : "";

            if (identifier.isEmpty()) {
                if (tilForgotId != null) tilForgotId.setError("Vui lòng nhập MSSV hoặc Email");
                etForgotId.requestFocus();
                return;
            }

            if (!dbHelper.isUserExists(identifier)) {
                if (tilForgotId != null) tilForgotId.setError("Không tìm thấy tài khoản với MSSV/Email này");
                etForgotId.requestFocus();
                return;
            }

            if (newPass.length() < 4) {
                if (tilForgotPass != null) tilForgotPass.setError("Mật khẩu mới cần ít nhất 4 ký tự");
                etForgotPass.requestFocus();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                if (tilForgotConfirm != null) tilForgotConfirm.setError("Mật khẩu xác nhận không trùng khớp");
                etForgotConfirm.requestFocus();
                return;
            }

            boolean updated = dbHelper.updatePassword(identifier, newPass);
            if (updated) {
                Toast.makeText(this, "Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.", Toast.LENGTH_LONG).show();
                etEmail.setText(identifier);
                etPassword.setText("");
                etPassword.requestFocus();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Không thể cập nhật mật khẩu. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
