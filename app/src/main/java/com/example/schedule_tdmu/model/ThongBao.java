package com.example.schedule_tdmu.model;

public class ThongBao {
    private int id;
    private String tieuDe;
    private String noiDung;
    private String ngayDang;
    private String loaiThongBao;
    private int daDoc; // 0: chưa đọc, 1: đã đọc

    public ThongBao() {
    }

    public ThongBao(int id, String tieuDe, String noiDung, String ngayDang, String loaiThongBao, int daDoc) {
        this.id = id;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.ngayDang = ngayDang;
        this.loaiThongBao = loaiThongBao;
        this.daDoc = daDoc;
    }

    public ThongBao(String tieuDe, String noiDung, String ngayDang, String loaiThongBao) {
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.ngayDang = ngayDang;
        this.loaiThongBao = loaiThongBao;
        this.daDoc = 0;
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
        return loaiThongBao;
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
}
