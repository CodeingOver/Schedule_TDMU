package com.example.schedule_tdmu.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LichHoc {
    private static final Pattern DATE_RANGE_PATTERN = Pattern.compile("([0-9]{1,2}/[0-9]{1,2}/[0-9]{2,4})\\s*đến\\s*([0-9]{1,2}/[0-9]{1,2}/[0-9]{2,4})", Pattern.CASE_INSENSITIVE);

    private int id;
    private int monHocId;
    private int thu; // 2: Thứ 2, 3: Thứ 3,... 8: Chủ Nhật
    private int tietBatDau;
    private int soTiet;
    private String phongHoc;
    private String giangVien;
    private int hocKy;
    private String namHoc;
    private String tuanHoc;

    // Cache ngày bắt đầu & ngày kết thúc để tăng tốc tối đa
    private transient Date parsedStartDate = null;
    private transient Date parsedEndDate = null;
    private transient boolean datesParsed = false;

    // Các thuộc tính dùng khi JOIN với bảng MonHoc
    private String maMon;
    private String tenMon;
    private String mauSac;

    public LichHoc() {
    }

    public LichHoc(int id, int monHocId, int thu, int tietBatDau, int soTiet, String phongHoc, String giangVien, int hocKy, String namHoc, String tuanHoc) {
        this.id = id;
        this.monHocId = monHocId;
        this.thu = thu;
        this.tietBatDau = tietBatDau;
        this.soTiet = soTiet;
        this.phongHoc = phongHoc;
        this.giangVien = giangVien;
        this.hocKy = hocKy;
        this.namHoc = namHoc;
        this.tuanHoc = tuanHoc;
    }

    public LichHoc(int monHocId, int thu, int tietBatDau, int soTiet, String phongHoc, String giangVien, int hocKy, String namHoc, String tuanHoc) {
        this.monHocId = monHocId;
        this.thu = thu;
        this.tietBatDau = tietBatDau;
        this.soTiet = soTiet;
        this.phongHoc = phongHoc;
        this.giangVien = giangVien;
        this.hocKy = hocKy;
        this.namHoc = namHoc;
        this.tuanHoc = tuanHoc;
    }

    private void parseDatesIfNeeded() {
        if (datesParsed) return;
        datesParsed = true;
        if (tuanHoc == null || tuanHoc.trim().isEmpty()) return;

        try {
            Matcher matcher = DATE_RANGE_PATTERN.matcher(tuanHoc);
            if (matcher.find()) {
                String startStr = matcher.group(1);
                String endStr = matcher.group(2);

                if (startStr != null && endStr != null) {
                    SimpleDateFormat sdfShort = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    SimpleDateFormat sdfLong = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    parsedStartDate = (startStr.length() <= 8) ? sdfShort.parse(startStr) : sdfLong.parse(startStr);
                    parsedEndDate = (endStr.length() <= 8) ? sdfShort.parse(endStr) : sdfLong.parse(endStr);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public boolean isClassActiveOnDate(Date targetDate) {
        if (targetDate == null) return true;
        parseDatesIfNeeded();
        if (parsedStartDate == null || parsedEndDate == null) return true;

        long targetMs = targetDate.getTime();
        long startMs = parsedStartDate.getTime() - 86400000L; // Biên Lùi 1 ngày
        long endMs = parsedEndDate.getTime() + 86400000L;    // Biên Tiến 1 ngày

        return targetMs >= startMs && targetMs <= endMs;
    }

    public boolean isClassActiveInWeek(Date weekStartDate) {
        if (weekStartDate == null) return true;
        parseDatesIfNeeded();
        if (parsedStartDate == null || parsedEndDate == null) return true;

        Calendar calWeekEnd = Calendar.getInstance();
        calWeekEnd.setTime(weekStartDate);
        calWeekEnd.add(Calendar.DAY_OF_YEAR, 6);

        long weekStartMs = weekStartDate.getTime() - 86400000L;
        long weekEndMs = calWeekEnd.getTimeInMillis() + 86400000L;

        long startMs = parsedStartDate.getTime();
        long endMs = parsedEndDate.getTime();

        return !(endMs < weekStartMs || startMs > weekEndMs);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMonHocId() {
        return monHocId;
    }

    public void setMonHocId(int monHocId) {
        this.monHocId = monHocId;
    }

    public int getThu() {
        return thu;
    }

    public void setThu(int thu) {
        this.thu = thu;
    }

    public int getTietBatDau() {
        return tietBatDau;
    }

    public void setTietBatDau(int tietBatDau) {
        this.tietBatDau = tietBatDau;
    }

    public int getSoTiet() {
        return soTiet;
    }

    public void setSoTiet(int soTiet) {
        this.soTiet = soTiet;
    }

    public String getPhongHoc() {
        return phongHoc;
    }

    public void setPhongHoc(String phongHoc) {
        this.phongHoc = phongHoc;
    }

    public String getGiangVien() {
        return giangVien;
    }

    public void setGiangVien(String giangVien) {
        this.giangVien = giangVien;
    }

    public int getHocKy() {
        return hocKy;
    }

    public void setHocKy(int hocKy) {
        this.hocKy = hocKy;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    public String getTuanHoc() {
        return tuanHoc;
    }

    public void setTuanHoc(String tuanHoc) {
        this.tuanHoc = tuanHoc;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public String getMauSac() {
        return mauSac;
    }

    public void setMauSac(String mauSac) {
        this.mauSac = mauSac;
    }
}
