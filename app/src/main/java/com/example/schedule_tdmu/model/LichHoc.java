package com.example.schedule_tdmu.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LichHoc {
    private static final Pattern DATE_RANGE_PATTERN = Pattern.compile("([0-9]{1,2}/[0-9]{1,2}/[0-9]{2,4})\\s*(?:đến|-|->|to)\\s*([0-9]{1,2}/[0-9]{1,2}/[0-9]{2,4})", Pattern.CASE_INSENSITIVE);

    public static class DateRange {
        public final long startDay;
        public final long endDay;

        public DateRange(long startDay, long endDay) {
            this.startDay = startDay;
            this.endDay = endDay;
        }
    }

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

    // Cache danh sách các khoảng ngày học để tăng tốc tối đa
    private transient List<DateRange> parsedRanges = null;
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
        parsedRanges = new ArrayList<>();
        if (tuanHoc == null || tuanHoc.trim().isEmpty()) return;

        try {
            Matcher matcher = DATE_RANGE_PATTERN.matcher(tuanHoc);
            SimpleDateFormat sdfShort = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            SimpleDateFormat sdfLong = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            while (matcher.find()) {
                String startStr = matcher.group(1);
                String endStr = matcher.group(2);

                if (startStr != null && endStr != null) {
                    Date sDate = (startStr.length() <= 8) ? sdfShort.parse(startStr) : sdfLong.parse(startStr);
                    Date eDate = (endStr.length() <= 8) ? sdfShort.parse(endStr) : sdfLong.parse(endStr);
                    if (sDate != null && eDate != null) {
                        long sDay = startOfDay(sDate);
                        long eDay = startOfDay(eDate);
                        parsedRanges.add(new DateRange(sDay, eDay));
                        if (parsedStartDate == null || sDate.before(parsedStartDate)) {
                            parsedStartDate = sDate;
                        }
                        if (parsedEndDate == null || eDate.after(parsedEndDate)) {
                            parsedEndDate = eDate;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static long startOfDay(Date date) {
        if (date == null) return 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Kiem tra xem ngay targetDate co nam trong khoang thoi gian hoc cua mon hoc hay khong.
     * So sanh chuan xac theo moc 00:00:00 cua tung ngay, tuyet doi khong dung sai so buffer lam lem ngay.
     */
    public boolean isClassActiveOnDate(Date targetDate) {
        if (targetDate == null) return true;
        parseDatesIfNeeded();
        if (parsedRanges == null || parsedRanges.isEmpty()) return true;

        long targetDay = startOfDay(targetDate);
        for (DateRange range : parsedRanges) {
            if (targetDay >= range.startDay && targetDay <= range.endDay) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiem tra chinh xac xem lop hoc co dien ra vao dung ngay date hay khong:
     * 1. Thu cua date phai trung voi thu cua LichHoc.
     * 2. Ngay date phai nam trong khoang thoi gian hoc (parsedRanges).
     */
    public boolean occursOnDate(Date date) {
        if (date == null) return false;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dow = c.get(Calendar.DAY_OF_WEEK);
        int thuOfDate = (dow == Calendar.SUNDAY) ? 8 : dow;
        if (thuOfDate != thu) return false;

        return isClassActiveOnDate(date);
    }

    /**
     * Kiem tra xem trong tuan hoc bat dau tu weekStartDate (Thu 2), mon hoc co buoi hoc nao khong.
     * Tinh chinh xac ngay dien ra cua mon hoc trong tuan nay (weekStartDate + dayOffset)
     * va kiem tra xem ngay do co nam trong thoi gian hoc hay khong.
     */
    public boolean isClassActiveInWeek(Date weekStartDate) {
        if (weekStartDate == null) return true;
        Calendar cal = Calendar.getInstance();
        cal.setTime(weekStartDate);
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        int dayOffset = (thu == 8) ? 6 : Math.max(0, thu - 2);
        cal.add(Calendar.DAY_OF_YEAR, dayOffset);
        Date classDateInWeek = cal.getTime();

        return occursOnDate(classDateInWeek);
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
