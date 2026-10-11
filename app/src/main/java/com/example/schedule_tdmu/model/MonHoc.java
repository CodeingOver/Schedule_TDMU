package com.example.schedule_tdmu.model;

public class MonHoc {
    private int id;
    private String maMon;
    private String tenMon;
    private int soTinChi;
    private String mauSac;
    private String lopHoc;
    private String giangVien;
    private int tongSoTuan;
    private int soTuanDaHoc;
    private int soBuoiVang;
    private float diemQuaTrinh;
    private String trangThai;

    public MonHoc() {
        this.tongSoTuan = 15;
        this.soTuanDaHoc = 8;
        this.soBuoiVang = 0;
        this.diemQuaTrinh = -1f;
        this.lopHoc = "D24CNTT01";
        this.giangVien = "Giảng viên TDMU";
        this.trangThai = "Đang diễn ra";
    }

    public MonHoc(int id, String maMon, String tenMon, int soTinChi, String mauSac) {
        this();
        this.id = id;
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soTinChi = soTinChi;
        this.mauSac = mauSac;
    }

    public MonHoc(String maMon, String tenMon, int soTinChi, String mauSac) {
        this();
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soTinChi = soTinChi;
        this.mauSac = mauSac;
    }

    public MonHoc(int id, String maMon, String tenMon, int soTinChi, String mauSac,
                  String lopHoc, String giangVien, int tongSoTuan, int soTuanDaHoc,
                  int soBuoiVang, float diemQuaTrinh, String trangThai) {
        this.id = id;
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soTinChi = soTinChi;
        this.mauSac = mauSac;
        this.lopHoc = lopHoc;
        this.giangVien = giangVien;
        this.tongSoTuan = tongSoTuan > 0 ? tongSoTuan : 15;
        this.soTuanDaHoc = Math.max(0, soTuanDaHoc);
        this.soBuoiVang = Math.max(0, soBuoiVang);
        this.diemQuaTrinh = diemQuaTrinh;
        this.trangThai = trangThai != null ? trangThai : "Đang diễn ra";
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

    public String getLopHoc() {
        return lopHoc != null && !lopHoc.isEmpty() ? lopHoc : "D24CNTT01";
    }

    public void setLopHoc(String lopHoc) {
        this.lopHoc = lopHoc;
    }

    public String getGiangVien() {
        return giangVien != null && !giangVien.isEmpty() ? giangVien : "Giảng viên TDMU";
    }

    public void setGiangVien(String giangVien) {
        this.giangVien = giangVien;
    }

    public int getTongSoTuan() {
        return tongSoTuan > 0 ? tongSoTuan : 15;
    }

    public void setTongSoTuan(int tongSoTuan) {
        this.tongSoTuan = tongSoTuan;
    }

    public int getSoTuanDaHoc() {
        return Math.min(getTongSoTuan(), Math.max(0, soTuanDaHoc));
    }

    public void setSoTuanDaHoc(int soTuanDaHoc) {
        this.soTuanDaHoc = soTuanDaHoc;
    }

    public int getSoBuoiVang() {
        return Math.max(0, soBuoiVang);
    }

    public void setSoBuoiVang(int soBuoiVang) {
        this.soBuoiVang = soBuoiVang;
    }

    public float getDiemQuaTrinh() {
        return diemQuaTrinh;
    }

    public void setDiemQuaTrinh(float diemQuaTrinh) {
        this.diemQuaTrinh = diemQuaTrinh;
    }

    public boolean hasDiemQuaTrinh() {
        return diemQuaTrinh >= 0f;
    }

    public String getTrangThai() {
        return trangThai != null && !trangThai.isEmpty() ? trangThai : "Đang diễn ra";
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public int getTiLeChuyenCan() {
        int totalSessions = getTongSoTuan();
        int attended = Math.max(0, totalSessions - getSoBuoiVang());
        return Math.min(100, Math.round((attended * 100f) / totalSessions));
    }

    public int getTienDoPhanTram() {
        return Math.min(100, Math.round((getSoTuanDaHoc() * 100f) / getTongSoTuan()));
    }
}
