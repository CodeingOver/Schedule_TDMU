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
import com.example.schedule_tdmu.model.LichThi;
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
            lichHocAdapter = new LichHocAdapter(listLichHoc, new LichHocAdapter.OnLichHocClickListener() {
                @Override
                public void onItemClick(LichHoc lichHoc) {
                    showDeleteLichHocDialog(lichHoc);
                }

                @Override
                public void onItemLongClick(LichHoc lichHoc) {
                    showDeleteLichHocDialog(lichHoc);
                }
            });
            rvScheduleList.setAdapter(lichHocAdapter);
        } else {
            List<LichThi> listLichThi = dbHelper.getAllLichThiWithMonHoc();
            lichThiAdapter = new LichThiAdapter(listLichThi, new LichThiAdapter.OnLichThiClickListener() {
                @Override
                public void onItemClick(LichThi lichThi) {
                    showDeleteLichThiDialog(lichThi);
                }

                @Override
                public void onItemLongClick(LichThi lichThi) {
                    showDeleteLichThiDialog(lichThi);
                }
            });
            rvScheduleList.setAdapter(lichThiAdapter);
        }
    }

    private void showDeleteLichHocDialog(LichHoc item) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("🗑️ Xóa Lịch Học")
                .setMessage("Bạn có chắc chắn muốn xóa môn [" + item.getTenMon() + "] khỏi thời khóa biểu không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteLichHoc(item.getId());
                    Toast.makeText(getContext(), "Đã xóa môn " + item.getTenMon() + "!", Toast.LENGTH_SHORT).show();
                    loadScheduleData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteLichThiDialog(LichThi item) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("🗑️ Xóa Lịch Thi")
                .setMessage("Bạn có chắc chắn muốn xóa lịch thi môn [" + item.getTenMon() + "] không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    SQLiteOpenHelper_deleteLichThi(item.getId());
                    Toast.makeText(getContext(), "Đã xóa lịch thi môn " + item.getTenMon() + "!", Toast.LENGTH_SHORT).show();
                    loadScheduleData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void SQLiteOpenHelper_deleteLichThi(int id) {
        if (dbHelper != null) {
            dbHelper.getWritableDatabase().delete(DatabaseHelper.TABLE_LICH_THI, DatabaseHelper.COL_LICH_THI_ID + " = ?", new String[]{String.valueOf(id)});
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
