package com.example.schedule_tdmu.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.DatabaseHelper;
import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.adapter.LichHocAdapter;
import com.example.schedule_tdmu.adapter.ThongBaoAdapter;
import com.example.schedule_tdmu.model.LichHoc;
import com.example.schedule_tdmu.model.ThongBao;
import com.example.schedule_tdmu.utils.SharedPrefManager;

import java.util.List;

public class HomeFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private SharedPrefManager prefManager;
    private RecyclerView rvTodaySchedule, rvRecentNotifications;
    private LichHocAdapter lichHocAdapter;
    private ThongBaoAdapter thongBaoAdapter;
    private TextView tvGreeting, tvNextClassTitle, tvNextClassDetail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (getContext() != null) {
            dbHelper = new DatabaseHelper(getContext());
            prefManager = new SharedPrefManager(getContext());
        }

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvNextClassTitle = view.findViewById(R.id.tv_next_class_title);
        tvNextClassDetail = view.findViewById(R.id.tv_next_class_detail);

        if (prefManager != null) {
            tvGreeting.setText("Xin chào, " + prefManager.getStudentName() + " 👋");
        }

        rvTodaySchedule = view.findViewById(R.id.rv_today_schedule);
        rvRecentNotifications = view.findViewById(R.id.rv_recent_notifications);

        rvTodaySchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        loadData();

        return view;
    }

    private void loadData() {
        if (dbHelper == null) return;

        // Tải lịch học hôm nay (Thứ 2 = 2)
        List<LichHoc> todayList = dbHelper.getLichHocByThu(2);
        if (!todayList.isEmpty()) {
            LichHoc nextClass = todayList.get(0);
            tvNextClassTitle.setText(nextClass.getTenMon());
            tvNextClassDetail.setText("Tiết " + nextClass.getTietBatDau() + " | Phòng " + nextClass.getPhongHoc());
        } else {
            tvNextClassTitle.setText("Chưa có lịch học tiếp theo");
            tvNextClassDetail.setText("Hãy tận hưởng thời gian nghỉ ngơi nhé!");
        }

        lichHocAdapter = new LichHocAdapter(todayList);
        rvTodaySchedule.setAdapter(lichHocAdapter);

        // Tải thông báo mới nhất
        List<ThongBao> listThongBao = dbHelper.getAllThongBao();
        thongBaoAdapter = new ThongBaoAdapter(listThongBao, thongBao -> {
            dbHelper.markThongBaoDaDoc(thongBao.getId());
            loadData();
        });
        rvRecentNotifications.setAdapter(thongBaoAdapter);
    }
}
