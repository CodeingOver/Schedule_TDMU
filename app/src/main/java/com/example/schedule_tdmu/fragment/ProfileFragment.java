package com.example.schedule_tdmu.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.schedule_tdmu.LoginActivity;
import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.utils.SharedPrefManager;

public class ProfileFragment extends Fragment {

    private SharedPrefManager prefManager;
    private TextView tvProfileName, tvProfileEmail, tvProfileMssv, tvProfileRole;
    private Button btnLogout, btnChangePassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (getContext() != null) {
            prefManager = new SharedPrefManager(getContext());
        }

        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        tvProfileMssv = view.findViewById(R.id.tv_profile_mssv);
        tvProfileRole = view.findViewById(R.id.tv_profile_role);

        btnLogout = view.findViewById(R.id.btn_logout);
        btnChangePassword = view.findViewById(R.id.btn_change_password);

        if (prefManager != null) {
            String name = prefManager.getStudentName();
            String email = prefManager.getStudentEmail();
            String mssv = email.split("@")[0];

            tvProfileName.setText(name);
            tvProfileEmail.setText(email);
            tvProfileMssv.setText("MSSV: " + mssv);

            if (prefManager.isAdmin()) {
                tvProfileRole.setText("⭐ QUẢN TRỊ VIÊN TDMU");
            } else {
                tvProfileRole.setText("🎓 SINH VIÊN CHÍNH QUY");
            }
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                if (prefManager != null) {
                    prefManager.logout();
                }
                Toast.makeText(getContext(), "Đã đăng xuất tài khoản!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
            });
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> Toast.makeText(getContext(), "Tính năng đổi mật khẩu đang mở rộng!", Toast.LENGTH_SHORT).show());
        }

        return view;
    }
}
