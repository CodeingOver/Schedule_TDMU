package com.example.schedule_tdmu.model;

public class MonHoc {
    private int id;
    private String maMon;
    private String tenMon;
    private int soTinChi;
    private String mauSac;

    public MonHoc() {
    }

    public MonHoc(int id, String maMon, String tenMon, int soTinChi, String mauSac) {
        this.id = id;
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soTinChi = soTinChi;
        this.mauSac = mauSac;
    }

    public MonHoc(String maMon, String tenMon, int soTinChi, String mauSac) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soTinChi = soTinChi;
        this.mauSac = mauSac;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getSoTinChi() {
        return soTinChi;
    }

    public void setSoTinChi(int soTinChi) {
        this.soTinChi = soTinChi;
    }

    public String getMauSac() {
        return mauSac;
    }

    public void setMauSac(String mauSac) {
        this.mauSac = mauSac;
    }
}
