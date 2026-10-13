package com.example.schedule_tdmu.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.DatabaseHelper;
import com.example.schedule_tdmu.MainActivity;
import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.adapter.LichHocAdapter;
import com.example.schedule_tdmu.adapter.LichThiAdapter;
import com.example.schedule_tdmu.adapter.WeekStripAdapter;
import com.example.schedule_tdmu.model.LichHoc;
import com.example.schedule_tdmu.model.LichThi;
import com.example.schedule_tdmu.model.MonHoc;
import com.example.schedule_tdmu.model.ThongBao;
import com.example.schedule_tdmu.utils.SharedPrefManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ScheduleFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private SharedPrefManager prefManager;
    private RecyclerView rvScheduleList, rvWeekStrip;
    private WeekStripAdapter weekStripAdapter;
    private FloatingActionButton fabAddSchedule;
    private Button btnImportExcel, btnScanScheduleTop;
    private Spinner spinnerWeek;
    private ImageButton btnPrevWeek, btnNextWeek;

    // Hero Card Views
    private TextView tvHeroGreeting, tvHeroStatusPill, tvHeroCourseCode, tvHeroCourseTitle;
    private TextView tvHeroTime, tvHeroCountdown, tvHeroRoom, tvHeroLecturer;
    private Button btnHeroDirections, btnHeroMaterials;

    // Mode Switcher Views (0: Theo ngày, 1: Lưới tuần, 2: Lịch thi)
    private LinearLayout btnModeDay, btnModeWeek, btnModeExam;
    private ImageView imgModeDay, imgModeWeek, imgModeExam;
    private TextView tvModeDay, tvModeWeek, tvModeExam;

    // Timeline & Bento Views
    private TextView tvTimelineDayTitle, tvTimelineTodayBadge, tvTimelineTotalCount, tvCurrentTimeLabel;
    private View layoutCurrentTimeIndicator, layoutEmptySchedule;
    private TextView tvBentoAttendancePct, tvBentoAttendanceCount, tvBentoExamCount;
    private ProgressBar pbBentoAttendance;
    private TextView tvTickerTitle, tvTickerSubtitle;

    // Thanh Thao tác Chọn Hàng Loạt
    private LinearLayout layoutWeekBar, layoutSelectionBar;
    private TextView tvSelectedCount;
    private Button btnSelectAll, btnDeleteSelected;
    private ImageButton btnCancelSelection;

    private LichHocAdapter lichHocAdapter;
    private LichThiAdapter lichThiAdapter;
    private int currentTab = 0; // 0: Lịch học, 1: Lịch thi
    private int viewMode = 0;   // 0: Theo ngày, 1: Lưới tuần, 2: Lịch thi
    private int selectedWeekIndex = 0;
    private int selectedThuFilter = 2;

    public static class ParsedSubject {
        public String maMon;
        public String tenMon;
        public int thu;
        public int tietBatDau;
        public int soTiet;
        public String phongHoc;
        public String giangVien;
        public String tuanHoc;

        public ParsedSubject(String maMon, String tenMon, int thu, int tietBatDau, int soTiet, String phongHoc, String giangVien, String tuanHoc) {
            this.maMon = maMon;
            this.tenMon = tenMon;
            this.thu = thu;
            this.tietBatDau = tietBatDau;
            this.soTiet = soTiet;
            this.phongHoc = phongHoc;
            this.giangVien = giangVien;
            this.tuanHoc = tuanHoc;
        }
    }

    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processExcelCsvFileImport(uri);
                }
            }
    );

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processImageOCRInBackground(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        if (getContext() != null) {
            dbHelper = new DatabaseHelper(getContext());
            prefManager = new SharedPrefManager(getContext());
        }

        // Mặc định chọn thứ hôm nay
        Calendar calNow = Calendar.getInstance();
        int dow = calNow.get(Calendar.DAY_OF_WEEK);
        selectedThuFilter = (dow == Calendar.SUNDAY) ? 8 : dow;

        bindViews(view);

        rvScheduleList.setLayoutManager(new LinearLayoutManager(getContext()));
        if (rvWeekStrip != null) {
            rvWeekStrip.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        }

        selectedWeekIndex = calculateCurrentWeekIndex();
        setupWeekSpinner();
        setupSelectionBarActions();
        setupModeSwitcherActions();

        if (btnImportExcel != null) {
            btnImportExcel.setOnClickListener(v -> filePickerLauncher.launch("*/*"));
        }

        if (btnScanScheduleTop != null) {
            btnScanScheduleTop.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }

        if (fabAddSchedule != null) {
            fabAddSchedule.setOnClickListener(v -> showAddOptionsDialog());
        }

        loadScheduleData();

        return view;
    }

    private void bindViews(View view) {
        rvScheduleList = view.findViewById(R.id.rv_schedule_list);
        rvWeekStrip = view.findViewById(R.id.rv_week_strip);
        fabAddSchedule = view.findViewById(R.id.fab_add_schedule);
        btnImportExcel = view.findViewById(R.id.btn_import_excel);
        btnScanScheduleTop = view.findViewById(R.id.btn_scan_schedule_top);

        layoutWeekBar = view.findViewById(R.id.layout_week_bar);
        spinnerWeek = view.findViewById(R.id.spinner_week);
        btnPrevWeek = view.findViewById(R.id.btn_prev_week);
        btnNextWeek = view.findViewById(R.id.btn_next_week);

        layoutSelectionBar = view.findViewById(R.id.layout_selection_bar);
        tvSelectedCount = view.findViewById(R.id.tv_selected_count);
        btnSelectAll = view.findViewById(R.id.btn_select_all);
        btnDeleteSelected = view.findViewById(R.id.btn_delete_selected);
        btnCancelSelection = view.findViewById(R.id.btn_cancel_selection);

        // Hero Card
        tvHeroGreeting = view.findViewById(R.id.tv_hero_greeting);
        tvHeroStatusPill = view.findViewById(R.id.tv_hero_status_pill);
        tvHeroCourseCode = view.findViewById(R.id.tv_hero_course_code);
        tvHeroCourseTitle = view.findViewById(R.id.tv_hero_course_title);
        tvHeroTime = view.findViewById(R.id.tv_hero_time);
        tvHeroCountdown = view.findViewById(R.id.tv_hero_countdown);
        tvHeroRoom = view.findViewById(R.id.tv_hero_room);
        tvHeroLecturer = view.findViewById(R.id.tv_hero_lecturer);
        btnHeroDirections = view.findViewById(R.id.btn_hero_directions);
        btnHeroMaterials = view.findViewById(R.id.btn_hero_materials);

        // Mode Switcher
        btnModeDay = view.findViewById(R.id.btn_mode_day);
        btnModeWeek = view.findViewById(R.id.btn_mode_week);
        btnModeExam = view.findViewById(R.id.btn_mode_exam);
        imgModeDay = view.findViewById(R.id.img_mode_day);
        imgModeWeek = view.findViewById(R.id.img_mode_week);
        imgModeExam = view.findViewById(R.id.img_mode_exam);
        tvModeDay = view.findViewById(R.id.tv_mode_day);
        tvModeWeek = view.findViewById(R.id.tv_mode_week);
        tvModeExam = view.findViewById(R.id.tv_mode_exam);

        // Timeline & Bento
        tvTimelineDayTitle = view.findViewById(R.id.tv_timeline_day_title);
        tvTimelineTodayBadge = view.findViewById(R.id.tv_timeline_today_badge);
        tvTimelineTotalCount = view.findViewById(R.id.tv_timeline_total_count);
        tvCurrentTimeLabel = view.findViewById(R.id.tv_current_time_label);
        layoutCurrentTimeIndicator = view.findViewById(R.id.layout_current_time_indicator);
        layoutEmptySchedule = view.findViewById(R.id.layout_empty_schedule);

        tvBentoAttendancePct = view.findViewById(R.id.tv_bento_attendance_pct);
        tvBentoAttendanceCount = view.findViewById(R.id.tv_bento_attendance_count);
        pbBentoAttendance = view.findViewById(R.id.pb_bento_attendance);
        tvBentoExamCount = view.findViewById(R.id.tv_bento_exam_count);
        tvTickerTitle = view.findViewById(R.id.tv_ticker_title);
        tvTickerSubtitle = view.findViewById(R.id.tv_ticker_subtitle);

        View cardBentoAttendance = view.findViewById(R.id.card_bento_attendance);
        if (cardBentoAttendance != null) {
            cardBentoAttendance.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToTab(R.id.nav_subjects);
                }
            });
        }

        View cardBentoExam = view.findViewById(R.id.card_bento_exam);
        if (cardBentoExam != null) {
            cardBentoExam.setOnClickListener(v -> switchViewMode(2));
        }

        View cardCampusTicker = view.findViewById(R.id.card_campus_ticker);
        if (cardCampusTicker != null) {
            cardCampusTicker.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToTab(R.id.nav_notifications);
                }
            });
        }
    }

    private void setupModeSwitcherActions() {
        if (btnModeDay != null) {
            btnModeDay.setOnClickListener(v -> {
                Calendar cal = Calendar.getInstance();
                int dow = cal.get(Calendar.DAY_OF_WEEK);
                if (selectedThuFilter == -1) {
                    selectedThuFilter = (dow == Calendar.SUNDAY) ? 8 : dow;
                }
                switchViewMode(0);
            });
        }
        if (btnModeWeek != null) {
            btnModeWeek.setOnClickListener(v -> {
                selectedThuFilter = -1;
                switchViewMode(1);
            });
        }
        if (btnModeExam != null) {
            btnModeExam.setOnClickListener(v -> switchViewMode(2));
        }
    }

    private void switchViewMode(int mode) {
        this.viewMode = mode;
        this.currentTab = (mode == 2) ? 1 : 0;
        exitSelectionMode();
        updateModeSwitcherUI();
        loadScheduleData();
    }

    private void updateModeSwitcherUI() {
        int colorPrimary = Color.parseColor("#3525CD");
        int colorSecondaryText = Color.parseColor("#464555");

        if (btnModeDay != null && tvModeDay != null && imgModeDay != null) {
            if (viewMode == 0) {
                btnModeDay.setBackgroundResource(R.drawable.bg_segmented_active);
                tvModeDay.setTextColor(colorPrimary);
                imgModeDay.setColorFilter(colorPrimary);
            } else {
                btnModeDay.setBackgroundColor(Color.TRANSPARENT);
                tvModeDay.setTextColor(colorSecondaryText);
                imgModeDay.setColorFilter(colorSecondaryText);
            }
        }

        if (btnModeWeek != null && tvModeWeek != null && imgModeWeek != null) {
            if (viewMode == 1) {
                btnModeWeek.setBackgroundResource(R.drawable.bg_segmented_active);
                tvModeWeek.setTextColor(colorPrimary);
                imgModeWeek.setColorFilter(colorPrimary);
            } else {
                btnModeWeek.setBackgroundColor(Color.TRANSPARENT);
                tvModeWeek.setTextColor(colorSecondaryText);
                imgModeWeek.setColorFilter(colorSecondaryText);
            }
        }

        if (btnModeExam != null && tvModeExam != null && imgModeExam != null) {
            if (viewMode == 2) {
                btnModeExam.setBackgroundResource(R.drawable.bg_pill_primary);
                tvModeExam.setTextColor(Color.WHITE);
                imgModeExam.setColorFilter(Color.WHITE);
            } else {
                btnModeExam.setBackgroundResource(R.drawable.bg_pill_secondary);
                tvModeExam.setTextColor(colorPrimary);
                imgModeExam.setColorFilter(colorPrimary);
            }
        }
    }

    private void updateHeroAndBentoCards() {
        if (dbHelper == null) return;

        // 1. Lời chào theo buổi trong ngày (không chứa emoji)
        Calendar calNow = Calendar.getInstance();
        int hour = calNow.get(Calendar.HOUR_OF_DAY);
        String greetingPrefix = "Chào buổi sáng";
        if (hour >= 12 && hour < 18) {
            greetingPrefix = "Chào buổi chiều";
        } else if (hour >= 18) {
            greetingPrefix = "Chào buổi tối";
        }

        String fullName = prefManager != null ? prefManager.getStudentName() : "Sinh viên TDMU";
        String shortName = fullName;
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            shortName = parts[parts.length - 2] + " " + parts[parts.length - 1];
        }
        if (tvHeroGreeting != null) {
            tvHeroGreeting.setText(greetingPrefix + ", " + shortName + "!");
        }

        // 2. Tìm tiết học đang diễn ra hoặc kế tiếp (lọc chính xác theo ngày thực tế)
        int dow = calNow.get(Calendar.DAY_OF_WEEK);
        int todayThu = (dow == Calendar.SUNDAY) ? 8 : dow;
        int nowMinutes = hour * 60 + calNow.get(Calendar.MINUTE);
        Date todayDate = calNow.getTime();

        List<LichHoc> activeTodayClasses = new ArrayList<>();
        for (LichHoc lh : dbHelper.getLichHocByThu(todayThu)) {
            if (lh.occursOnDate(todayDate)) {
                activeTodayClasses.add(lh);
            }
        }

        LichHoc nextClass = null;
        String heroStatus = null;
        String heroCountdownText = null;

        // 2.1 Kiểm tra xem có lớp nào đang trong giờ học hôm nay không
        for (LichHoc lh : activeTodayClasses) {
            int tietStart = lh.getTietBatDau();
            int tietEnd = tietStart + lh.getSoTiet() - 1;
            int startMin = LichHocAdapter.getPeriodStartMinutes(tietStart);
            int endMin = LichHocAdapter.getPeriodEndMinutes(tietEnd);
            if (nowMinutes >= startMin && nowMinutes <= endMin) {
                nextClass = lh;
                heroStatus = "Đang trong giờ học";
                heroCountdownText = "Đang học";
                break;
            }
        }

        // 2.2 Nếu không có lớp đang học, tìm lớp kế tiếp hôm nay chưa bắt đầu
        if (nextClass == null) {
            for (LichHoc lh : activeTodayClasses) {
                int tietStart = lh.getTietBatDau();
                int startMin = LichHocAdapter.getPeriodStartMinutes(tietStart);
                if (nowMinutes < startMin) {
                    nextClass = lh;
                    heroStatus = "Tiết học kế tiếp";
                    int diff = startMin - nowMinutes;
                    if (diff < 60) {
                        heroCountdownText = "Còn " + diff + " phút";
                    } else {
                        heroCountdownText = "Còn " + (diff / 60) + "h " + (diff % 60) + "p";
                    }
                    break;
                }
            }
        }

        // 2.3 Nếu hôm nay không còn lớp nào (đã học xong hoặc hôm nay nghỉ), tìm môn ở các ngày tiếp theo (tối đa 14 ngày)
        if (nextClass == null) {
            Calendar calFuture = Calendar.getInstance();
            calFuture.setTime(todayDate);
            for (int dayOffset = 1; dayOffset <= 14; dayOffset++) {
                calFuture.add(Calendar.DAY_OF_YEAR, 1);
                Date futureDate = calFuture.getTime();
                int futureDow = calFuture.get(Calendar.DAY_OF_WEEK);
                int futureThu = (futureDow == Calendar.SUNDAY) ? 8 : futureDow;

                List<LichHoc> futureList = dbHelper.getLichHocByThu(futureThu);
                for (LichHoc flh : futureList) {
                    if (flh.occursOnDate(futureDate)) {
                        nextClass = flh;
                        SimpleDateFormat sdfDayMonth = new SimpleDateFormat("dd/MM", Locale.getDefault());
                        String thuName = (futureThu == 8) ? "Chủ Nhật" : ("Thứ " + futureThu);
                        heroStatus = "Lịch học tiêu điểm";
                        heroCountdownText = thuName + " (" + sdfDayMonth.format(futureDate) + ")";
                        break;
                    }
                }
                if (nextClass != null) {
                    break;
                }
            }
        }

        if (nextClass != null) {
            final LichHoc heroItem = nextClass;
            int tietStart = heroItem.getTietBatDau();
            int tietEnd = tietStart + heroItem.getSoTiet() - 1;
            String timeRange = LichHocAdapter.getTdmuPeriodTime(tietStart, tietEnd);

            if (tvHeroCourseCode != null) {
                tvHeroCourseCode.setText("Mã môn: " + heroItem.getMaMon() + " • Lớp chính quy");
            }
            if (tvHeroCourseTitle != null) {
                tvHeroCourseTitle.setText(heroItem.getTenMon());
            }
            if (tvHeroTime != null) {
                tvHeroTime.setText(timeRange + " (Tiết " + tietStart + " - " + tietEnd + ")");
            }
            if (tvHeroRoom != null) {
                tvHeroRoom.setText("Phòng " + heroItem.getPhongHoc() + " (Khu giảng đường TDMU)");
            }
            if (tvHeroLecturer != null) {
                tvHeroLecturer.setText(heroItem.getGiangVien());
            }

            if (tvHeroCountdown != null && tvHeroStatusPill != null) {
                tvHeroStatusPill.setText(heroStatus != null ? heroStatus : "Lịch học tiêu điểm");
                tvHeroCountdown.setText(heroCountdownText != null ? heroCountdownText : "Sắp tới");
            }

            if (btnHeroDirections != null) {
                btnHeroDirections.setOnClickListener(v -> {
                    String roomGuide = buildTdmuRoomGuide(heroItem.getPhongHoc());
                    new AlertDialog.Builder(getContext())
                            .setTitle("Chỉ dẫn phòng học: " + heroItem.getPhongHoc())
                            .setMessage("Học phần: " + heroItem.getTenMon() + " (" + heroItem.getMaMon() + ")\n" +
                                    "Khung giờ: " + timeRange + " (Tiết " + tietStart + " - " + tietEnd + ")\n" +
                                    "Giảng viên: " + heroItem.getGiangVien() + "\n\n" +
                                    roomGuide)
                            .setPositiveButton("Đã hiểu", null)
                            .show();
                });
            }

            if (btnHeroMaterials != null) {
                btnHeroMaterials.setOnClickListener(v -> showLichHocDetailDialog(heroItem));
            }
        } else {
            if (tvHeroStatusPill != null) tvHeroStatusPill.setText("Chưa có lịch");
            if (tvHeroCourseCode != null) tvHeroCourseCode.setText("Hôm nay không có lịch học");
            if (tvHeroCourseTitle != null) tvHeroCourseTitle.setText("Chưa có tiết học kế tiếp");
            if (tvHeroTime != null) tvHeroTime.setText("Nghỉ học hoặc chưa đến tuần học");
            if (tvHeroCountdown != null) tvHeroCountdown.setText("Trống");
            if (tvHeroRoom != null) tvHeroRoom.setText("Chưa có phòng học");
            if (tvHeroLecturer != null) tvHeroLecturer.setText("Chưa có giảng viên");
            if (btnHeroDirections != null) {
                btnHeroDirections.setOnClickListener(v -> showAddOptionsDialog());
            }
            if (btnHeroMaterials != null) {
                btnHeroMaterials.setOnClickListener(v -> showAddOptionsDialog());
            }
        }

        // 3. Cập nhật giờ hiện tại trên vạch đỏ Timeline
        if (tvCurrentTimeLabel != null) {
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvCurrentTimeLabel.setText(sdfTime.format(calNow.getTime()) + " Hiện tại");
        }

        // 4. Cập nhật Bento Chuyên cần & Lịch thi
        List<MonHoc> listMon = dbHelper.getAllMonHoc();
        int totalWeeksStudied = 0;
        int totalAbsences = 0;
        for (MonHoc mh : listMon) {
            totalWeeksStudied += mh.getSoTuanDaHoc();
            totalAbsences += mh.getSoBuoiVang();
        }
        int attendedSessions = Math.max(0, totalWeeksStudied - totalAbsences);
        int pct = totalWeeksStudied > 0 ? Math.min(100, Math.round((attendedSessions * 100f) / totalWeeksStudied)) : 0;

        if (tvBentoAttendancePct != null) tvBentoAttendancePct.setText(pct + "%");
        if (tvBentoAttendanceCount != null) {
            tvBentoAttendanceCount.setText(listMon.isEmpty() ? "Chưa có môn" : (attendedSessions + "/" + Math.max(1, totalWeeksStudied) + " Buổi"));
        }
        if (pbBentoAttendance != null) pbBentoAttendance.setProgress(pct);

        List<LichThi> listThi = dbHelper.getAllLichThiWithMonHoc();
        int examCount = listThi.size();
        if (tvModeExam != null) {
            tvModeExam.setText("Lịch thi (" + examCount + ")");
        }
        if (tvBentoExamCount != null) {
            tvBentoExamCount.setText(examCount + " Môn thi");
        }

        List<ThongBao> listTb = dbHelper.getAllThongBao();
        if (tvTickerTitle != null && tvTickerSubtitle != null) {
            if (!listTb.isEmpty()) {
                ThongBao latest = listTb.get(0);
                tvTickerTitle.setText("Thông báo: " + latest.getTieuDe());
                tvTickerSubtitle.setText(latest.getNoiDung());
            } else {
                tvTickerTitle.setText("Chưa có thông báo học tập mới");
                tvTickerSubtitle.setText("Nhấn vào đây để sang mục Thông báo và tự thêm nhắc hẹn");
            }
        }
    }

    private int calculateCurrentWeekIndex() {
        Calendar calNow = Calendar.getInstance();
        Calendar calStart = Calendar.getInstance();
        calStart.set(2026, Calendar.AUGUST, 10, 0, 0, 0);

        long diffMs = calNow.getTimeInMillis() - calStart.getTimeInMillis();
        long days = diffMs / (24 * 60 * 60 * 1000L);
        int weekIndex = (int) (days / 7);

        if (weekIndex < 0) return 0;
        if (weekIndex >= 20) return 19;
        return weekIndex;
    }

    private void setupSelectionBarActions() {
        if (btnSelectAll != null) {
            btnSelectAll.setOnClickListener(v -> {
                if (lichHocAdapter != null) {
                    lichHocAdapter.selectAll();
                }
            });
        }

        if (btnCancelSelection != null) {
            btnCancelSelection.setOnClickListener(v -> exitSelectionMode());
        }

        if (btnDeleteSelected != null) {
            btnDeleteSelected.setOnClickListener(v -> {
                if (lichHocAdapter == null) return;
                Set<Integer> selectedIds = lichHocAdapter.getSelectedIds();
                int count = selectedIds.size();
                if (count == 0) {
                    Toast.makeText(getContext(), "Vui lòng chọn ít nhất 1 môn học!", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(getContext())
                        .setTitle("Xóa " + count + " môn học đã chọn")
                        .setMessage("Bạn có chắc chắn muốn xóa " + count + " môn học đã chọn khỏi thời khóa biểu không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            for (int id : selectedIds) {
                                dbHelper.deleteLichHoc(id);
                            }
                            Toast.makeText(getContext(), "Đã xóa thành công " + count + " môn học!", Toast.LENGTH_SHORT).show();
                            exitSelectionMode();
                            loadScheduleData();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }
    }

    private void exitSelectionMode() {
        if (lichHocAdapter != null) {
            lichHocAdapter.setSelectionMode(false);
        }
        if (layoutSelectionBar != null) layoutSelectionBar.setVisibility(View.GONE);
    }

    private void setupWeekSpinner() {
        if (getContext() == null || spinnerWeek == null) return;

        List<String> weekLabels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.AUGUST, 10, 0, 0, 0);

        for (int i = 1; i <= 20; i++) {
            Date startDate = cal.getTime();
            Calendar calEnd = (Calendar) cal.clone();
            calEnd.add(Calendar.DAY_OF_YEAR, 6);
            Date endDate = calEnd.getTime();

            String label = "Tuần " + i + " • " + sdf.format(startDate) + " - " + sdf.format(endDate);
            weekLabels.add(label);

            cal.add(Calendar.WEEK_OF_YEAR, 1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, weekLabels);
        spinnerWeek.setAdapter(adapter);
        spinnerWeek.setSelection(selectedWeekIndex);

        spinnerWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedWeekIndex = position;
                loadScheduleData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (btnPrevWeek != null) {
            btnPrevWeek.setOnClickListener(v -> {
                if (spinnerWeek.getSelectedItemPosition() > 0) {
                    spinnerWeek.setSelection(spinnerWeek.getSelectedItemPosition() - 1);
                }
            });
        }

        if (btnNextWeek != null) {
            btnNextWeek.setOnClickListener(v -> {
                if (spinnerWeek.getSelectedItemPosition() < weekLabels.size() - 1) {
                    spinnerWeek.setSelection(spinnerWeek.getSelectedItemPosition() + 1);
                }
            });
        }
    }

    private void setupWeekStrip() {
        if (getContext() == null || rvWeekStrip == null) return;

        Date weekStartDate = getSelectedWeekStartDate();
        List<LichHoc> allList = dbHelper != null ? dbHelper.getAllLichHocWithMonHoc() : new ArrayList<>();

        List<WeekStripAdapter.DayItem> dayItems = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(weekStartDate);
        SimpleDateFormat sdfNum = new SimpleDateFormat("dd", Locale.getDefault());

        String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (int i = 0; i < 7; i++) {
            Date dayDate = cal.getTime();
            int thuVal = (i == 6) ? 8 : (i + 2);
            int classCount = 0;

            for (LichHoc lh : allList) {
                if (lh.getThu() == thuVal && lh.isClassActiveOnDate(dayDate)) {
                    classCount++;
                }
            }

            dayItems.add(new WeekStripAdapter.DayItem(thuVal, dayNames[i], sdfNum.format(dayDate), dayDate, classCount > 0, classCount));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        dayItems.add(new WeekStripAdapter.DayItem(-1, "Cả tuần", "T2-CN", null, true, allList.size()));

        if (weekStripAdapter == null) {
            weekStripAdapter = new WeekStripAdapter(dayItems, dayItem -> {
                selectedThuFilter = dayItem.thuVal;
                viewMode = (selectedThuFilter == -1) ? 1 : 0;
                currentTab = 0;
                updateModeSwitcherUI();
                loadScheduleData();
            });
            rvWeekStrip.setAdapter(weekStripAdapter);
        }
        weekStripAdapter.updateDays(dayItems, selectedThuFilter);
    }

    private Date getSelectedWeekStartDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.AUGUST, 10, 0, 0, 0);
        cal.add(Calendar.WEEK_OF_YEAR, selectedWeekIndex);
        return cal.getTime();
    }

    private void showAddOptionsDialog() {
        if (getContext() == null) return;
        String[] options = {
                "Thêm Lịch Học Thủ Công",
                "Thêm Lịch Thi Mới",
                "Nhập TKB từ File Excel / CSV (.csv, .xlsx)",
                "Quét TKB từ Ảnh Album (Google ML Kit)"
        };
        new AlertDialog.Builder(getContext())
                .setTitle("Tác vụ Thời Khóa Biểu & Lịch Thi")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddScheduleDialog();
                    } else if (which == 1) {
                        showAddOrEditExamDialog(null);
                    } else if (which == 2) {
                        filePickerLauncher.launch("*/*");
                    } else if (which == 3) {
                        imagePickerLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private String buildTdmuRoomGuide(String rawRoom) {
        if (rawRoom == null || rawRoom.trim().isEmpty()) {
            return "Vị trí: Chưa cập nhật mã phòng học cụ thể.";
        }
        String room = rawRoom.trim().toUpperCase();
        String building = "Khu giảng đường TDMU";
        String gateHint = "Cổng 1 hoặc Cổng 2 (Đường Trần Văn Ơn)";
        String floorHint = "Kiểm tra bảng sơ đồ tầng tại sảnh tòa nhà";

        Matcher m = Pattern.compile("^([A-Z]+[0-9]?)[^0-9]*([0-9]{3})").matcher(room);
        if (m.find()) {
            String blockCode = m.group(1);
            String roomNum = m.group(2);
            if (blockCode != null) {
                building = "Tòa / Dãy " + blockCode;
                if (blockCode.startsWith("A3") || blockCode.startsWith("A4") || blockCode.startsWith("A2") || blockCode.startsWith("K23") || blockCode.startsWith("I4")) {
                    gateHint = "Cổng 5 (Bãi gửi xe phía Bắc)";
                } else if (blockCode.startsWith("I1") || blockCode.startsWith("I2") || blockCode.startsWith("I3") || blockCode.startsWith("F")) {
                    gateHint = "Bãi gửi xe trung tâm (cạnh GS25) hoặc Cổng 4";
                } else if (blockCode.startsWith("A1") || blockCode.startsWith("B")) {
                    gateHint = "Cổng 1 (Mặt tiền Trần Văn Ơn)";
                } else if (blockCode.startsWith("C") || blockCode.startsWith("D") || blockCode.startsWith("E")) {
                    gateHint = "Cổng 2 hoặc Cổng 3";
                } else if (blockCode.startsWith("G") || blockCode.startsWith("H") || blockCode.startsWith("K1") || blockCode.startsWith("K2")) {
                    gateHint = "Cổng 3 hoặc Cổng 4";
                }
            }
            if (roomNum != null && roomNum.length() == 3) {
                char floorDigit = roomNum.charAt(0);
                if (floorDigit == '1') floorHint = "Tầng Trệt (Phòng " + room + ")";
                else if (floorDigit == '2') floorHint = "Lầu 1 (Phòng " + room + ")";
                else if (floorDigit == '3') floorHint = "Lầu 2 (Phòng " + room + ")";
                else if (floorDigit == '4') floorHint = "Lầu 3 (Phòng " + room + ")";
                else if (floorDigit == '5') floorHint = "Lầu 4 (Phòng " + room + ")";
            }
        }

        return "• Khu vực: " + building + "\n"
                + "• Tầng / Lầu: " + floorHint + "\n"
                + "• Cổng & Bãi xe gần nhất: " + gateHint;
    }

    private void showAddOrEditExamDialog(@Nullable LichThi existing) {
        if (getContext() == null || dbHelper == null) return;
        boolean isEdit = existing != null;

        List<MonHoc> monHocList = dbHelper.getAllMonHoc();

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        Spinner spMon = new Spinner(getContext());
        EditText etNewMaMon = new EditText(getContext());
        EditText etNewTenMon = new EditText(getContext());

        if (!monHocList.isEmpty()) {
            TextView tvSelectMon = new TextView(getContext());
            tvSelectMon.setText("Chọn học phần thi:");
            layout.addView(tvSelectMon);

            List<String> monOptions = new ArrayList<>();
            int selectedMonIdx = 0;
            for (int i = 0; i < monHocList.size(); i++) {
                MonHoc mh = monHocList.get(i);
                monOptions.add(mh.getMaMon() + " - " + mh.getTenMon());
                if (isEdit && mh.getId() == existing.getMonHocId()) {
                    selectedMonIdx = i;
                }
            }
            monOptions.add("+ Nhập môn học mới...");
            spMon.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, monOptions));
            spMon.setSelection(selectedMonIdx);
            layout.addView(spMon);

            etNewMaMon.setHint("Mã môn mới (VD: LING189)");
            etNewMaMon.setVisibility(View.GONE);
            layout.addView(etNewMaMon);

            etNewTenMon.setHint("Tên môn học mới");
            etNewTenMon.setVisibility(View.GONE);
            layout.addView(etNewTenMon);

            spMon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    boolean isNew = position == monHocList.size();
                    etNewMaMon.setVisibility(isNew ? View.VISIBLE : View.GONE);
                    etNewTenMon.setVisibility(isNew ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        } else {
            etNewMaMon.setHint("Mã học phần thi (VD: LING189)");
            layout.addView(etNewMaMon);

            etNewTenMon.setHint("Tên học phần thi");
            layout.addView(etNewTenMon);
        }

        EditText etNgayGio = new EditText(getContext());
        etNgayGio.setHint("Ngày giờ thi (Nhấn để chọn hoặc nhập dd/MM/yyyy HH:mm)");
        if (isEdit && existing.getNgayGioThi() != null) {
            etNgayGio.setText(existing.getNgayGioThi());
        } else {
            SimpleDateFormat sdfDefault = new SimpleDateFormat("dd/MM/yyyy 08:00", Locale.getDefault());
            etNgayGio.setText(sdfDefault.format(new Date()));
        }
        etNgayGio.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(getContext(), (view, y, m, d) -> {
                new TimePickerDialog(getContext(), (tView, h, min) -> {
                    String formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d %02d:%02d", d, m + 1, y, h, min);
                    etNgayGio.setText(formatted);
                }, 8, 0, true).show();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
        layout.addView(etNgayGio);

        EditText etPhong = new EditText(getContext());
        etPhong.setHint("Phòng thi (VD: B2-301)");
        if (isEdit) etPhong.setText(existing.getPhongThi());
        layout.addView(etPhong);

        EditText etHinhThuc = new EditText(getContext());
        etHinhThuc.setHint("Hình thức thi (VD: Trắc nghiệm / Tự luận)");
        etHinhThuc.setText(isEdit && existing.getHinhThucThi() != null ? existing.getHinhThucThi() : "Trắc nghiệm & Tự luận");
        layout.addView(etHinhThuc);

        EditText etSbd = new EditText(getContext());
        etSbd.setHint("Số báo danh (VD: 24)");
        etSbd.setText(isEdit && existing.getSbd() != null ? existing.getSbd() : "");
        layout.addView(etSbd);

        EditText etGhiChu = new EditText(getContext());
        etGhiChu.setHint("Ghi chú phòng thi");
        etGhiChu.setText(isEdit && existing.getGhiChu() != null ? existing.getGhiChu() : "Mang theo thẻ sinh viên TDMU");
        layout.addView(etGhiChu);

        new AlertDialog.Builder(getContext())
                .setTitle(isEdit ? "Cập Nhật Lịch Thi" : "Thêm Lịch Thi Chính Thức")
                .setView(layout)
                .setPositiveButton(isEdit ? "Lưu Thay Đổi" : "Lưu Lịch Thi", (d, w) -> {
                    int targetMonId = -1;
                    String displayTenMon = "";

                    if (monHocList.isEmpty() || spMon.getSelectedItemPosition() == monHocList.size()) {
                        String maMonNew = etNewMaMon.getText().toString().trim();
                        String tenMonNew = etNewTenMon.getText().toString().trim();
                        if (maMonNew.isEmpty() || tenMonNew.isEmpty()) {
                            Toast.makeText(getContext(), "Vui lòng nhập mã môn và tên môn thi!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        long insertedId = dbHelper.insertMonHoc(new MonHoc(maMonNew, tenMonNew, 3, "#3525CD"));
                        if (insertedId == -1) {
                            for (MonHoc mh : dbHelper.getAllMonHoc()) {
                                if (mh.getMaMon().equalsIgnoreCase(maMonNew)) {
                                    insertedId = mh.getId();
                                    break;
                                }
                            }
                        }
                        targetMonId = (int) insertedId;
                        displayTenMon = tenMonNew;
                    } else {
                        MonHoc selectedMon = monHocList.get(Math.max(0, spMon.getSelectedItemPosition()));
                        targetMonId = selectedMon.getId();
                        displayTenMon = selectedMon.getTenMon();
                    }

                    String ngayGio = etNgayGio.getText().toString().trim();
                    String phong = etPhong.getText().toString().trim();
                    String hinhThuc = etHinhThuc.getText().toString().trim();
                    String sbd = etSbd.getText().toString().trim();
                    String ghiChu = etGhiChu.getText().toString().trim();

                    if (ngayGio.isEmpty() || phong.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập ngày giờ và phòng thi!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isEdit) {
                        existing.setMonHocId(targetMonId);
                        existing.setNgayGioThi(ngayGio);
                        existing.setPhongThi(phong);
                        existing.setHinhThucThi(hinhThuc);
                        existing.setSbd(sbd.isEmpty() ? "---" : sbd);
                        existing.setGhiChu(ghiChu);
                        dbHelper.updateLichThi(existing);
                        Toast.makeText(getContext(), "Đã cập nhật lịch thi môn " + displayTenMon, Toast.LENGTH_SHORT).show();
                    } else {
                        dbHelper.insertLichThi(new LichThi(targetMonId, ngayGio, phong, hinhThuc, sbd.isEmpty() ? "---" : sbd, ghiChu));
                        Toast.makeText(getContext(), "Đã thêm lịch thi môn " + displayTenMon, Toast.LENGTH_SHORT).show();
                    }
                    com.example.schedule_tdmu.utils.NotificationHelper.syncAllScheduleReminders(getContext());
                    switchViewMode(2);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ==========================================
    // XỬ LÝ NHẬP FILE EXCEL (.xlsx) & CSV/TEXT TDMU
    // ==========================================
    private void processExcelCsvFileImport(Uri fileUri) {
        if (getContext() == null || fileUri == null) return;

        String pathOrUri = fileUri.toString().toLowerCase();
        String mimeType = getContext().getContentResolver().getType(fileUri);
        if ((mimeType != null && (mimeType.startsWith("image/") || mimeType.startsWith("video/")))
                || pathOrUri.endsWith(".png") || pathOrUri.endsWith(".jpg") || pathOrUri.endsWith(".jpeg") || pathOrUri.endsWith(".webp")) {
            Toast.makeText(getContext(), "Vui lòng chọn file Excel/CSV (.csv, .xlsx, .txt), không chọn file Ảnh!", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(getContext(), "Đang giải mã và nhập TKB từ file Excel TDMU...", Toast.LENGTH_SHORT).show();

        Executors.newSingleThreadExecutor().execute(() -> {
            List<ParsedSubject> subjects = new ArrayList<>();

            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
                if (inputStream != null) {
                    byte[] fileBytes = readInputStreamToBytes(inputStream);
                    inputStream.close();

                    subjects = parseXlsxZipBytes(fileBytes);

                    if (subjects.isEmpty()) {
                        subjects = parseCsvTextBytes(fileBytes);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            int count = 0;
            for (ParsedSubject ps : subjects) {
                saveParsedSubjectToDb(ps.maMon, ps.tenMon, ps.thu, ps.tietBatDau, ps.soTiet, ps.phongHoc, ps.giangVien, ps.tuanHoc);
                count++;
            }

            final int finalCount = count;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalCount > 0) {
                    com.example.schedule_tdmu.utils.NotificationHelper.syncAllScheduleReminders(getContext());
                    Toast.makeText(getContext(), "Đã nhập thành công " + finalCount + " môn học từ File Excel TDMU!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Vui lòng chọn file Excel/CSV TDMU (.xlsx / .csv) hợp lệ!", Toast.LENGTH_SHORT).show();
                }
                loadScheduleData();
            });
        });
    }

    private byte[] readInputStreamToBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[8192];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private List<ParsedSubject> parseXlsxZipBytes(byte[] fileBytes) {
        List<ParsedSubject> list = new ArrayList<>();
        try {
            List<String> sharedStrings = new ArrayList<>();
            String sheet1Xml = null;

            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(fileBytes);
            ZipInputStream zipStream = new ZipInputStream(bais);
            ZipEntry entry;

            while ((entry = zipStream.getNextEntry()) != null) {
                String entryName = entry.getName().toLowerCase();
                if (entryName.endsWith("sharedstrings.xml")) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zipStream, StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                    Pattern tPattern = Pattern.compile("<t[^>]*>(.*?)</t>");
                    Matcher tMatcher = tPattern.matcher(sb.toString());
                    while (tMatcher.find()) {
                        String matchStr = tMatcher.group(1);
                        if (matchStr != null) {
                            sharedStrings.add(matchStr.trim());
                        }
                    }
                } else if (entryName.endsWith("sheet1.xml")) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zipStream, StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    sheet1Xml = sb.toString();
                }
            }
            zipStream.close();

            if (sheet1Xml != null) {
                Pattern rowPattern = Pattern.compile("<row r=\"([0-9]+)\"[^>]*>(.*?)</row>");
                Matcher rowMatcher = rowPattern.matcher(sheet1Xml);

                while (rowMatcher.find()) {
                    String rowNumStr = rowMatcher.group(1);
                    int rowNum = rowNumStr != null ? Integer.parseInt(rowNumStr) : 0;
                    if (rowNum <= 1) continue;

                    String rowContent = rowMatcher.group(2);
                    if (rowContent == null) continue;

                    Pattern cellPattern = Pattern.compile("<c r=\"([A-Z]+)[0-9]+\"(?: t=\"([a-z]+)\")?[^>]*>(?:<v>([^<]*)</v>)?");
                    Matcher cellMatcher = cellPattern.matcher(rowContent);

                    String maMon = "";
                    String tenMon = "";
                    int thu = 2;
                    int tietBD = 1;
                    int soTiet = 3;
                    String phong = "I4.112";
                    String gv = "Giảng viên TDMU";
                    String tuanHoc = "1-15";

                    while (cellMatcher.find()) {
                        String col = cellMatcher.group(1);
                        String type = cellMatcher.group(2);
                        String val = cellMatcher.group(3);

                        if (val == null) continue;

                        String cellString = val;
                        if ("s".equalsIgnoreCase(type)) {
                            try {
                                int strIdx = Integer.parseInt(val);
                                if (strIdx >= 0 && strIdx < sharedStrings.size()) {
                                    cellString = sharedStrings.get(strIdx);
                                }
                            } catch (Exception ignored) {}
                        }

                        if ("A".equalsIgnoreCase(col)) {
                            maMon = cellString.trim();
                        } else if ("B".equalsIgnoreCase(col)) {
                            tenMon = cellString.replaceAll("\\([0-9]+\\+[0-9]+\\)", "").trim();
                        } else if ("F".equalsIgnoreCase(col)) {
                            try { thu = Integer.parseInt(cellString.trim()); } catch (Exception ignored) {}
                        } else if ("G".equalsIgnoreCase(col)) {
                            try { tietBD = Integer.parseInt(cellString.trim()); } catch (Exception ignored) {}
                        } else if ("H".equalsIgnoreCase(col)) {
                            try { soTiet = Integer.parseInt(cellString.trim()); } catch (Exception ignored) {}
                        } else if ("I".equalsIgnoreCase(col)) {
                            phong = cellString.split("-")[0].trim();
                        } else if ("J".equalsIgnoreCase(col)) {
                            gv = cellString.trim();
                        } else if ("K".equalsIgnoreCase(col)) {
                            tuanHoc = cellString.trim();
                        }
                    }

                    if (!maMon.isEmpty() && !tenMon.isEmpty() && !maMon.equalsIgnoreCase("Mã MH") && !tenMon.toLowerCase().contains("tên môn")) {
                        list.add(new ParsedSubject(maMon, tenMon, thu, tietBD, soTiet, phong, gv, tuanHoc));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<ParsedSubject> parseCsvTextBytes(byte[] fileBytes) {
        List<ParsedSubject> list = new ArrayList<>();
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(fileBytes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(bais, StandardCharsets.UTF_8));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("\0") || line.contains("PNG") || line.contains("JFIF")) break;

                String[] parts = line.split("[,;\\t]");
                if (parts.length >= 7) {
                    String maMon = parts[0].replace("\"", "").trim();
                    String tenMon = parts[1].replace("\"", "").replaceAll("\\([0-9]+\\+[0-9]+\\)", "").trim();

                    if (maMon.isEmpty() || tenMon.isEmpty() || maMon.equalsIgnoreCase("Mã MH") || tenMon.toLowerCase().contains("tên môn") || maMon.length() > 20) {
                        continue;
                    }

                    int thu = 2;
                    try { thu = Integer.parseInt(parts[5].trim()); } catch (Exception ignored) {}

                    int tietBD = 1;
                    try { tietBD = Integer.parseInt(parts[6].trim()); } catch (Exception ignored) {}

                    int soTiet = 3;
                    if (parts.length > 7) {
                        try { soTiet = Integer.parseInt(parts[7].trim()); } catch (Exception ignored) {}
                    }

                    String phong = parts.length > 8 ? parts[8].replace("\"", "").trim() : "I4.112";
                    String gv = parts.length > 9 ? parts[9].replace("\"", "").trim() : "Giảng viên TDMU";
                    String tuanHoc = parts.length > 10 ? parts[10].replace("\"", "").trim() : "1-15";

                    list.add(new ParsedSubject(maMon, tenMon, thu, tietBD, soTiet, phong, gv, tuanHoc));
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==========================================
    // XỬ LÝ QUÉT ẢNH OCR BẰNG GOOGLE ML KIT
    // ==========================================
    private void processImageOCRInBackground(Uri imageUri) {
        if (getContext() == null || imageUri == null) return;

        Toast.makeText(getContext(), "Đang bóc tách TKB bằng Google ML Kit...", Toast.LENGTH_SHORT).show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputImage inputImage = InputImage.fromFilePath(getContext(), imageUri);
                TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                recognizer.process(inputImage)
                        .addOnSuccessListener(text -> new Handler(Looper.getMainLooper()).post(() -> parseAllSubjectsAndStartWizard(text.getText())))
                        .addOnFailureListener(e -> new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), "Lỗi nhận diện văn bản: " + e.getMessage(), Toast.LENGTH_SHORT).show()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void parseAllSubjectsAndStartWizard(String recognizedText) {
        if (recognizedText == null || recognizedText.trim().isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy chữ nào trong bức ảnh này!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ParsedSubject> subjects = parseAllTDMUSubjects(recognizedText);

        if (subjects.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin lịch học hợp lệ trong bức ảnh này!", Toast.LENGTH_LONG).show();
            return;
        }

        showFormConfirmStepDialog(subjects, 0);
    }

    private List<ParsedSubject> parseAllTDMUSubjects(String fullText) {
        List<ParsedSubject> list = new ArrayList<>();

        String extractedMaMon = "CNTT001";
        String extractedTenMon = "An ninh mạng";

        Pattern maMonPopupPattern = Pattern.compile("Mã môn học:\\s*([A-Z0-9]+)");
        Matcher maMonPopupMatcher = maMonPopupPattern.matcher(fullText);
        if (maMonPopupMatcher.find() && maMonPopupMatcher.group(1) != null) {
            extractedMaMon = maMonPopupMatcher.group(1);
        }

        Pattern tenMonPopupPattern = Pattern.compile("Tên môn học:\\s*([^\\n]+)");
        Matcher tenMonPopupMatcher = tenMonPopupPattern.matcher(fullText);
        if (tenMonPopupMatcher.find() && tenMonPopupMatcher.group(1) != null) {
            String g1 = tenMonPopupMatcher.group(1);
            if (g1 != null) {
                extractedTenMon = g1.replaceAll("\\([0-9]+\\+[0-9]+\\)", "").trim();
            }
        }

        Pattern popupLinePattern = Pattern.compile("(Thứ\\s*[2-8]|Chủ Nhật)\\s*,\\s*từ\\s*([0-9]{1,2}:[0-9]{2})\\s*đến\\s*([0-9]{1,2}:[0-9]{2})\\s*,\\s*Ph\\s*([^,]+)\\s*,\\s*GV\\s*([^,]+)", Pattern.CASE_INSENSITIVE);
        Matcher popupMatcher = popupLinePattern.matcher(fullText);

        while (popupMatcher.find()) {
            String thuStr = popupMatcher.group(1);
            String startTimeStr = popupMatcher.group(2);
            String endTimeStr = popupMatcher.group(3);
            String phongStr = popupMatcher.group(4);
            String gvStr = popupMatcher.group(5);

            int thuVal = 2;
            if (thuStr != null) {
                if (thuStr.contains("3")) thuVal = 3;
                else if (thuStr.contains("4")) thuVal = 4;
                else if (thuStr.contains("5")) thuVal = 5;
                else if (thuStr.contains("6")) thuVal = 6;
                else if (thuStr.contains("7")) thuVal = 7;
                else if (thuStr.toLowerCase().contains("chủ nhật")) thuVal = 8;
            }

            int tietBD = 1;
            int soTiet = 3;
            if (startTimeStr != null && (startTimeStr.startsWith("07") || startTimeStr.startsWith("7"))) {
                tietBD = 1;
                soTiet = (endTimeStr != null && endTimeStr.contains("11")) ? 5 : 3;
            } else if (startTimeStr != null && (startTimeStr.startsWith("09") || startTimeStr.startsWith("9"))) {
                tietBD = 4;
                soTiet = 3;
            } else if (startTimeStr != null && startTimeStr.startsWith("13")) {
                tietBD = 7;
                soTiet = (endTimeStr != null && endTimeStr.contains("17")) ? 5 : 3;
            } else if (startTimeStr != null && startTimeStr.startsWith("15")) {
                tietBD = 10;
                soTiet = 3;
            }

            list.add(new ParsedSubject(
                    extractedMaMon,
                    extractedTenMon,
                    thuVal,
                    tietBD,
                    soTiet,
                    phongStr != null ? phongStr.trim() : "I4.112",
                    gvStr != null ? gvStr.trim() : "Lê Từ Minh Trí",
                    "1-15"
            ));
        }

        if (!list.isEmpty()) {
            return list;
        }

        Pattern timePattern = Pattern.compile("([0-9]{1,2}:[0-9]{2})\\s*->\\s*([0-9]{1,2}:[0-9]{2})");
        Matcher timeMatcher = timePattern.matcher(fullText);

        while (timeMatcher.find()) {
            int startTimeIndex = timeMatcher.start();
            String startTimeStr = timeMatcher.group(1);
            String endTimeStr = timeMatcher.group(2);

            int blockStart = Math.max(0, startTimeIndex - 200);
            int blockEnd = Math.min(fullText.length(), timeMatcher.end() + 50);
            String snippet = fullText.substring(blockStart, blockEnd);

            String maMon = "MH" + (int) (Math.random() * 899 + 100);
            Pattern codePattern = Pattern.compile("\\(([A-Z0-9]{4,10})\\)");
            Matcher codeMatcher = codePattern.matcher(snippet);
            if (codeMatcher.find() && codeMatcher.group(1) != null) {
                maMon = codeMatcher.group(1);
            }

            String tenMon = "Môn học TDMU";
            String[] snippetLines = snippet.split("\n");
            for (String line : snippetLines) {
                String trimmed = line.trim();
                if (trimmed.length() > 4 && !trimmed.toLowerCase().contains("học kỳ")
                        && !trimmed.toLowerCase().contains("tuần") && !trimmed.toLowerCase().contains("nhóm:")
                        && !trimmed.toLowerCase().contains("phòng:") && !trimmed.toLowerCase().contains("gv:")
                        && !trimmed.toLowerCase().contains("thứ") && !trimmed.contains("->")) {
                    tenMon = trimmed.replaceAll("\\([0-9]+\\+[0-9]+\\)", "")
                            .replaceAll("\\([A-Z0-9]+\\)", "").trim();
                    if (!tenMon.isEmpty()) break;
                }
            }

            String phong = "TDMU Room";
            Pattern phongPattern = Pattern.compile("(Phòng|Phong|P\\.):?\\s*([A-Za-z0-9\\.\\-]+)");
            Matcher phongMatcher = phongPattern.matcher(snippet);
            if (phongMatcher.find() && phongMatcher.group(2) != null) {
                String pStr = phongMatcher.group(2);
                if (pStr != null) {
                    phong = pStr.split("-")[0].trim();
                }
            }

            String gv = "Giảng viên TDMU";
            Pattern gvPattern = Pattern.compile("GV:?\\s*([\\p{L}\\s]+)");
            Matcher gvMatcher = gvPattern.matcher(snippet);
            if (gvMatcher.find() && gvMatcher.group(1) != null) {
                String gvStr = gvMatcher.group(1);
                if (gvStr != null) {
                    gv = gvStr.trim();
                }
            }

            int thu = 3;
            if (snippet.contains("21/09") || snippet.toLowerCase().contains("thứ 2")) {
                thu = 2;
            } else if (snippet.contains("22/09") || snippet.toLowerCase().contains("thứ 3")) {
                thu = 3;
            } else if (snippet.contains("23/09") || snippet.toLowerCase().contains("thứ 4")) {
                thu = 4;
            } else if (snippet.contains("24/09") || snippet.toLowerCase().contains("thứ 5")) {
                thu = 5;
            } else if (snippet.contains("25/09") || snippet.toLowerCase().contains("thứ 6")) {
                thu = 6;
            } else if (snippet.contains("26/09") || snippet.toLowerCase().contains("thứ 7")) {
                thu = 7;
            } else if (snippet.contains("27/09") || snippet.toLowerCase().contains("chủ nhật")) {
                thu = 8;
            }

            int tietBD = 1;
            int soTiet = 3;
            if (startTimeStr != null && (startTimeStr.startsWith("07") || startTimeStr.startsWith("7"))) {
                tietBD = 1;
                soTiet = (endTimeStr != null && endTimeStr.contains("11")) ? 5 : 3;
            } else if (startTimeStr != null && (startTimeStr.startsWith("09") || startTimeStr.startsWith("9"))) {
                tietBD = 4;
                soTiet = 3;
            } else if (startTimeStr != null && startTimeStr.startsWith("13")) {
                tietBD = 7;
                soTiet = (endTimeStr != null && endTimeStr.contains("17")) ? 5 : 3;
            } else if (startTimeStr != null && startTimeStr.startsWith("15")) {
                tietBD = 10;
                soTiet = 3;
            }

            list.add(new ParsedSubject(maMon, tenMon, thu, tietBD, soTiet, phong, gv, "1-15"));
        }

        return list;
    }

    private void setupPeriodTimePreviewWatcher(EditText etTietBatDau, EditText etSoTiet, TextView tvPreviewTimeRange) {
        if (etTietBatDau == null || etSoTiet == null || tvPreviewTimeRange == null) return;

        Runnable updatePreview = () -> {
            try {
                String s1 = etTietBatDau.getText().toString().trim();
                String s2 = etSoTiet.getText().toString().trim();
                if (s1.isEmpty() || s2.isEmpty()) {
                    tvPreviewTimeRange.setText("Nhập tiết bắt đầu và số tiết để xem khung giờ TDMU");
                    return;
                }
                int tStart = Integer.parseInt(s1);
                int count = Integer.parseInt(s2);
                if (tStart < 1 || tStart > 15 || count < 1 || (tStart + count - 1) > 15) {
                    tvPreviewTimeRange.setText("Tiết học TDMU hợp lệ từ Tiết 1 đến Tiết 15");
                    return;
                }
                int tEnd = tStart + count - 1;
                String range = LichHocAdapter.getTdmuPeriodTime(tStart, tEnd);
                tvPreviewTimeRange.setText("Khung giờ học: " + range.replace(" - ", " -> ") + " (Tiết " + tStart + " - " + tEnd + ")");
            } catch (Exception e) {
                tvPreviewTimeRange.setText("Khung giờ học: 07:00 -> 09:30 (Tiết 1 - 3)");
            }
        };

        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview.run();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        etTietBatDau.addTextChangedListener(watcher);
        etSoTiet.addTextChangedListener(watcher);
        updatePreview.run();
    }

    private void showFormConfirmStepDialog(List<ParsedSubject> subjects, int currentIndex) {
        if (getContext() == null || subjects == null || subjects.isEmpty() || currentIndex >= subjects.size()) return;

        ParsedSubject currentSubject = subjects.get(currentIndex);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_schedule, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText etMaMon = dialogView.findViewById(R.id.et_ma_mon);
        EditText etTenMon = dialogView.findViewById(R.id.et_ten_mon);
        EditText etSoTinChi = dialogView.findViewById(R.id.et_so_tin_chi);
        Spinner spinnerThu = dialogView.findViewById(R.id.spinner_thu);
        EditText etTietBatDau = dialogView.findViewById(R.id.et_tiet_bat_dau);
        EditText etSoTiet = dialogView.findViewById(R.id.et_so_tiet);
        TextView tvPreviewTimeRange = dialogView.findViewById(R.id.tv_preview_time_range);
        EditText etTuanHoc = dialogView.findViewById(R.id.et_tuan_hoc);
        EditText etPhongHoc = dialogView.findViewById(R.id.et_phong_hoc);
        EditText etGiangVien = dialogView.findViewById(R.id.et_giang_vien);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        if (tvTitle != null) {
            tvTitle.setText("Môn " + (currentIndex + 1) + "/" + subjects.size() + ": Bóc Tách TKB");
        }

        String[] dsThu = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, dsThu);
        spinnerThu.setAdapter(adapter);

        etMaMon.setText(currentSubject.maMon);
        etTenMon.setText(currentSubject.tenMon);
        if (etSoTinChi != null) etSoTinChi.setText("3");
        spinnerThu.setSelection(Math.max(0, currentSubject.thu - 2));
        etTietBatDau.setText(String.valueOf(currentSubject.tietBatDau));
        etSoTiet.setText(String.valueOf(currentSubject.soTiet));
        if (etTuanHoc != null) etTuanHoc.setText(currentSubject.tuanHoc != null ? currentSubject.tuanHoc : "1-15");
        etPhongHoc.setText(currentSubject.phongHoc);
        etGiangVien.setText(currentSubject.giangVien);

        setupPeriodTimePreviewWatcher(etTietBatDau, etSoTiet, tvPreviewTimeRange);

        boolean isLast = (currentIndex == subjects.size() - 1);
        btnSave.setText(isLast ? "Lưu & Hoàn Tất" : "Lưu & Tiếp Theo (" + (currentIndex + 2) + "/" + subjects.size() + ")");

        if (!isLast) {
            builder.setNeutralButton("Lưu Tất Cả (" + (subjects.size() - currentIndex) + " Môn)", (d, which) -> {
                int count = 0;
                for (int i = currentIndex; i < subjects.size(); i++) {
                    ParsedSubject ps = subjects.get(i);
                    saveParsedSubjectToDb(ps.maMon, ps.tenMon, ps.thu, ps.tietBatDau, ps.soTiet, ps.phongHoc, ps.giangVien, ps.tuanHoc);
                    count++;
                }
                com.example.schedule_tdmu.utils.NotificationHelper.syncAllScheduleReminders(getContext());
                Toast.makeText(getContext(), "Đã lưu thành công tất cả " + count + " môn học vào CSDL!", Toast.LENGTH_LONG).show();
                loadScheduleData();
            });
        }

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String maMon = etMaMon.getText().toString().trim();
            String tenMon = etTenMon.getText().toString().trim();
            String tietBDStr = etTietBatDau.getText().toString().trim();
            String soTietStr = etSoTiet.getText().toString().trim();
            String tuanHocStr = etTuanHoc != null && !etTuanHoc.getText().toString().trim().isEmpty()
                    ? etTuanHoc.getText().toString().trim()
                    : currentSubject.tuanHoc;

            if (maMon.isEmpty() || tenMon.isEmpty() || tietBDStr.isEmpty() || soTietStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            int thu = spinnerThu.getSelectedItemPosition() + 2;
            int tietBD = Integer.parseInt(tietBDStr);
            int soTietVal = Integer.parseInt(soTietStr);

            saveParsedSubjectToDb(maMon, tenMon, thu, tietBD, soTietVal, etPhongHoc.getText().toString().trim(), etGiangVien.getText().toString().trim(), tuanHocStr);

            dialog.dismiss();

            if (currentIndex + 1 < subjects.size()) {
                Toast.makeText(getContext(), "Đã lưu môn [" + tenMon + "]. Chuyển sang môn tiếp theo...", Toast.LENGTH_SHORT).show();
                showFormConfirmStepDialog(subjects, currentIndex + 1);
            } else {
                com.example.schedule_tdmu.utils.NotificationHelper.syncAllScheduleReminders(getContext());
                Toast.makeText(getContext(), "Hoàn tất! Đã lưu toàn bộ " + subjects.size() + " môn học vào CSDL!", Toast.LENGTH_LONG).show();
                loadScheduleData();
            }
        });

        dialog.show();
    }

    private void saveParsedSubjectToDb(String maMon, String tenMon, int thu, int tietBD, int soTietVal, String phong, String gv, String tuanHoc) {
        if (dbHelper == null) return;

        MonHoc mhNew = new MonHoc(maMon, tenMon, 3, "#3525CD");
        mhNew.setGiangVien(gv);
        long monHocId = dbHelper.insertMonHoc(mhNew);
        if (monHocId == -1) {
            List<MonHoc> allMon = dbHelper.getAllMonHoc();
            for (MonHoc mh : allMon) {
                if (mh.getMaMon().equalsIgnoreCase(maMon)) {
                    monHocId = mh.getId();
                    break;
                }
            }
        }

        if (monHocId != -1) {
            LichHoc lh = new LichHoc((int) monHocId, thu, tietBD, soTietVal, phong, gv, 1, "2026-2027", tuanHoc != null ? tuanHoc : "1-15");
            dbHelper.insertLichHoc(lh);
        }
    }

    private void loadScheduleData() {
        if (dbHelper == null) return;

        updateHeroAndBentoCards();
        Date currentWeekStartDate = getSelectedWeekStartDate();

        if (currentTab == 0) {
            if (layoutWeekBar != null) layoutWeekBar.setVisibility(View.VISIBLE);
            if (layoutCurrentTimeIndicator != null) layoutCurrentTimeIndicator.setVisibility(View.VISIBLE);
            setupWeekStrip();

            List<LichHoc> allList = dbHelper.getAllLichHocWithMonHoc();
            List<LichHoc> filteredList = new ArrayList<>();

            for (LichHoc lh : allList) {
                if (lh.isClassActiveInWeek(currentWeekStartDate)) {
                    if (selectedThuFilter == -1 || lh.getThu() == selectedThuFilter) {
                        filteredList.add(lh);
                    }
                }
            }

            // Cập nhật tiêu đề Timeline
            Calendar calToday = Calendar.getInstance();
            int todayDow = calToday.get(Calendar.DAY_OF_WEEK);
            int todayThu = (todayDow == Calendar.SUNDAY) ? 8 : todayDow;

            if (tvTimelineDayTitle != null) {
                if (selectedThuFilter == -1) {
                    tvTimelineDayTitle.setText("Thời khóa biểu cả tuần");
                } else {
                    String thuName = selectedThuFilter == 8 ? "Chủ Nhật" : "Thứ " + selectedThuFilter;
                    tvTimelineDayTitle.setText("Lịch học " + thuName);
                }
            }
            if (tvTimelineTodayBadge != null) {
                tvTimelineTodayBadge.setVisibility(selectedThuFilter == todayThu ? View.VISIBLE : View.GONE);
            }
            if (tvTimelineTotalCount != null) {
                tvTimelineTotalCount.setText("Tổng: " + filteredList.size() + " môn");
            }

            if (layoutEmptySchedule != null) {
                layoutEmptySchedule.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            if (lichHocAdapter == null) {
                lichHocAdapter = new LichHocAdapter(filteredList, currentWeekStartDate, new LichHocAdapter.OnLichHocClickListener() {
                    @Override
                    public void onItemClick(LichHoc lichHoc) {
                        showLichHocDetailDialog(lichHoc);
                    }

                    @Override
                    public void onItemLongClick(LichHoc lichHoc) {
                        enterSelectionMode();
                    }
                });

                lichHocAdapter.setOnSelectionChangedListener(selectedCount -> {
                    if (tvSelectedCount != null) {
                        tvSelectedCount.setText("Đã chọn " + selectedCount + " môn");
                    }
                    if (selectedCount > 0 || (lichHocAdapter != null && lichHocAdapter.isSelectionMode())) {
                        if (layoutSelectionBar != null) layoutSelectionBar.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                lichHocAdapter.setWeekStartDate(currentWeekStartDate);
                lichHocAdapter.updateData(filteredList);
            }
            rvScheduleList.setAdapter(lichHocAdapter);
        } else {
            if (layoutWeekBar != null) layoutWeekBar.setVisibility(View.GONE);
            if (layoutCurrentTimeIndicator != null) layoutCurrentTimeIndicator.setVisibility(View.GONE);
            if (layoutEmptySchedule != null) layoutEmptySchedule.setVisibility(View.GONE);

            List<LichThi> listLichThi = dbHelper.getAllLichThiWithMonHoc();
            if (tvTimelineDayTitle != null) {
                tvTimelineDayTitle.setText("Lịch thi chính thức Học kỳ");
            }
            if (tvTimelineTodayBadge != null) {
                tvTimelineTodayBadge.setVisibility(View.GONE);
            }
            if (tvTimelineTotalCount != null) {
                tvTimelineTotalCount.setText("Tổng: " + listLichThi.size() + " môn thi");
            }

            if (lichThiAdapter == null) {
                lichThiAdapter = new LichThiAdapter(listLichThi, new LichThiAdapter.OnLichThiClickListener() {
                    @Override
                    public void onItemClick(LichThi lichThi) {
                        showLichThiDetailDialog(lichThi);
                    }

                    @Override
                    public void onItemLongClick(LichThi lichThi) {
                        showDeleteLichThiDialog(lichThi);
                    }
                });
            } else {
                lichThiAdapter.updateData(listLichThi);
            }
            rvScheduleList.setAdapter(lichThiAdapter);
        }
    }

    private void enterSelectionMode() {
        if (lichHocAdapter != null) {
            lichHocAdapter.setSelectionMode(true);
        }
        if (layoutSelectionBar != null) layoutSelectionBar.setVisibility(View.VISIBLE);
    }

    private void showLichHocDetailDialog(LichHoc item) {
        if (getContext() == null || item == null) return;

        String thuStr = item.getThu() == 8 ? "Chủ Nhật" : "Thứ " + item.getThu();
        int tietStart = item.getTietBatDau();
        int tietEnd = tietStart + item.getSoTiet() - 1;
        String timeStr = LichHocAdapter.getTdmuPeriodTime(tietStart, tietEnd);

        String message = "Mã môn học: " + (item.getMaMon() != null ? item.getMaMon() : "N/A") + "\n\n" +
                "Thời gian: " + thuStr + " (" + timeStr + " • Tiết " + tietStart + " - " + tietEnd + ")\n\n" +
                "Tuần học: " + (item.getTuanHoc() != null ? item.getTuanHoc() : "1-15") + "\n\n" +
                "Phòng học: " + (item.getPhongHoc() != null ? item.getPhongHoc() : "N/A") + "\n\n" +
                "Giảng viên: " + (item.getGiangVien() != null ? item.getGiangVien() : "N/A") + "\n\n" +
                "Học kỳ: " + (item.getHocKy() > 0 ? "Học kỳ " + item.getHocKy() : "Học kỳ 1") +
                " (Năm học " + (item.getNamHoc() != null ? item.getNamHoc() : "2026-2027") + ")";

        new AlertDialog.Builder(getContext())
                .setTitle("Chi Tiết Môn Học: " + item.getTenMon())
                .setMessage(message)
                .setPositiveButton("Đóng", null)
                .setNegativeButton("Chỉnh sửa", (dialog, which) -> showEditScheduleDialog(item))
                .setNeutralButton("Xóa Môn", (dialog, which) -> showDeleteLichHocDialog(item))
                .show();
    }

    private void showEditScheduleDialog(LichHoc item) {
        if (getContext() == null || item == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_schedule, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText etMaMon = dialogView.findViewById(R.id.et_ma_mon);
        EditText etTenMon = dialogView.findViewById(R.id.et_ten_mon);
        EditText etSoTinChi = dialogView.findViewById(R.id.et_so_tin_chi);
        Spinner spinnerThu = dialogView.findViewById(R.id.spinner_thu);
        EditText etTietBatDau = dialogView.findViewById(R.id.et_tiet_bat_dau);
        EditText etSoTiet = dialogView.findViewById(R.id.et_so_tiet);
        TextView tvPreviewTimeRange = dialogView.findViewById(R.id.tv_preview_time_range);
        EditText etTuanHoc = dialogView.findViewById(R.id.et_tuan_hoc);
        EditText etPhongHoc = dialogView.findViewById(R.id.et_phong_hoc);
        EditText etGiangVien = dialogView.findViewById(R.id.et_giang_vien);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        if (tvTitle != null) tvTitle.setText("Cập Nhật Lịch Học");
        String[] dsThu = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        spinnerThu.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, dsThu));

        etMaMon.setText(item.getMaMon());
        etMaMon.setEnabled(false);
        etTenMon.setText(item.getTenMon());
        etTenMon.setEnabled(false);
        if (etSoTinChi != null) {
            etSoTinChi.setText("3");
            etSoTinChi.setEnabled(false);
        }
        spinnerThu.setSelection(Math.max(0, item.getThu() - 2));
        etTietBatDau.setText(String.valueOf(item.getTietBatDau()));
        etSoTiet.setText(String.valueOf(item.getSoTiet()));
        if (etTuanHoc != null) etTuanHoc.setText(item.getTuanHoc() != null ? item.getTuanHoc() : "1-15");
        etPhongHoc.setText(item.getPhongHoc());
        etGiangVien.setText(item.getGiangVien());

        setupPeriodTimePreviewWatcher(etTietBatDau, etSoTiet, tvPreviewTimeRange);

        AlertDialog dialog = builder.create();
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String tietBDStr = etTietBatDau.getText().toString().trim();
            String soTietStr = etSoTiet.getText().toString().trim();
            if (tietBDStr.isEmpty() || soTietStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tiết bắt đầu và số tiết!", Toast.LENGTH_SHORT).show();
                return;
            }

            final int newThu = spinnerThu.getSelectedItemPosition() + 2;
            final int newTietBD;
            final int newSoTiet;
            try {
                newTietBD = Integer.parseInt(tietBDStr);
                newSoTiet = Integer.parseInt(soTietStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Tiết bắt đầu và số tiết phải là số nguyên!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newTietBD < 1 || newTietBD > 15 || newSoTiet < 1 || (newTietBD + newSoTiet - 1) > 15) {
                Toast.makeText(getContext(), "Tiết học TDMU phải nằm trong khoảng Tiết 1 đến Tiết 15!", Toast.LENGTH_SHORT).show();
                return;
            }

            final String oldPhong = item.getPhongHoc() != null ? item.getPhongHoc().trim() : "";
            final int oldThu = item.getThu();
            final int oldTietBD = item.getTietBatDau();
            final String newPhong = etPhongHoc.getText().toString().trim();
            final String newGv = etGiangVien.getText().toString().trim();
            final String newTuanHoc = etTuanHoc != null && !etTuanHoc.getText().toString().trim().isEmpty()
                    ? etTuanHoc.getText().toString().trim()
                    : (item.getTuanHoc() != null ? item.getTuanHoc() : "1-15");

            Runnable commitUpdate = () -> {
                item.setThu(newThu);
                item.setTietBatDau(newTietBD);
                item.setSoTiet(newSoTiet);
                item.setPhongHoc(newPhong);
                item.setGiangVien(newGv);
                item.setTuanHoc(newTuanHoc);
                dbHelper.updateLichHoc(item);

                boolean roomChanged = !oldPhong.isEmpty() && !newPhong.isEmpty() && !oldPhong.equalsIgnoreCase(newPhong);
                boolean scheduleShifted = (oldThu != newThu) || (oldTietBD != newTietBD);

                if (roomChanged || scheduleShifted) {
                    String thuLabel = newThu == 8 ? "Chủ Nhật" : "Thứ " + newThu;
                    int endTiet = newTietBD + newSoTiet - 1;
                    String timeRange = LichHocAdapter.getTdmuPeriodTime(newTietBD, endTiet);
                    String notifTitle = roomChanged
                            ? ("Thông báo đổi phòng: " + item.getTenMon())
                            : ("Cập nhật lịch học: " + item.getTenMon());
                    String notifContent = "Học phần " + item.getTenMon() + " (" + item.getMaMon() + ") được cập nhật sang "
                            + thuLabel + ", Tiết " + newTietBD + "-" + endTiet + " (" + timeRange + ") tại phòng " + newPhong + ".";
                    String nowStr = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

                    ThongBao urgentTb = new ThongBao(
                            notifTitle,
                            notifContent,
                            nowStr,
                            "URGENT",
                            "Đồng bộ từ Thời khóa biểu",
                            roomChanged ? oldPhong : "",
                            roomChanged ? newPhong : ""
                    );
                    dbHelper.insertThongBao(urgentTb);

                    com.example.schedule_tdmu.utils.NotificationHelper.scheduleNotification(
                            getContext(),
                            notifTitle,
                            notifContent,
                            System.currentTimeMillis() + 1200L,
                            (int) (System.currentTimeMillis() & 0x7fffffff)
                    );

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).refreshHeaderBadge();
                    }
                }

                com.example.schedule_tdmu.utils.NotificationHelper.syncAllScheduleReminders(getContext());
                Toast.makeText(getContext(), "Đã cập nhật lịch học & đồng bộ nhắc nhở!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadScheduleData();
            };

            LichHoc firstConflict = dbHelper.findConflictingLichHoc(newThu, newTietBD, newSoTiet, item.getId());
            if (firstConflict != null) {
                int cEnd = firstConflict.getTietBatDau() + firstConflict.getSoTiet() - 1;
                String thuLabel = newThu == 8 ? "Chủ Nhật" : "Thứ " + newThu;
                new AlertDialog.Builder(getContext())
                        .setTitle("Cảnh báo trùng tiết học")
                        .setMessage("Khung giờ " + thuLabel + " (Tiết " + newTietBD + "-" + (newTietBD + newSoTiet - 1)
                                + ") bị trùng với môn:\n• " + firstConflict.getTenMon()
                                + " (Tiết " + firstConflict.getTietBatDau() + "-" + cEnd + ", Phòng " + firstConflict.getPhongHoc() + ").\n\n"
                                + "Bạn có chắc chắn vẫn muốn lưu thay đổi này không?")
                        .setPositiveButton("Vẫn lưu", (d, w) -> commitUpdate.run())
                        .setNegativeButton("Chỉnh lại", null)
                        .show();
            } else {
                commitUpdate.run();
            }
        });
        dialog.show();
    }

    private void showLichThiDetailDialog(LichThi item) {
        if (getContext() == null || item == null) return;

        String message = "Môn thi: " + (item.getTenMon() != null ? item.getTenMon() : "N/A") + "\n\n" +
                "Ngày giờ thi: " + (item.getNgayGioThi() != null ? item.getNgayGioThi() : "Chưa cập nhật") + "\n\n" +
                "Hình thức thi: " + (item.getHinhThucThi() != null ? item.getHinhThucThi() : "Trắc nghiệm / Tự luận") + "\n\n" +
                "Phòng thi: " + (item.getPhongThi() != null ? item.getPhongThi() : "N/A") +
                " | SBD: " + (item.getSbd() != null ? item.getSbd() : "---") + "\n\n" +
                "Ghi chú: " + (item.getGhiChu() != null && !item.getGhiChu().isEmpty() ? item.getGhiChu() : "Mang theo thẻ sinh viên TDMU khi vào phòng thi.");

        new AlertDialog.Builder(getContext())
                .setTitle("Chi Tiết Lịch Thi")
                .setMessage(message)
                .setPositiveButton("Đóng", null)
                .setNegativeButton("Chỉnh sửa", (dialog, which) -> showAddOrEditExamDialog(item))
                .setNeutralButton("Xóa Lịch Thi", (dialog, which) -> showDeleteLichThiDialog(item))
                .show();
    }

    private void showDeleteLichHocDialog(LichHoc item) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa Lịch Học")
                .setMessage("Bạn có chắc chắn muốn xóa môn [" + item.getTenMon() + "] khỏi thời khóa biểu không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteLichHoc(item.getId());
                    com.example.schedule_tdmu.utils.NotificationHelper.syncAllScheduleReminders(getContext());
                    Toast.makeText(getContext(), "Đã xóa môn " + item.getTenMon() + "!", Toast.LENGTH_SHORT).show();
                    loadScheduleData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteLichThiDialog(LichThi item) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa Lịch Thi")
                .setMessage("Bạn có chắc chắn muốn xóa lịch thi môn [" + item.getTenMon() + "] không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteLichThi(item.getId());
                    com.example.schedule_tdmu.utils.NotificationHelper.syncAllScheduleReminders(getContext());
                    Toast.makeText(getContext(), "Đã xóa lịch thi môn " + item.getTenMon() + "!", Toast.LENGTH_SHORT).show();
                    loadScheduleData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAddScheduleDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_schedule, null);
        builder.setView(dialogView);

        EditText etMaMon = dialogView.findViewById(R.id.et_ma_mon);
        EditText etTenMon = dialogView.findViewById(R.id.et_ten_mon);
        EditText etSoTinChi = dialogView.findViewById(R.id.et_so_tin_chi);
        Spinner spinnerThu = dialogView.findViewById(R.id.spinner_thu);
        EditText etTietBatDau = dialogView.findViewById(R.id.et_tiet_bat_dau);
        EditText etSoTiet = dialogView.findViewById(R.id.et_so_tiet);
        TextView tvPreviewTimeRange = dialogView.findViewById(R.id.tv_preview_time_range);
        EditText etTuanHoc = dialogView.findViewById(R.id.et_tuan_hoc);
        EditText etPhongHoc = dialogView.findViewById(R.id.et_phong_hoc);
        EditText etGiangVien = dialogView.findViewById(R.id.et_giang_vien);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        String[] dsThu = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, dsThu);
        spinnerThu.setAdapter(adapter);
        if (selectedThuFilter >= 2 && selectedThuFilter <= 8) {
            spinnerThu.setSelection(selectedThuFilter - 2);
        }

        setupPeriodTimePreviewWatcher(etTietBatDau, etSoTiet, tvPreviewTimeRange);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String maMon = etMaMon.getText().toString().trim();
            String tenMon = etTenMon.getText().toString().trim();
            String tietBDStr = etTietBatDau.getText().toString().trim();
            String soTietStr = etSoTiet.getText().toString().trim();

            if (maMon.isEmpty() || tenMon.isEmpty() || tietBDStr.isEmpty() || soTietStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ mã môn, tên môn và số tiết!", Toast.LENGTH_SHORT).show();
                return;
            }

            final int thu = spinnerThu.getSelectedItemPosition() + 2;
            final int tietBD;
            final int soTiet;
            try {
                tietBD = Integer.parseInt(tietBDStr);
                soTiet = Integer.parseInt(soTietStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Tiết bắt đầu và số tiết phải là số nguyên!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tietBD < 1 || tietBD > 15 || soTiet < 1 || (tietBD + soTiet - 1) > 15) {
                Toast.makeText(getContext(), "Tiết học TDMU phải nằm trong khoảng Tiết 1 đến Tiết 15!", Toast.LENGTH_SHORT).show();
                return;
            }

            int parsedCredits = 3;
            if (etSoTinChi != null && !etSoTinChi.getText().toString().trim().isEmpty()) {
                try {
                    parsedCredits = Math.max(1, Integer.parseInt(etSoTinChi.getText().toString().trim()));
                } catch (NumberFormatException ignored) {}
            }
            final int soTinChi = parsedCredits;
            final String tuanHoc = (etTuanHoc != null && !etTuanHoc.getText().toString().trim().isEmpty())
                    ? etTuanHoc.getText().toString().trim()
                    : "1-15";
            final String phongHoc = etPhongHoc.getText().toString().trim().isEmpty()
                    ? "Chưa xếp phòng"
                    : etPhongHoc.getText().toString().trim();
            final String giangVien = etGiangVien.getText().toString().trim().isEmpty()
                    ? "Giảng viên TDMU"
                    : etGiangVien.getText().toString().trim();

            Runnable commitInsert = () -> {
                MonHoc newMon = new MonHoc(maMon, tenMon, soTinChi, "#3525CD");
                newMon.setGiangVien(giangVien);
                long monHocId = dbHelper.insertMonHoc(newMon);
                if (monHocId == -1) {
                    List<MonHoc> allMon = dbHelper.getAllMonHoc();
                    for (MonHoc mh : allMon) {
                        if (mh.getMaMon().equalsIgnoreCase(maMon)) {
                            monHocId = mh.getId();
                            break;
                        }
                    }
                }

                LichHoc lh = new LichHoc((int) monHocId, thu, tietBD, soTiet, phongHoc,
                        giangVien, 1, "2026-2027", tuanHoc);
                dbHelper.insertLichHoc(lh);

                com.example.schedule_tdmu.utils.NotificationHelper.syncAllScheduleReminders(getContext());
                Toast.makeText(getContext(), "Đã thêm lịch học & đồng bộ nhắc nhở!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadScheduleData();
            };

            LichHoc firstConflict = dbHelper.findConflictingLichHoc(thu, tietBD, soTiet, -1);
            if (firstConflict != null) {
                int cEnd = firstConflict.getTietBatDau() + firstConflict.getSoTiet() - 1;
                String thuLabel = thu == 8 ? "Chủ Nhật" : "Thứ " + thu;
                new AlertDialog.Builder(getContext())
                        .setTitle("Cảnh báo trùng tiết học")
                        .setMessage("Khung giờ " + thuLabel + " (Tiết " + tietBD + "-" + (tietBD + soTiet - 1)
                                + ") bị trùng với môn:\n• " + firstConflict.getTenMon()
                                + " (Tiết " + firstConflict.getTietBatDau() + "-" + cEnd + ", Phòng " + firstConflict.getPhongHoc() + ").\n\n"
                                + "Bạn có chắc chắn vẫn muốn thêm môn học này không?")
                        .setPositiveButton("Vẫn lưu", (d, w) -> commitInsert.run())
                        .setNegativeButton("Chỉnh lại", null)
                        .show();
            } else {
                commitInsert.run();
            }
        });

        dialog.show();
    }
}
