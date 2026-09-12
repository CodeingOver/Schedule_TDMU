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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.DatabaseHelper;
import com.example.schedule_tdmu.LoginActivity;
import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.adapter.ThongBaoAdapter;
import com.example.schedule_tdmu.model.ThongBao;
import com.example.schedule_tdmu.utils.SharedPrefManager;

import java.util.List;

public class NotificationFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private SharedPrefManager prefManager;
    private RecyclerView rvAllNotifications;
    private ThongBaoAdapter adapter;
    private TextView tvProfileName, tvProfileEmail;
    private Button btnLogout;

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

        if (prefManager != null) {
            tvProfileName.setText(prefManager.getStudentName());
            tvProfileEmail.setText(prefManager.getStudentEmail());
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
