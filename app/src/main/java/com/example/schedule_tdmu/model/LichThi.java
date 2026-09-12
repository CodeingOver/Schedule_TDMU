package com.example.schedule_tdmu.model;

public class LichThi {
    private int id;
    private int monHocId;
    private String ngayGioThi; // Định dạng "YYYY-MM-DD HH:mm"
    private String phongThi;
    private String hinhThucThi;
    private String sbd;
    private String ghiChu;

    // Thuộc tính dùng khi JOIN với MonHoc
    private String maMon;
    private String tenMon;

    public LichThi() {
    }

    public LichThi(int id, int monHocId, String ngayGioThi, String phongThi, String hinhThucThi, String sbd, String ghiChu) {
        this.id = id;
        this.monHocId = monHocId;
        this.ngayGioThi = ngayGioThi;
        this.phongThi = phongThi;
        this.hinhThucThi = hinhThucThi;
        this.sbd = sbd;
        this.ghiChu = ghiChu;
    }

    public LichThi(int monHocId, String ngayGioThi, String phongThi, String hinhThucThi, String sbd, String ghiChu) {
        this.monHocId = monHocId;
        this.ngayGioThi = ngayGioThi;
        this.phongThi = phongThi;
        this.hinhThucThi = hinhThucThi;
        this.sbd = sbd;
        this.ghiChu = ghiChu;
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

    public String getNgayGioThi() {
        return ngayGioThi;
    }

    public void setNgayGioThi(String ngayGioThi) {
        this.ngayGioThi = ngayGioThi;
    }

    public String getPhongThi() {
        return phongThi;
    }

    public void setPhongThi(String phongThi) {
        this.phongThi = phongThi;
    }

    public String getHinhThucThi() {
        return hinhThucThi;
    }

    public void setHinhThucThi(String hinhThucThi) {
        this.hinhThucThi = hinhThucThi;
    }

    public String getSbd() {
        return sbd;
    }

    public void setSbd(String sbd) {
        this.sbd = sbd;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
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
}
