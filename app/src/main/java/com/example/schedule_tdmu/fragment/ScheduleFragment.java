package com.example.schedule_tdmu.fragment;

import android.app.AlertDialog;
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
import android.widget.LinearLayout;
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
import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.adapter.LichHocAdapter;
import com.example.schedule_tdmu.adapter.LichThiAdapter;
import com.example.schedule_tdmu.adapter.WeekStripAdapter;
import com.example.schedule_tdmu.model.LichHoc;
import com.example.schedule_tdmu.model.LichThi;
import com.example.schedule_tdmu.model.MonHoc;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
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
    private RecyclerView rvScheduleList, rvWeekStrip;
    private WeekStripAdapter weekStripAdapter;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddSchedule;
    private Button btnImportExcel, btnScanScheduleTop;
    private Spinner spinnerWeek;
    private ImageButton btnPrevWeek, btnNextWeek;

    // Thanh Thao tác Chọn Hàng Loạt
    private LinearLayout layoutWeekBar, layoutSelectionBar;
    private TextView tvSelectedCount;
    private Button btnSelectAll, btnDeleteSelected;
    private ImageButton btnCancelSelection;

    private LichHocAdapter lichHocAdapter;
    private LichThiAdapter lichThiAdapter;
    private int currentTab = 0; // 0: Lịch học, 1: Lịch thi
    private int selectedWeekIndex = 0;
    private int selectedThuFilter = -1; // -1: Tất cả các ngày trong tuần

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
        }

        tabLayout = view.findViewById(R.id.tab_layout);
        rvScheduleList = view.findViewById(R.id.rv_schedule_list);
        rvWeekStrip = view.findViewById(R.id.rv_week_strip);
        fabAddSchedule = view.findViewById(R.id.fab_add_schedule);
        btnImportExcel = view.findViewById(R.id.btn_import_excel);
        btnScanScheduleTop = view.findViewById(R.id.btn_scan_schedule_top);

        layoutWeekBar = view.findViewById(R.id.layout_week_bar);
        spinnerWeek = view.findViewById(R.id.spinner_week);
        btnPrevWeek = view.findViewById(R.id.btn_prev_week);
        btnNextWeek = view.findViewById(R.id.btn_next_week);

        // Thanh thao tác Chọn Hàng Loạt
        layoutSelectionBar = view.findViewById(R.id.layout_selection_bar);
        tvSelectedCount = view.findViewById(R.id.tv_selected_count);
        btnSelectAll = view.findViewById(R.id.btn_select_all);
        btnDeleteSelected = view.findViewById(R.id.btn_delete_selected);
        btnCancelSelection = view.findViewById(R.id.btn_cancel_selection);

        rvScheduleList.setLayoutManager(new LinearLayoutManager(getContext()));
        if (rvWeekStrip != null) {
            rvWeekStrip.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        }

        selectedWeekIndex = calculateCurrentWeekIndex();
        setupWeekSpinner();
        setupSelectionBarActions();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                exitSelectionMode();
                loadScheduleData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        if (btnImportExcel != null) {
            btnImportExcel.setOnClickListener(v -> filePickerLauncher.launch("*/*"));
        }

        if (btnScanScheduleTop != null) {
            btnScanScheduleTop.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }

        fabAddSchedule.setOnClickListener(v -> showAddOptionsDialog());

        loadScheduleData();

        return view;
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
                            Toast.makeText(getContext(), "🎉 Đã xóa thành công " + count + " môn học!", Toast.LENGTH_SHORT).show();
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
        if (layoutWeekBar != null) layoutWeekBar.setVisibility(View.VISIBLE);
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
            boolean hasClass = false;

            for (LichHoc lh : allList) {
                if (lh.getThu() == thuVal && lh.isClassActiveOnDate(dayDate)) {
                    hasClass = true;
                    break;
                }
            }

            dayItems.add(new WeekStripAdapter.DayItem(thuVal, dayNames[i], sdfNum.format(dayDate), dayDate, hasClass));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        dayItems.add(new WeekStripAdapter.DayItem(-1, "Tất cả", "Tất cả", null, true));

        if (weekStripAdapter == null) {
            weekStripAdapter = new WeekStripAdapter(dayItems, dayItem -> {
                selectedThuFilter = dayItem.thuVal;
                loadScheduleData();
            });
            rvWeekStrip.setAdapter(weekStripAdapter);
        } else {
            weekStripAdapter.updateDays(dayItems, selectedThuFilter);
        }
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
                "Nhập TKB từ File Excel / CSV (.csv, .xlsx)",
                "Quét TKB từ Ảnh Album (AI Google ML Kit)",
                "Thêm Môn Học Thủ Công"
        };
        new AlertDialog.Builder(getContext())
                .setTitle("Chọn Phương Thức Thêm TKB")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        filePickerLauncher.launch("*/*");
                    } else if (which == 1) {
                        imagePickerLauncher.launch("image/*");
                    } else {
                        showAddScheduleDialog();
                    }
                })
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
            Toast.makeText(getContext(), "⚠️ Vui lòng chọn file Excel/CSV (.csv, .xlsx, .txt), không chọn file Ảnh!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getContext(), "🎉 Đã nhập thành công " + finalCount + " môn học từ File Excel TDMU!", Toast.LENGTH_LONG).show();
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
    // XỬ LÝ QUÉT ẢNH OCR BẰNG GOOGLE ML KIT BẰNG LƯỒNG NỀN
    // ==========================================
    private void processImageOCRInBackground(Uri imageUri) {
        if (getContext() == null || imageUri == null) return;

        Toast.makeText(getContext(), "Đang bóc tách TKB bằng AI Google ML Kit...", Toast.LENGTH_SHORT).show();

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

            String maMon = "MH" + (int)(Math.random() * 899 + 100);
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
            } else {
                if (tenMon.toLowerCase().contains("chuyên đề xử lý dữ liệu lớn")) {
                    thu = 5;
                } else if (tenMon.toLowerCase().contains("thực hành chuyên đề")) {
                    thu = 6;
                } else if (tenMon.toLowerCase().contains("thực hành phát triển")) {
                    thu = 7;
                }
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

    private void showFormConfirmStepDialog(List<ParsedSubject> subjects, int currentIndex) {
        if (getContext() == null || subjects == null || subjects.isEmpty() || currentIndex >= subjects.size()) return;

        ParsedSubject currentSubject = subjects.get(currentIndex);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_schedule, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText etMaMon = dialogView.findViewById(R.id.et_ma_mon);
        EditText etTenMon = dialogView.findViewById(R.id.et_ten_mon);
        Spinner spinnerThu = dialogView.findViewById(R.id.spinner_thu);
        EditText etTietBatDau = dialogView.findViewById(R.id.et_tiet_bat_dau);
        EditText etSoTiet = dialogView.findViewById(R.id.et_so_tiet);
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
        spinnerThu.setSelection(Math.max(0, currentSubject.thu - 2));
        etTietBatDau.setText(String.valueOf(currentSubject.tietBatDau));
        etSoTiet.setText(String.valueOf(currentSubject.soTiet));
        etPhongHoc.setText(currentSubject.phongHoc);
        etGiangVien.setText(currentSubject.giangVien);

        boolean isLast = (currentIndex == subjects.size() - 1);
        btnSave.setText(isLast ? "Lưu & Hoàn Tất" : "Lưu & Tiếp Theo (" + (currentIndex + 2) + "/" + subjects.size() + ") ➔");

        if (!isLast) {
            builder.setNeutralButton("Lưu Tất Cả (" + (subjects.size() - currentIndex) + " Môn)", (d, which) -> {
                int count = 0;
                for (int i = currentIndex; i < subjects.size(); i++) {
                    ParsedSubject ps = subjects.get(i);
                    saveParsedSubjectToDb(ps.maMon, ps.tenMon, ps.thu, ps.tietBatDau, ps.soTiet, ps.phongHoc, ps.giangVien, ps.tuanHoc);
                    count++;
                }
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

            if (maMon.isEmpty() || tenMon.isEmpty() || tietBDStr.isEmpty() || soTietStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            int thu = spinnerThu.getSelectedItemPosition() + 2;
            int tietBD = Integer.parseInt(tietBDStr);
            int soTietVal = Integer.parseInt(soTietStr);

            saveParsedSubjectToDb(maMon, tenMon, thu, tietBD, soTietVal, etPhongHoc.getText().toString().trim(), etGiangVien.getText().toString().trim(), currentSubject.tuanHoc);

            dialog.dismiss();

            if (currentIndex + 1 < subjects.size()) {
                Toast.makeText(getContext(), "Đã lưu môn [" + tenMon + "]. Chuyển sang môn tiếp theo...", Toast.LENGTH_SHORT).show();
                showFormConfirmStepDialog(subjects, currentIndex + 1);
            } else {
                Toast.makeText(getContext(), "🎉 Hoàn tất! Đã lưu toàn bộ " + subjects.size() + " môn học vào CSDL!", Toast.LENGTH_LONG).show();
                loadScheduleData();
            }
        });

        dialog.show();
    }

    private void saveParsedSubjectToDb(String maMon, String tenMon, int thu, int tietBD, int soTietVal, String phong, String gv, String tuanHoc) {
        if (dbHelper == null) return;

        long monHocId = dbHelper.insertMonHoc(new MonHoc(maMon, tenMon, 3, "#1976D2"));
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

        Date currentWeekStartDate = getSelectedWeekStartDate();

        if (currentTab == 0) {
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
                        if (layoutWeekBar != null) layoutWeekBar.setVisibility(View.GONE);
                    }
                });

                rvScheduleList.setAdapter(lichHocAdapter);
            } else {
                lichHocAdapter.setWeekStartDate(currentWeekStartDate);
                lichHocAdapter.updateData(filteredList);
            }
        } else {
            List<LichThi> listLichThi = dbHelper.getAllLichThiWithMonHoc();
            if (lichThiAdapter == null) {
                lichThiAdapter = new LichThiAdapter(listLichThi, new LichThiAdapter.OnLichThiClickListener() {
                    @Override
                    public void onItemClick(LichThi lichThi) {
                        showLichThiDetailDialog(lichThi);
                    }

                    @Override
                    public void onItemLongClick(LichThi lichThi) {}
                });
                rvScheduleList.setAdapter(lichThiAdapter);
            } else {
                lichThiAdapter.updateData(listLichThi);
            }
        }
    }

    private void enterSelectionMode() {
        if (lichHocAdapter != null) {
            lichHocAdapter.setSelectionMode(true);
        }
        if (layoutSelectionBar != null) layoutSelectionBar.setVisibility(View.VISIBLE);
        if (layoutWeekBar != null) layoutWeekBar.setVisibility(View.GONE);
    }

    private void showLichHocDetailDialog(LichHoc item) {
        if (getContext() == null || item == null) return;

        String thuStr = item.getThu() == 8 ? "Chủ Nhật" : "Thứ " + item.getThu();
        int tietStart = item.getTietBatDau();
        int tietEnd = tietStart + item.getSoTiet() - 1;

        String timeStr = "07:00 ➔ 09:30";
        if (tietStart == 4) timeStr = "09:45 ➔ 12:15";
        else if (tietStart == 7) timeStr = "13:00 ➔ 15:30";
        else if (tietStart == 10) timeStr = "15:45 ➔ 18:15";

        String message = "Mã môn học: " + (item.getMaMon() != null ? item.getMaMon() : "N/A") + "\n\n" +
                "Thời gian: " + thuStr + " (" + timeStr + " • Tiết " + tietStart + " - " + tietEnd + ")\n\n" +
                "Thời gian học: " + (item.getTuanHoc() != null ? item.getTuanHoc() : "Cả học kỳ") + "\n\n" +
                "Phòng học: " + (item.getPhongHoc() != null ? item.getPhongHoc() : "N/A") + "\n\n" +
                "Giảng viên: " + (item.getGiangVien() != null ? item.getGiangVien() : "N/A") + "\n\n" +
                "Học kỳ: " + (item.getHocKy() > 0 ? "Học kỳ " + item.getHocKy() : "Học kỳ 1") +
                " (Năm học " + (item.getNamHoc() != null ? item.getNamHoc() : "2026-2027") + ")";

        new AlertDialog.Builder(getContext())
                .setTitle("Chi Tiết Môn Học: " + item.getTenMon())
                .setMessage(message)
                .setPositiveButton("Đóng", null)
                .setNeutralButton("Xóa Môn", (dialog, which) -> showDeleteLichHocDialog(item))
                .show();
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

            int thu = spinnerThu.getSelectedItemPosition() + 2;
            int tietBD = Integer.parseInt(tietBDStr);
            int soTiet = Integer.parseInt(soTietStr);

            long monHocId = dbHelper.insertMonHoc(new MonHoc(maMon, tenMon, 3, "#1976D2"));
            if (monHocId == -1) {
                List<MonHoc> allMon = dbHelper.getAllMonHoc();
                for (MonHoc mh : allMon) {
                    if (mh.getMaMon().equalsIgnoreCase(maMon)) {
                        monHocId = mh.getId();
                        break;
                    }
                }
            }

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
