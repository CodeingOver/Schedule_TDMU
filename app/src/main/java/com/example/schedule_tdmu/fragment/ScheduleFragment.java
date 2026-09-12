package com.example.schedule_tdmu.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.DatabaseHelper;
import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.adapter.LichHocAdapter;
import com.example.schedule_tdmu.adapter.LichThiAdapter;
import com.example.schedule_tdmu.model.LichHoc;
import com.example.schedule_tdmu.model.MonHoc;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class ScheduleFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView rvScheduleList;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddSchedule;
    private LichHocAdapter lichHocAdapter;
    private LichThiAdapter lichThiAdapter;
    private int currentTab = 0; // 0: Lịch học, 1: Lịch thi

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        if (getContext() != null) {
            dbHelper = new DatabaseHelper(getContext());
        }

        tabLayout = view.findViewById(R.id.tab_layout);
        rvScheduleList = view.findViewById(R.id.rv_schedule_list);
        fabAddSchedule = view.findViewById(R.id.fab_add_schedule);

        rvScheduleList.setLayoutManager(new LinearLayoutManager(getContext()));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadScheduleData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        fabAddSchedule.setOnClickListener(v -> showAddScheduleDialog());

        loadScheduleData();

        return view;
    }

    private void loadScheduleData() {
        if (dbHelper == null) return;

        if (currentTab == 0) {
            List<LichHoc> listLichHoc = dbHelper.getAllLichHocWithMonHoc();
            lichHocAdapter = new LichHocAdapter(listLichHoc);
            rvScheduleList.setAdapter(lichHocAdapter);
        } else {
            lichThiAdapter = new LichThiAdapter(dbHelper.getAllLichThiWithMonHoc());
            rvScheduleList.setAdapter(lichThiAdapter);
        }
    }

    private void showAddScheduleDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_schedule, null);
        builder.setView(dialogView);

        EditText etMaMon = dialogView.findViewById(R.id.et_ma_mon);
        EditText etTenMon = dialogView.findViewById(R.id.et_ten_mon);
        Spinner spinnerThu = dialogView.findViewById(R.id.spinner_thu);
        EditText etTietBatDau = dialogView.findViewById(R.id.et_tiet_bat_dau);
        EditText etSoTiet = dialogView.findViewById(R.id.et_so_tiet);
        EditText etPhongHoc = dialogView.findViewById(R.id.et_phong_hoc);
        EditText etGiangVien = dialogView.findViewById(R.id.et_giang_vien);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        String[] dsThu = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, dsThu);
        spinnerThu.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String maMon = etMaMon.getText().toString().trim();
            String tenMon = etTenMon.getText().toString().trim();
            String tietBDStr = etTietBatDau.getText().toString().trim();
            String soTietStr = etSoTiet.getText().toString().trim();

            if (maMon.isEmpty() || tenMon.isEmpty() || tietBDStr.isEmpty() || soTietStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            int thu = spinnerThu.getSelectedItemPosition() + 2; // 0 -> Thứ 2
            int tietBD = Integer.parseInt(tietBDStr);
            int soTiet = Integer.parseInt(soTietStr);

            // Thêm môn học
            long monHocId = dbHelper.insertMonHoc(new MonHoc(maMon, tenMon, 3, "#1976D2"));
            if (monHocId == -1) {
                // Đã tồn tại mã môn, lấy danh sách môn học chọn id tương ứng
                List<MonHoc> allMon = dbHelper.getAllMonHoc();
                for (MonHoc mh : allMon) {
                    if (mh.getMaMon().equalsIgnoreCase(maMon)) {
                        monHocId = mh.getId();
                        break;
                    }
                }
            }

            // Thêm lịch học
            LichHoc lh = new LichHoc((int) monHocId, thu, tietBD, soTiet, etPhongHoc.getText().toString().trim(),
                    etGiangVien.getText().toString().trim(), 1, "2024-2025", "1-15");
            dbHelper.insertLichHoc(lh);

            Toast.makeText(getContext(), "Đã thêm lịch học thành công!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadScheduleData();
        });

        dialog.show();
    }
}
