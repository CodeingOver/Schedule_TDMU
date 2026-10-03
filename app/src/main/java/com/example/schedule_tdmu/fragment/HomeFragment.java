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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private SharedPrefManager prefManager;
    private RecyclerView rvTodaySchedule, rvRecentNotifications;
    private LichHocAdapter lichHocAdapter;
    private ThongBaoAdapter thongBaoAdapter;
    private TextView tvGreeting, tvTodayDate, tvSummaryTitle, tvSummaryDetail, tvSummaryBadge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (getContext() != null) {
            dbHelper = new DatabaseHelper(getContext());
            prefManager = new SharedPrefManager(getContext());
        }

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvTodayDate = view.findViewById(R.id.tv_today_date);
        tvSummaryTitle = view.findViewById(R.id.tv_summary_title);
        tvSummaryDetail = view.findViewById(R.id.tv_summary_detail);
        tvSummaryBadge = view.findViewById(R.id.tv_summary_badge);

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

        // Tính toán thứ và ngày hôm nay thực tế
        Calendar calToday = Calendar.getInstance();
        int dayOfWeek = calToday.get(Calendar.DAY_OF_WEEK); // Sunday = 1, Monday = 2
        int thuToday = (dayOfWeek == Calendar.SUNDAY) ? 8 : dayOfWeek;
        Date todayDate = calToday.getTime();

        SimpleDateFormat sdfHeader = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String thuName = (thuToday == 8) ? "Chủ Nhật" : "Thứ " + thuToday;
        String headerDateStr = "Hôm nay, " + thuName + " (" + sdfHeader.format(todayDate) + ")";
        if (tvTodayDate != null) {
            tvTodayDate.setText(headerDateStr);
        }

        // Tải danh sách môn học theo Thứ và lọc theo Khoảng Ngày Học (Cột K "Thời gian học" trong Excel)
        List<LichHoc> allThuList = dbHelper.getLichHocByThu(thuToday);
        List<LichHoc> todayActiveList = new ArrayList<>();

        for (LichHoc lh : allThuList) {
            if (lh.isClassActiveOnDate(todayDate)) {
                todayActiveList.add(lh);
            }
        }

        // Cập nhật Thẻ Tổng Quan Lịch Học Hôm Nay
        int totalToday = todayActiveList.size();
        if (tvSummaryTitle != null && tvSummaryDetail != null && tvSummaryBadge != null) {
            if (totalToday > 0) {
                tvSummaryTitle.setText("Lịch học hôm nay (" + totalToday + " môn)");
                tvSummaryDetail.setText("Kiểm tra thông tin phòng học & giờ học ở bên dưới");
                tvSummaryBadge.setText(totalToday + " Môn");
            } else {
                tvSummaryTitle.setText("Hôm nay không có lịch học");
                tvSummaryDetail.setText("Hãy tận hưởng thời gian nghỉ ngơi nhé! 🎉");
                tvSummaryBadge.setText("Nghỉ học");
            }
        }

        lichHocAdapter = new LichHocAdapter(todayActiveList, todayDate, null);
        lichHocAdapter.setShowStatusTags(true);
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
