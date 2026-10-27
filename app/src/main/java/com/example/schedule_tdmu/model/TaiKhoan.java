package com.example.schedule_tdmu.model;

public class TaiKhoan {
    private int id;
    private String mssv;
    private String email;
    private String matKhau;
    private String hoTen;
    private String lopHoc;
    private String khoaVien;
    private String vaiTro; // "student" hoặc "admin"
    private String ngayTao;

    public TaiKhoan() {
    }

    public TaiKhoan(int id, String mssv, String email, String matKhau, String hoTen, String lopHoc, String khoaVien, String vaiTro, String ngayTao) {
        this.id = id;
        this.mssv = mssv;
        this.email = email;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.lopHoc = lopHoc;
        this.khoaVien = khoaVien;
        this.vaiTro = vaiTro;
        this.ngayTao = ngayTao;
    }

    public TaiKhoan(String mssv, String email, String matKhau, String hoTen, String lopHoc, String khoaVien, String vaiTro, String ngayTao) {
        this.mssv = mssv;
        this.email = email;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.lopHoc = lopHoc;
        this.khoaVien = khoaVien;
        this.vaiTro = vaiTro;
        this.ngayTao = ngayTao;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMssv() {
        return mssv;
    }

    public void setMssv(String mssv) {
        this.mssv = mssv;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getLopHoc() {
        return lopHoc;
    }

    public void setLopHoc(String lopHoc) {
        this.lopHoc = lopHoc;
    }

    public String getKhoaVien() {
        return khoaVien;
    }

    public void setKhoaVien(String khoaVien) {
        this.khoaVien = khoaVien;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(String ngayTao) {
        this.ngayTao = ngayTao;
    }
}
