package com.example.schedule_tdmu.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.DatabaseHelper;
import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.adapter.MonHocAdapter;
import com.example.schedule_tdmu.model.LichHoc;
import com.example.schedule_tdmu.model.MonHoc;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SubjectsFragment extends Fragment implements MonHocAdapter.OnMonHocActionListener {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_ACTIVE = 1;
    private static final int FILTER_GRADED = 2;

    private TextView tvTongTinChi, tvTongSoMon, tvChuyenCanChung, tvTrangThaiChuyenCan;
    private TextView pillSubjectAll, pillSubjectActive, pillSubjectGraded;
    private MaterialButton btnThemMonHoc;
    private RecyclerView rvMonHoc;
    private LinearLayout layoutEmptySubjects;

    private DatabaseHelper dbHelper;
    private MonHocAdapter adapter;
    private int currentFilter = FILTER_ALL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subjects, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        tvTongTinChi = view.findViewById(R.id.tvTongTinChi);
        tvTongSoMon = view.findViewById(R.id.tvTongSoMon);
        tvChuyenCanChung = view.findViewById(R.id.tvChuyenCanChung);
        tvTrangThaiChuyenCan = view.findViewById(R.id.tvTrangThaiChuyenCan);

        pillSubjectAll = view.findViewById(R.id.pillSubjectAll);
        pillSubjectActive = view.findViewById(R.id.pillSubjectActive);
        pillSubjectGraded = view.findViewById(R.id.pillSubjectGraded);

        btnThemMonHoc = view.findViewById(R.id.btnThemMonHoc);
        rvMonHoc = view.findViewById(R.id.rvMonHoc);
        layoutEmptySubjects = view.findViewById(R.id.layoutEmptySubjects);

        rvMonHoc.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MonHocAdapter(requireContext(), new ArrayList<>(), new HashMap<>(), this);
        rvMonHoc.setAdapter(adapter);

        pillSubjectAll.setOnClickListener(v -> applyFilter(FILTER_ALL));
        pillSubjectActive.setOnClickListener(v -> applyFilter(FILTER_ACTIVE));
        pillSubjectGraded.setOnClickListener(v -> applyFilter(FILTER_GRADED));

        btnThemMonHoc.setOnClickListener(v -> showAddOrEditMonHocDialog(null));

        loadSubjectsData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSubjectsData();
    }

    private void applyFilter(int filter) {
        currentFilter = filter;
        updateFilterPillStyles();
        loadSubjectsData();
    }

    private void updateFilterPillStyles() {
        setPillStyle(pillSubjectAll, currentFilter == FILTER_ALL);
        setPillStyle(pillSubjectActive, currentFilter == FILTER_ACTIVE);
        setPillStyle(pillSubjectGraded, currentFilter == FILTER_GRADED);
    }

    private void setPillStyle(TextView pill, boolean active) {
        if (active) {
            pill.setBackgroundResource(R.drawable.bg_pill_primary);
            pill.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            pill.setBackgroundResource(R.drawable.bg_pill_surface_high);
            pill.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        }
    }

    private void loadSubjectsData() {
        List<MonHoc> allSubjects = dbHelper.getAllMonHoc();
        List<LichHoc> allLichHoc = dbHelper.getAllLichHocWithMonHoc();

        Map<Integer, String> scheduleMap = new HashMap<>();
        for (LichHoc lh : allLichHoc) {
            if (!scheduleMap.containsKey(lh.getMonHocId())) {
                String thuText = lh.getThu() == 8 ? "Chủ nhật" : ("Thứ " + lh.getThu());
                int tietKetThuc = lh.getTietBatDau() + lh.getSoTiet() - 1;
                String summary = "Phòng " + lh.getPhongHoc() + " • " + thuText
                        + " (Tiết " + lh.getTietBatDau() + "-" + tietKetThuc + ")";
                scheduleMap.put(lh.getMonHocId(), summary);
            }
        }

        int totalCredits = 0;
        int totalAttendanceSum = 0;
        for (MonHoc mh : allSubjects) {
            totalCredits += mh.getSoTinChi();
            totalAttendanceSum += mh.getTiLeChuyenCan();
        }

        tvTongTinChi.setText(totalCredits + " TC");
        tvTongSoMon.setText(allSubjects.isEmpty() ? "Chưa có học phần" : (allSubjects.size() + " học phần trong kỳ"));

        if (allSubjects.isEmpty()) {
            tvChuyenCanChung.setText("0%");
            tvTrangThaiChuyenCan.setText("Chưa có dữ liệu");
            tvTrangThaiChuyenCan.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        } else {
            float avgAttendance = totalAttendanceSum * 1f / allSubjects.size();
            tvChuyenCanChung.setText(String.format(Locale.US, "%.1f%%", avgAttendance));
            if (avgAttendance >= 80f) {
                tvTrangThaiChuyenCan.setText("Đạt điều kiện dự thi");
                tvTrangThaiChuyenCan.setTextColor(ContextCompat.getColor(requireContext(), R.color.tertiary_container));
            } else {
                tvTrangThaiChuyenCan.setText("Cần chú ý chuyên cần");
                tvTrangThaiChuyenCan.setTextColor(ContextCompat.getColor(requireContext(), R.color.error));
            }
        }

        List<MonHoc> filteredList = new ArrayList<>();
        for (MonHoc mh : allSubjects) {
            if (currentFilter == FILTER_ALL) {
                filteredList.add(mh);
            } else if (currentFilter == FILTER_ACTIVE) {
                if (mh.getSoTuanDaHoc() < mh.getTongSoTuan()) {
                    filteredList.add(mh);
                }
            } else if (currentFilter == FILTER_GRADED) {
                if (mh.hasDiemQuaTrinh()) {
                    filteredList.add(mh);
                }
            }
        }

        adapter.updateData(filteredList, scheduleMap);

        if (filteredList.isEmpty()) {
            rvMonHoc.setVisibility(View.GONE);
            layoutEmptySubjects.setVisibility(View.VISIBLE);
        } else {
            rvMonHoc.setVisibility(View.VISIBLE);
            layoutEmptySubjects.setVisibility(View.GONE);
        }
    }

    private void showAddOrEditMonHocDialog(@Nullable MonHoc existing) {
        boolean isEdit = existing != null;

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (18 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad / 2, pad, 0);

        EditText edtMaMon = createField(container, "Mã học phần (VD: LING189)",
                isEdit ? existing.getMaMon() : "", InputType.TYPE_CLASS_TEXT);
        EditText edtTenMon = createField(container, "Tên học phần",
                isEdit ? existing.getTenMon() : "", InputType.TYPE_CLASS_TEXT);
        EditText edtLopHoc = createField(container, "Mã lớp (VD: D22CNTT01)",
                isEdit ? existing.getLopHoc() : "D22CNTT01", InputType.TYPE_CLASS_TEXT);
        EditText edtGiangVien = createField(container, "Giảng viên phụ trách",
                isEdit ? existing.getGiangVien() : "", InputType.TYPE_CLASS_TEXT);
        EditText edtSoTinChi = createField(container, "Số tín chỉ (1 - 6)",
                isEdit ? String.valueOf(existing.getSoTinChi()) : "3", InputType.TYPE_CLASS_NUMBER);
        EditText edtSoTuanDaHoc = createField(container, "Số tuần đã học (trên 15 tuần)",
                isEdit ? String.valueOf(existing.getSoTuanDaHoc()) : "8", InputType.TYPE_CLASS_NUMBER);
        EditText edtSoBuoiVang = createField(container, "Số buổi vắng",
                isEdit ? String.valueOf(existing.getSoBuoiVang()) : "0", InputType.TYPE_CLASS_NUMBER);
        EditText edtDiemQT = createField(container, "Điểm quá trình (để trống nếu chưa có)",
                (isEdit && existing.hasDiemQuaTrinh()) ? String.valueOf(existing.getDiemQuaTrinh()) : "",
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? "Cập nhật học phần" : "Thêm môn học mới")
                .setView(container)
                .setPositiveButton(isEdit ? "Lưu thay đổi" : "Thêm môn", (dialog, which) -> {
                    String maMon = edtMaMon.getText().toString().trim();
                    String tenMon = edtTenMon.getText().toString().trim();
                    String lopHoc = edtLopHoc.getText().toString().trim();
                    String giangVien = edtGiangVien.getText().toString().trim();

                    if (maMon.isEmpty() || tenMon.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập mã môn và tên môn học", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int soTinChi = parseIntSafe(edtSoTinChi.getText().toString(), 3);
                    int soTuanDaHoc = parseIntSafe(edtSoTuanDaHoc.getText().toString(), 8);
                    int soBuoiVang = parseIntSafe(edtSoBuoiVang.getText().toString(), 0);
                    float diemQT = parseFloatSafe(edtDiemQT.getText().toString(), -1f);

                    if (isEdit) {
                        existing.setMaMon(maMon);
                        existing.setTenMon(tenMon);
                        existing.setLopHoc(lopHoc.isEmpty() ? "D22CNTT01" : lopHoc);
                        existing.setGiangVien(giangVien.isEmpty() ? "Giảng viên TDMU" : giangVien);
                        existing.setSoTinChi(Math.max(1, soTinChi));
                        existing.setSoTuanDaHoc(Math.min(existing.getTongSoTuan(), Math.max(0, soTuanDaHoc)));
                        existing.setSoBuoiVang(Math.max(0, soBuoiVang));
                        existing.setDiemQuaTrinh(diemQT);
                        dbHelper.updateMonHoc(existing);
                        Toast.makeText(requireContext(), "Đã cập nhật học phần " + maMon, Toast.LENGTH_SHORT).show();
                    } else {
                        MonHoc newMon = new MonHoc(
                                0,
                                maMon,
                                tenMon,
                                Math.max(1, soTinChi),
                                "#3525CD",
                                lopHoc.isEmpty() ? "D22CNTT01" : lopHoc,
                                giangVien.isEmpty() ? "Giảng viên TDMU" : giangVien,
                                15,
                                Math.min(15, Math.max(0, soTuanDaHoc)),
                                Math.max(0, soBuoiVang),
                                diemQT,
                                "Đang diễn ra"
                        );
                        dbHelper.insertMonHoc(newMon);
                        Toast.makeText(requireContext(), "Đã thêm môn học " + tenMon, Toast.LENGTH_SHORT).show();
                    }
                    loadSubjectsData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private EditText createField(LinearLayout parent, String hint, String value, int inputType) {
        EditText editText = new EditText(requireContext());
        editText.setHint(hint);
        editText.setText(value);
        editText.setInputType(inputType);
        editText.setTextSize(14f);
        parent.addView(editText);
        return editText;
    }

    private int parseIntSafe(String val, int fallback) {
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private float parseFloatSafe(String val, float fallback) {
        if (val == null || val.trim().isEmpty()) return fallback;
        try {
            return Float.parseFloat(val.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    @Override
    public void onEditMonHoc(MonHoc monHoc) {
        showAddOrEditMonHocDialog(monHoc);
    }

    @Override
    public void onDeleteMonHoc(MonHoc monHoc) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa học phần")
                .setMessage("Bạn có chắc muốn xóa học phần " + monHoc.getTenMon() + " (" + monHoc.getMaMon() + ")? Các buổi học liên quan cũng sẽ bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteMonHoc(monHoc.getId());
                    loadSubjectsData();
                    Toast.makeText(requireContext(), "Đã xóa học phần " + monHoc.getMaMon(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
