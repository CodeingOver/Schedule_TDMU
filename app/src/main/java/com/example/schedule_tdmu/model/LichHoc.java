package com.example.schedule_tdmu.model;

public class LichHoc {
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
