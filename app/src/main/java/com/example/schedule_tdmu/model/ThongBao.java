package com.example.schedule_tdmu.model;

public class ThongBao {
    private int id;
    private String tieuDe;
    private String noiDung;
    private String ngayDang;
    private String loaiThongBao; // URGENT, EXAM, SCHEDULE, GENERAL
    private int daDoc; // 0: chưa đọc, 1: đã đọc
    private String nguonThongBao;
    private String phongCu;
    private String phongMoi;

    public ThongBao() {
    }

    public ThongBao(int id, String tieuDe, String noiDung, String ngayDang, String loaiThongBao, int daDoc) {
        this(id, tieuDe, noiDung, ngayDang, loaiThongBao, daDoc, "Khoa CNTT", "", "");
    }

    public ThongBao(int id, String tieuDe, String noiDung, String ngayDang, String loaiThongBao,
                    int daDoc, String nguonThongBao, String phongCu, String phongMoi) {
        this.id = id;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.ngayDang = ngayDang;
        this.loaiThongBao = loaiThongBao;
        this.daDoc = daDoc;
        this.nguonThongBao = nguonThongBao;
        this.phongCu = phongCu;
        this.phongMoi = phongMoi;
    }

    public ThongBao(String tieuDe, String noiDung, String ngayDang, String loaiThongBao) {
        this(0, tieuDe, noiDung, ngayDang, loaiThongBao, 0, "Khoa CNTT", "", "");
    }

    public ThongBao(String tieuDe, String noiDung, String ngayDang, String loaiThongBao,
                    String nguonThongBao, String phongCu, String phongMoi) {
        this(0, tieuDe, noiDung, ngayDang, loaiThongBao, 0, nguonThongBao, phongCu, phongMoi);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getNgayDang() {
        return ngayDang;
    }

    public void setNgayDang(String ngayDang) {
        this.ngayDang = ngayDang;
    }

    public String getLoaiThongBao() {
        return loaiThongBao != null ? loaiThongBao : "Chung";
    }

    public void setLoaiThongBao(String loaiThongBao) {
        this.loaiThongBao = loaiThongBao;
    }

    public int getDaDoc() {
        return daDoc;
    }

    public void setDaDoc(int daDoc) {
        this.daDoc = daDoc;
    }

    public boolean isDaDoc() {
        return daDoc == 1;
    }

    public String getNguonThongBao() {
        return nguonThongBao != null && !nguonThongBao.isEmpty() ? nguonThongBao : "Phòng Đào tạo TDMU";
    }

    public void setNguonThongBao(String nguonThongBao) {
        this.nguonThongBao = nguonThongBao;
    }

    public String getPhongCu() {
        return phongCu != null ? phongCu : "";
    }

    public void setPhongCu(String phongCu) {
        this.phongCu = phongCu;
    }

    public String getPhongMoi() {
        return phongMoi != null ? phongMoi : "";
    }

    public void setPhongMoi(String phongMoi) {
        this.phongMoi = phongMoi;
    }
}
