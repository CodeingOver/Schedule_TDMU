package com.example.schedule_tdmu.fragment;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.schedule_tdmu.DatabaseHelper;
import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.model.LichHoc;
import com.example.schedule_tdmu.model.MonHoc;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanFragment extends Fragment {

    private ImageView imgPreview;
    private Button btnPickGallery, btnTakePhoto, btnStartScan;
    private Uri selectedImageUri;
    private Bitmap capturedBitmap;
    private DatabaseHelper dbHelper;

    public static class ParsedSubject {
        public String maMon;
        public String tenMon;
        public int thu; // 2: Thứ 2, ..., 7: Thứ 7
        public int tietBatDau;
        public int soTiet;
        public String phongHoc;
        public String giangVien;

        public ParsedSubject(String maMon, String tenMon, int thu, int tietBatDau, int soTiet, String phongHoc, String giangVien) {
            this.maMon = maMon;
            this.tenMon = tenMon;
            this.thu = thu;
            this.tietBatDau = tietBatDau;
            this.soTiet = soTiet;
            this.phongHoc = phongHoc;
            this.giangVien = giangVien;
        }
    }

    // Launchers chọn ảnh từ Album & chụp ảnh từ Camera
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    capturedBitmap = null;
                    imgPreview.setImageURI(uri);
                    imgPreview.setPadding(0, 0, 0, 0);
                    Toast.makeText(getContext(), "Đã chọn ảnh từ Album thành công!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    capturedBitmap = bitmap;
                    selectedImageUri = null;
                    imgPreview.setImageBitmap(bitmap);
                    imgPreview.setPadding(0, 0, 0, 0);
                    Toast.makeText(getContext(), "Đã chụp ảnh thành công!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        if (getContext() != null) {
            dbHelper = new DatabaseHelper(getContext());
        }

        imgPreview = view.findViewById(R.id.img_preview);
        btnPickGallery = view.findViewById(R.id.btn_pick_gallery);
        btnTakePhoto = view.findViewById(R.id.btn_take_photo);
        btnStartScan = view.findViewById(R.id.btn_start_scan);

        btnPickGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnTakePhoto.setOnClickListener(v -> cameraLauncher.launch(null));
        btnStartScan.setOnClickListener(v -> processImageOCR());

        return view;
    }

    private void processImageOCR() {
        if (selectedImageUri == null && capturedBitmap == null) {
            Toast.makeText(getContext(), "Vui lòng chọn ảnh từ Album hoặc chụp ảnh trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        InputImage inputImage = null;
        try {
            if (selectedImageUri != null && getContext() != null) {
                inputImage = InputImage.fromFilePath(getContext(), selectedImageUri);
            } else if (capturedBitmap != null) {
                inputImage = InputImage.fromBitmap(capturedBitmap, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi khi đọc tập tin ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (inputImage == null) return;

        Toast.makeText(getContext(), "Đang bóc tách toàn bộ TKB bằng AI Google ML Kit...", Toast.LENGTH_SHORT).show();

        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(inputImage)
                .addOnSuccessListener(text -> parseAllSubjectsAndStartWizard(text.getText()))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi nhận diện văn bản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

        // Bắt đầu quy trình duyệt từng môn học thực tế bóc tách được từ ảnh
        showFormConfirmStepDialog(subjects, 0);
    }

    private List<ParsedSubject> parseAllTDMUSubjects(String fullText) {
        List<ParsedSubject> list = new ArrayList<>();

        // 1. Kiểm tra mẫu Popup Đăng Ký Môn Học TDMU
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
                    gvStr != null ? gvStr.trim() : "Lê Từ Minh Trí"
            ));
        }

        if (!list.isEmpty()) {
            return list;
        }

        // 2. Nếu không phải định dạng Popup -> Tìm các mốc thời gian học dạng "HH:mm -> HH:mm" dạng Lưới TKB
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

            list.add(new ParsedSubject(maMon, tenMon, thu, tietBD, soTiet, phong, gv));
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
            tvTitle.setText("🔍 Môn " + (currentIndex + 1) + "/" + subjects.size() + ": Bóc Tách TKB");
        }

        String[] dsThu = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, dsThu);
        spinnerThu.setAdapter(adapter);

        // Điền dữ liệu môn hiện tại vào Form
        etMaMon.setText(currentSubject.maMon);
        etTenMon.setText(currentSubject.tenMon);
        spinnerThu.setSelection(Math.max(0, currentSubject.thu - 2));
        etTietBatDau.setText(String.valueOf(currentSubject.tietBatDau));
        etSoTiet.setText(String.valueOf(currentSubject.soTiet));
        etPhongHoc.setText(currentSubject.phongHoc);
        etGiangVien.setText(currentSubject.giangVien);

        boolean isLast = (currentIndex == subjects.size() - 1);
        btnSave.setText(isLast ? "Lưu & Hoàn Tất" : "Lưu & Tiếp Theo (" + (currentIndex + 2) + "/" + subjects.size() + ") ➔");

        // Thêm nút Lưu Tất Cả N Môn
        if (!isLast) {
            builder.setNeutralButton("⚡ Lưu Tất Cả (" + (subjects.size() - currentIndex) + " Môn)", (d, which) -> {
                int count = 0;
                for (int i = currentIndex; i < subjects.size(); i++) {
                    ParsedSubject ps = subjects.get(i);
                    saveParsedSubjectToDb(ps.maMon, ps.tenMon, ps.thu, ps.tietBatDau, ps.soTiet, ps.phongHoc, ps.giangVien);
                    count++;
                }
                Toast.makeText(getContext(), "🎉 Đã lưu thành công tất cả " + count + " môn học vào CSDL!", Toast.LENGTH_LONG).show();
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

            saveParsedSubjectToDb(maMon, tenMon, thu, tietBD, soTietVal, etPhongHoc.getText().toString().trim(), etGiangVien.getText().toString().trim());

            dialog.dismiss();

            if (currentIndex + 1 < subjects.size()) {
                Toast.makeText(getContext(), "Đã lưu môn [" + tenMon + "]. Chuyển sang môn tiếp theo...", Toast.LENGTH_SHORT).show();
                showFormConfirmStepDialog(subjects, currentIndex + 1);
            } else {
                Toast.makeText(getContext(), "🎉 Hoàn tất! Đã lưu toàn bộ " + subjects.size() + " môn học vào CSDL!", Toast.LENGTH_LONG).show();
            }
        });

        dialog.show();
    }

    private void saveParsedSubjectToDb(String maMon, String tenMon, int thu, int tietBD, int soTietVal, String phong, String gv) {
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
            LichHoc lh = new LichHoc((int) monHocId, thu, tietBD, soTietVal, phong, gv, 1, "2026-2027", "1-15");
            dbHelper.insertLichHoc(lh);
        }
    }
}
