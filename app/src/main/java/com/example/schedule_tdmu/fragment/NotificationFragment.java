package com.example.schedule_tdmu.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.DatabaseHelper;
import com.example.schedule_tdmu.LoginActivity;
import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.adapter.ThongBaoAdapter;
import com.example.schedule_tdmu.model.ThongBao;
import com.example.schedule_tdmu.utils.NotificationHelper;
import com.example.schedule_tdmu.utils.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private SharedPrefManager prefManager;
    private RecyclerView rvAllNotifications;
    private ThongBaoAdapter adapter;
    private TextView tvProfileName, tvProfileEmail;
    private Button btnLogout, btnAddNotification;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        if (getContext() != null) {
            dbHelper = new DatabaseHelper(getContext());
            prefManager = new SharedPrefManager(getContext());
        }

        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnAddNotification = view.findViewById(R.id.btn_add_notification);

        if (prefManager != null) {
            tvProfileName.setText(prefManager.getStudentName());
            tvProfileEmail.setText(prefManager.getStudentEmail());

            // Chỉ hiển thị nút Đăng thông báo nếu người dùng là ADMIN
            if (prefManager.isAdmin()) {
                btnAddNotification.setVisibility(View.VISIBLE);
                btnAddNotification.setOnClickListener(v -> showAddNotificationDialog());
            } else {
                btnAddNotification.setVisibility(View.GONE);
            }
        }

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

        rvAllNotifications = view.findViewById(R.id.rv_all_notifications);
        rvAllNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        loadNotifications();

        return view;
    }

    private void showAddNotificationDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_notification, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.et_notification_title);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_notification_type);
        EditText etContent = dialogView.findViewById(R.id.et_notification_content);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_notif);
        Button btnSend = dialogView.findViewById(R.id.btn_send_notif);

        String[] types = {"Học tập", "Thi cử", "Chung"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(typeAdapter);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            String type = spinnerType.getSelectedItem().toString();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ tiêu đề và nội dung!", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

            // 1. Lưu thông báo vào SQLite Database
            ThongBao newNotif = new ThongBao(title, content, currentDate, type);
            dbHelper.insertThongBao(newNotif);

            // 2. Kích hoạt phát Thông báo ngay lập tức trên thanh trạng thái thiết bị
            NotificationHelper.scheduleNotification(getContext(), "📢 " + title, content, System.currentTimeMillis() + 1000, (int) System.currentTimeMillis());

            Toast.makeText(getContext(), "Đăng thông báo thành công!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadNotifications();
        });

        dialog.show();
    }

    private void loadNotifications() {
        if (dbHelper == null) return;

        List<ThongBao> list = dbHelper.getAllThongBao();
        adapter = new ThongBaoAdapter(list, thongBao -> {
            dbHelper.markThongBaoDaDoc(thongBao.getId());
            loadNotifications();
        });
        rvAllNotifications.setAdapter(adapter);
    }
}
