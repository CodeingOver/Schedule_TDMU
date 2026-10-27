package com.example.schedule_tdmu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.schedule_tdmu.model.LichHoc;
import com.example.schedule_tdmu.model.LichThi;
import com.example.schedule_tdmu.model.MonHoc;
import com.example.schedule_tdmu.model.TaiKhoan;
import com.example.schedule_tdmu.model.ThongBao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ScheduleTDMU.db";
    private static final int DATABASE_VERSION = 5;

    // Bảng TaiKhoan
    public static final String TABLE_TAI_KHOAN = "TaiKhoan";
    public static final String COL_TK_ID = "id";
    public static final String COL_TK_MSSV = "mssv";
    public static final String COL_TK_EMAIL = "email";
    public static final String COL_TK_MAT_KHAU = "mat_khau";
    public static final String COL_TK_HO_TEN = "ho_ten";
    public static final String COL_TK_LOP_HOC = "lop_hoc";
    public static final String COL_TK_KHOA_VIEN = "khoa_vien";
    public static final String COL_TK_VAI_TRO = "vai_tro";
    public static final String COL_TK_NGAY_TAO = "ngay_tao";

    // Bảng MonHoc
    public static final String TABLE_MON_HOC = "MonHoc";
    public static final String COL_MON_HOC_ID = "id";
    public static final String COL_MA_MON = "ma_mon";
    public static final String COL_TEN_MON = "ten_mon";
    public static final String COL_SO_TIN_CHI = "so_tin_chi";
    public static final String COL_MAU_SAC = "mau_sac";
    public static final String COL_LOP_HOC = "lop_hoc";
    public static final String COL_MH_GIANG_VIEN = "giang_vien";
    public static final String COL_TONG_SO_TUAN = "tong_so_tuan";
    public static final String COL_SO_TUAN_DA_HOC = "so_tuan_da_hoc";
    public static final String COL_SO_BUOI_VANG = "so_buoi_vang";
    public static final String COL_DIEM_QUA_TRINH = "diem_qua_trinh";
    public static final String COL_TRANG_THAI = "trang_thai";

    // Bảng LichHoc
    public static final String TABLE_LICH_HOC = "LichHoc";
    public static final String COL_LICH_HOC_ID = "id";
    public static final String COL_LH_MON_HOC_ID = "mon_hoc_id";
    public static final String COL_THU = "thu";
    public static final String COL_TIET_BAT_DAU = "tiet_bat_dau";
    public static final String COL_SO_TIET = "so_tiet";
    public static final String COL_PHONG_HOC = "phong_hoc";
    public static final String COL_GIANG_VIEN = "giang_vien";
    public static final String COL_HOC_KY = "hoc_ky";
    public static final String COL_NAM_HOC = "nam_hoc";
    public static final String COL_TUAN_HOC = "tuan_hoc";

    // Bảng LichThi
    public static final String TABLE_LICH_THI = "LichThi";
    public static final String COL_LICH_THI_ID = "id";
    public static final String COL_LT_MON_HOC_ID = "mon_hoc_id";
    public static final String COL_NGAY_GIO_THI = "ngay_gio_thi";
    public static final String COL_PHONG_THI = "phong_thi";
    public static final String COL_HINH_THUC_THI = "hinh_thuc_thi";
    public static final String COL_SBD = "sbd";
    public static final String COL_GHI_CHU_THI = "ghi_chu";

    // Bảng ThongBao
    public static final String TABLE_THONG_BAO = "ThongBao";
    public static final String COL_THONG_BAO_ID = "id";
    public static final String COL_TIEU_DE = "tieu_de";
    public static final String COL_NOI_DUNG = "noi_dung";
    public static final String COL_NGAY_DANG = "ngay_dang";
    public static final String COL_LOAI_THONG_BAO = "loai_thong_bao";
    public static final String COL_DA_DOC = "da_doc";
    public static final String COL_NGUON_TB = "nguon_thong_bao";
    public static final String COL_PHONG_CU = "phong_cu";
    public static final String COL_PHONG_MOI = "phong_moi";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        ensureTaiKhoanTable(db);
        cleanDuplicateLichHoc(db);
        seedDefaultAccounts(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_TAI_KHOAN = "CREATE TABLE " + TABLE_TAI_KHOAN + " (" +
                COL_TK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TK_MSSV + " TEXT UNIQUE, " +
                COL_TK_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COL_TK_MAT_KHAU + " TEXT NOT NULL, " +
                COL_TK_HO_TEN + " TEXT NOT NULL, " +
                COL_TK_LOP_HOC + " TEXT DEFAULT '', " +
                COL_TK_KHOA_VIEN + " TEXT DEFAULT 'Viện Kỹ thuật - Công nghệ', " +
                COL_TK_VAI_TRO + " TEXT DEFAULT 'student', " +
                COL_TK_NGAY_TAO + " TEXT);";

        String CREATE_TABLE_MON_HOC = "CREATE TABLE " + TABLE_MON_HOC + " (" +
                COL_MON_HOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MA_MON + " TEXT NOT NULL UNIQUE, " +
                COL_TEN_MON + " TEXT NOT NULL, " +
                COL_SO_TIN_CHI + " INTEGER DEFAULT 3, " +
                COL_MAU_SAC + " TEXT DEFAULT '#3525CD', " +
                COL_LOP_HOC + " TEXT DEFAULT '', " +
                COL_MH_GIANG_VIEN + " TEXT DEFAULT '', " +
                COL_TONG_SO_TUAN + " INTEGER DEFAULT 15, " +
                COL_SO_TUAN_DA_HOC + " INTEGER DEFAULT 0, " +
                COL_SO_BUOI_VANG + " INTEGER DEFAULT 0, " +
                COL_DIEM_QUA_TRINH + " REAL DEFAULT -1, " +
                COL_TRANG_THAI + " TEXT DEFAULT 'Đang diễn ra');";

        String CREATE_TABLE_LICH_HOC = "CREATE TABLE " + TABLE_LICH_HOC + " (" +
                COL_LICH_HOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_LH_MON_HOC_ID + " INTEGER NOT NULL, " +
                COL_THU + " INTEGER NOT NULL, " +
                COL_TIET_BAT_DAU + " INTEGER NOT NULL, " +
                COL_SO_TIET + " INTEGER NOT NULL, " +
                COL_PHONG_HOC + " TEXT, " +
                COL_GIANG_VIEN + " TEXT, " +
                COL_HOC_KY + " INTEGER DEFAULT 1, " +
                COL_NAM_HOC + " TEXT, " +
                COL_TUAN_HOC + " TEXT, " +
                "FOREIGN KEY(" + COL_LH_MON_HOC_ID + ") REFERENCES " +
                TABLE_MON_HOC + "(" + COL_MON_HOC_ID + ") ON DELETE CASCADE);";

        String CREATE_TABLE_LICH_THI = "CREATE TABLE " + TABLE_LICH_THI + " (" +
                COL_LICH_THI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_LT_MON_HOC_ID + " INTEGER NOT NULL, " +
                COL_NGAY_GIO_THI + " TEXT NOT NULL, " +
                COL_PHONG_THI + " TEXT, " +
                COL_HINH_THUC_THI + " TEXT, " +
                COL_SBD + " TEXT, " +
                COL_GHI_CHU_THI + " TEXT, " +
                "FOREIGN KEY(" + COL_LT_MON_HOC_ID + ") REFERENCES " +
                TABLE_MON_HOC + "(" + COL_MON_HOC_ID + ") ON DELETE CASCADE);";

        String CREATE_TABLE_THONG_BAO = "CREATE TABLE " + TABLE_THONG_BAO + " (" +
                COL_THONG_BAO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TIEU_DE + " TEXT NOT NULL, " +
                COL_NOI_DUNG + " TEXT, " +
                COL_NGAY_DANG + " TEXT, " +
                COL_LOAI_THONG_BAO + " TEXT, " +
                COL_DA_DOC + " INTEGER DEFAULT 0, " +
                COL_NGUON_TB + " TEXT DEFAULT '', " +
                COL_PHONG_CU + " TEXT DEFAULT '', " +
                COL_PHONG_MOI + " TEXT DEFAULT '');";

        db.execSQL(CREATE_TABLE_TAI_KHOAN);
        db.execSQL(CREATE_TABLE_MON_HOC);
        db.execSQL(CREATE_TABLE_LICH_HOC);
        db.execSQL(CREATE_TABLE_LICH_THI);
        db.execSQL(CREATE_TABLE_THONG_BAO);

        seedDefaultAccounts(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAI_KHOAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LICH_THI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LICH_HOC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MON_HOC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_THONG_BAO);
        onCreate(db);
    }

    private void ensureTaiKhoanTable(SQLiteDatabase db) {
        if (db == null || db.isReadOnly()) return;
        try {
            String CREATE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS " + TABLE_TAI_KHOAN + " (" +
                    COL_TK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TK_MSSV + " TEXT UNIQUE, " +
                    COL_TK_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    COL_TK_MAT_KHAU + " TEXT NOT NULL, " +
                    COL_TK_HO_TEN + " TEXT NOT NULL, " +
                    COL_TK_LOP_HOC + " TEXT DEFAULT '', " +
                    COL_TK_KHOA_VIEN + " TEXT DEFAULT 'Viện Kỹ thuật - Công nghệ', " +
                    COL_TK_VAI_TRO + " TEXT DEFAULT 'student', " +
                    COL_TK_NGAY_TAO + " TEXT);";
            db.execSQL(CREATE_IF_NOT_EXISTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seedDefaultAccounts(SQLiteDatabase db) {
        if (db == null || db.isReadOnly()) return;
        try {
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TAI_KHOAN, null);
            int count = 0;
            if (cursor != null) {
                if (cursor.moveToFirst()) count = cursor.getInt(0);
                cursor.close();
            }
            if (count == 0) {
                // 1. Sinh viên mẫu Nguyễn Minh Anh
                ContentValues v1 = new ContentValues();
                v1.put(COL_TK_MSSV, "21020485");
                v1.put(COL_TK_EMAIL, "21020485@student.tdmu.edu.vn");
                v1.put(COL_TK_MAT_KHAU, "123456");
                v1.put(COL_TK_HO_TEN, "Nguyễn Minh Anh");
                v1.put(COL_TK_LOP_HOC, "D21CNTT01");
                v1.put(COL_TK_KHOA_VIEN, "Viện Kỹ thuật - Công nghệ");
                v1.put(COL_TK_VAI_TRO, "student");
                v1.put(COL_TK_NGAY_TAO, "2026-10-15 00:00:00");
                db.insert(TABLE_TAI_KHOAN, null, v1);

                // 2. Tài khoản Quản trị viên
                ContentValues v2 = new ContentValues();
                v2.put(COL_TK_MSSV, "admin");
                v2.put(COL_TK_EMAIL, "admin@tdmu.edu.vn");
                v2.put(COL_TK_MAT_KHAU, "admin123");
                v2.put(COL_TK_HO_TEN, "Quản trị viên TDMU");
                v2.put(COL_TK_LOP_HOC, "Phòng Đào tạo");
                v2.put(COL_TK_KHOA_VIEN, "Ban Giám hiệu & Quản trị");
                v2.put(COL_TK_VAI_TRO, "admin");
                v2.put(COL_TK_NGAY_TAO, "2026-10-15 00:00:00");
                db.insert(TABLE_TAI_KHOAN, null, v2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long insertMonHocInternal(SQLiteDatabase db, MonHoc monHoc) {
        ContentValues values = new ContentValues();
        values.put(COL_MA_MON, monHoc.getMaMon());
        values.put(COL_TEN_MON, monHoc.getTenMon());
        values.put(COL_SO_TIN_CHI, monHoc.getSoTinChi());
        values.put(COL_MAU_SAC, monHoc.getMauSac());
        values.put(COL_LOP_HOC, monHoc.getLopHoc());
        values.put(COL_MH_GIANG_VIEN, monHoc.getGiangVien());
        values.put(COL_TONG_SO_TUAN, monHoc.getTongSoTuan());
        values.put(COL_SO_TUAN_DA_HOC, monHoc.getSoTuanDaHoc());
        values.put(COL_SO_BUOI_VANG, monHoc.getSoBuoiVang());
        values.put(COL_DIEM_QUA_TRINH, monHoc.getDiemQuaTrinh());
        values.put(COL_TRANG_THAI, monHoc.getTrangThai());
        return db.insertWithOnConflict(TABLE_MON_HOC, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void cleanDuplicateLichHoc() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            cleanDuplicateLichHoc(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cleanDuplicateLichHoc(SQLiteDatabase db) {
        if (db == null || db.isReadOnly()) return;
        try {
            String sql = "DELETE FROM " + TABLE_LICH_HOC + " WHERE " + COL_LICH_HOC_ID + " NOT IN (" +
                    "SELECT MIN(" + COL_LICH_HOC_ID + ") FROM " + TABLE_LICH_HOC +
                    " GROUP BY " + COL_LH_MON_HOC_ID + ", " + COL_THU + ", " + COL_TIET_BAT_DAU + ", " +
                    COL_SO_TIET + ", COALESCE(" + COL_PHONG_HOC + ", ''), COALESCE(" + COL_TUAN_HOC + ", '')" +
                    ")";
            db.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isLichHocDuplicate(SQLiteDatabase db, LichHoc lichHoc) {
        if (db == null || lichHoc == null) return false;
        String selection = COL_LH_MON_HOC_ID + " = ? AND " +
                COL_THU + " = ? AND " +
                COL_TIET_BAT_DAU + " = ? AND " +
                COL_SO_TIET + " = ? AND " +
                "COALESCE(" + COL_PHONG_HOC + ", '') = ? AND " +
                "COALESCE(" + COL_TUAN_HOC + ", '') = ?";
        String[] selectionArgs = new String[]{
                String.valueOf(lichHoc.getMonHocId()),
                String.valueOf(lichHoc.getThu()),
                String.valueOf(lichHoc.getTietBatDau()),
                String.valueOf(lichHoc.getSoTiet()),
                lichHoc.getPhongHoc() != null ? lichHoc.getPhongHoc().trim() : "",
                lichHoc.getTuanHoc() != null ? lichHoc.getTuanHoc().trim() : ""
        };
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_LICH_HOC, new String[]{COL_LICH_HOC_ID}, selection, selectionArgs, null, null, null);
            return (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private long insertLichHocInternal(SQLiteDatabase db, LichHoc lichHoc) {
        if (isLichHocDuplicate(db, lichHoc)) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put(COL_LH_MON_HOC_ID, lichHoc.getMonHocId());
        values.put(COL_THU, lichHoc.getThu());
        values.put(COL_TIET_BAT_DAU, lichHoc.getTietBatDau());
        values.put(COL_SO_TIET, lichHoc.getSoTiet());
        values.put(COL_PHONG_HOC, lichHoc.getPhongHoc());
        values.put(COL_GIANG_VIEN, lichHoc.getGiangVien());
        values.put(COL_HOC_KY, lichHoc.getHocKy());
        values.put(COL_NAM_HOC, lichHoc.getNamHoc());
        values.put(COL_TUAN_HOC, lichHoc.getTuanHoc());
        return db.insert(TABLE_LICH_HOC, null, values);
    }

    private long insertLichThiInternal(SQLiteDatabase db, LichThi lichThi) {
        ContentValues values = new ContentValues();
        values.put(COL_LT_MON_HOC_ID, lichThi.getMonHocId());
        values.put(COL_NGAY_GIO_THI, lichThi.getNgayGioThi());
        values.put(COL_PHONG_THI, lichThi.getPhongThi());
        values.put(COL_HINH_THUC_THI, lichThi.getHinhThucThi());
        values.put(COL_SBD, lichThi.getSbd());
        values.put(COL_GHI_CHU_THI, lichThi.getGhiChu());
        return db.insert(TABLE_LICH_THI, null, values);
    }

    private long insertThongBaoInternal(SQLiteDatabase db, ThongBao thongBao) {
        ContentValues values = new ContentValues();
        values.put(COL_TIEU_DE, thongBao.getTieuDe());
        values.put(COL_NOI_DUNG, thongBao.getNoiDung());
        values.put(COL_NGAY_DANG, thongBao.getNgayDang());
        values.put(COL_LOAI_THONG_BAO, thongBao.getLoaiThongBao());
        values.put(COL_DA_DOC, thongBao.getDaDoc());
        values.put(COL_NGUON_TB, thongBao.getNguonThongBao());
        values.put(COL_PHONG_CU, thongBao.getPhongCu());
        values.put(COL_PHONG_MOI, thongBao.getPhongMoi());
        return db.insert(TABLE_THONG_BAO, null, values);
    }

    public void clearAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LICH_THI, null, null);
        db.delete(TABLE_LICH_HOC, null, null);
        db.delete(TABLE_MON_HOC, null, null);
        db.delete(TABLE_THONG_BAO, null, null);
    }

    // ==========================================
    // 1. QUẢN LÝ MÔN HỌC (CRUD)
    // ==========================================

    public long insertMonHoc(MonHoc monHoc) {
        SQLiteDatabase db = this.getWritableDatabase();
        return insertMonHocInternal(db, monHoc);
    }

    public int updateMonHoc(MonHoc monHoc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MA_MON, monHoc.getMaMon());
        values.put(COL_TEN_MON, monHoc.getTenMon());
        values.put(COL_SO_TIN_CHI, monHoc.getSoTinChi());
        values.put(COL_MAU_SAC, monHoc.getMauSac());
        values.put(COL_LOP_HOC, monHoc.getLopHoc());
        values.put(COL_MH_GIANG_VIEN, monHoc.getGiangVien());
        values.put(COL_TONG_SO_TUAN, monHoc.getTongSoTuan());
        values.put(COL_SO_TUAN_DA_HOC, monHoc.getSoTuanDaHoc());
        values.put(COL_SO_BUOI_VANG, monHoc.getSoBuoiVang());
        values.put(COL_DIEM_QUA_TRINH, monHoc.getDiemQuaTrinh());
        values.put(COL_TRANG_THAI, monHoc.getTrangThai());

        return db.update(TABLE_MON_HOC, values, COL_MON_HOC_ID + " = ?",
                new String[]{String.valueOf(monHoc.getId())});
    }

    public int deleteMonHoc(int monHocId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_MON_HOC, COL_MON_HOC_ID + " = ?",
                new String[]{String.valueOf(monHocId)});
    }

    public List<MonHoc> getAllMonHoc() {
        List<MonHoc> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MON_HOC + " ORDER BY " + COL_MON_HOC_ID + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                MonHoc item = new MonHoc(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_MON_HOC_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_MA_MON)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TEN_MON)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_SO_TIN_CHI)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_MAU_SAC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_LOP_HOC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_MH_GIANG_VIEN)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TONG_SO_TUAN)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_SO_TUAN_DA_HOC)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_SO_BUOI_VANG)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(COL_DIEM_QUA_TRINH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANG_THAI))
                );
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // ==========================================
    // 2. QUẢN LÝ LỊCH HỌC (CRUD)
    // ==========================================

    public long insertLichHoc(LichHoc lichHoc) {
        SQLiteDatabase db = this.getWritableDatabase();
        return insertLichHocInternal(db, lichHoc);
    }

    public int updateLichHoc(LichHoc lichHoc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LH_MON_HOC_ID, lichHoc.getMonHocId());
        values.put(COL_THU, lichHoc.getThu());
        values.put(COL_TIET_BAT_DAU, lichHoc.getTietBatDau());
        values.put(COL_SO_TIET, lichHoc.getSoTiet());
        values.put(COL_PHONG_HOC, lichHoc.getPhongHoc());
        values.put(COL_GIANG_VIEN, lichHoc.getGiangVien());
        values.put(COL_HOC_KY, lichHoc.getHocKy());
        values.put(COL_NAM_HOC, lichHoc.getNamHoc());
        values.put(COL_TUAN_HOC, lichHoc.getTuanHoc());

        return db.update(TABLE_LICH_HOC, values, COL_LICH_HOC_ID + " = ?",
                new String[]{String.valueOf(lichHoc.getId())});
    }

    public int deleteLichHoc(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_LICH_HOC, COL_LICH_HOC_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public List<LichHoc> getAllLichHocWithMonHoc() {
        List<LichHoc> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT lh.*, mh." + COL_MA_MON + ", mh." + COL_TEN_MON + ", mh." + COL_MAU_SAC +
                " FROM " + TABLE_LICH_HOC + " lh " +
                " INNER JOIN " + TABLE_MON_HOC + " mh ON lh." + COL_LH_MON_HOC_ID + " = mh." + COL_MON_HOC_ID +
                " ORDER BY lh." + COL_THU + " ASC, lh." + COL_TIET_BAT_DAU + " ASC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                LichHoc item = new LichHoc(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_LICH_HOC_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_LH_MON_HOC_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_THU)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TIET_BAT_DAU)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_SO_TIET)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONG_HOC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_GIANG_VIEN)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_HOC_KY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NAM_HOC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TUAN_HOC))
                );
                item.setMaMon(cursor.getString(cursor.getColumnIndexOrThrow(COL_MA_MON)));
                item.setTenMon(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEN_MON)));
                item.setMauSac(cursor.getString(cursor.getColumnIndexOrThrow(COL_MAU_SAC)));
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<LichHoc> getLichHocByThu(int thu) {
        List<LichHoc> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT lh.*, mh." + COL_MA_MON + ", mh." + COL_TEN_MON + ", mh." + COL_MAU_SAC +
                " FROM " + TABLE_LICH_HOC + " lh " +
                " INNER JOIN " + TABLE_MON_HOC + " mh ON lh." + COL_LH_MON_HOC_ID + " = mh." + COL_MON_HOC_ID +
                " WHERE lh." + COL_THU + " = ?" +
                " ORDER BY lh." + COL_TIET_BAT_DAU + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(thu)});
        if (cursor.moveToFirst()) {
            do {
                LichHoc item = new LichHoc(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_LICH_HOC_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_LH_MON_HOC_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_THU)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TIET_BAT_DAU)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_SO_TIET)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONG_HOC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_GIANG_VIEN)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_HOC_KY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NAM_HOC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TUAN_HOC))
                );
                item.setMaMon(cursor.getString(cursor.getColumnIndexOrThrow(COL_MA_MON)));
                item.setTenMon(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEN_MON)));
                item.setMauSac(cursor.getString(cursor.getColumnIndexOrThrow(COL_MAU_SAC)));
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public LichHoc findConflictingLichHoc(int thu, int tietBatDau, int soTiet, int excludeLichHocId) {
        int newStart = Math.max(1, tietBatDau);
        int newEnd = newStart + Math.max(1, soTiet) - 1;
        List<LichHoc> sameDayList = getLichHocByThu(thu);
        for (LichHoc existing : sameDayList) {
            if (existing.getId() == excludeLichHocId) continue;
            int exStart = existing.getTietBatDau();
            int exEnd = exStart + existing.getSoTiet() - 1;
            if (newStart <= exEnd && newEnd >= exStart) {
                return existing;
            }
        }
        return null;
    }

    // ==========================================
    // 3. QUẢN LÝ LỊCH THI (CRUD)
    // ==========================================

    public long insertLichThi(LichThi lichThi) {
        SQLiteDatabase db = this.getWritableDatabase();
        return insertLichThiInternal(db, lichThi);
    }

    public int updateLichThi(LichThi lichThi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LT_MON_HOC_ID, lichThi.getMonHocId());
        values.put(COL_NGAY_GIO_THI, lichThi.getNgayGioThi());
        values.put(COL_PHONG_THI, lichThi.getPhongThi());
        values.put(COL_HINH_THUC_THI, lichThi.getHinhThucThi());
        values.put(COL_SBD, lichThi.getSbd());
        values.put(COL_GHI_CHU_THI, lichThi.getGhiChu());

        return db.update(TABLE_LICH_THI, values, COL_LICH_THI_ID + " = ?",
                new String[]{String.valueOf(lichThi.getId())});
    }

    public int deleteLichThi(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_LICH_THI, COL_LICH_THI_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public List<LichThi> getAllLichThiWithMonHoc() {
        List<LichThi> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT lt.*, mh." + COL_MA_MON + ", mh." + COL_TEN_MON +
                " FROM " + TABLE_LICH_THI + " lt " +
                " INNER JOIN " + TABLE_MON_HOC + " mh ON lt." + COL_LT_MON_HOC_ID + " = mh." + COL_MON_HOC_ID +
                " ORDER BY lt." + COL_NGAY_GIO_THI + " ASC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                LichThi item = new LichThi(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_LICH_THI_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_LT_MON_HOC_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NGAY_GIO_THI)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONG_THI)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_HINH_THUC_THI)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_SBD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_GHI_CHU_THI))
                );
                item.setMaMon(cursor.getString(cursor.getColumnIndexOrThrow(COL_MA_MON)));
                item.setTenMon(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEN_MON)));
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // ==========================================
    // 4. QUẢN LÝ THÔNG BÁO (CRUD)
    // ==========================================

    public long insertThongBao(ThongBao thongBao) {
        SQLiteDatabase db = this.getWritableDatabase();
        return insertThongBaoInternal(db, thongBao);
    }

    public int updateThongBao(ThongBao thongBao) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TIEU_DE, thongBao.getTieuDe());
        values.put(COL_NOI_DUNG, thongBao.getNoiDung());
        values.put(COL_NGAY_DANG, thongBao.getNgayDang());
        values.put(COL_LOAI_THONG_BAO, thongBao.getLoaiThongBao());
        values.put(COL_DA_DOC, thongBao.getDaDoc());
        values.put(COL_NGUON_TB, thongBao.getNguonThongBao());
        values.put(COL_PHONG_CU, thongBao.getPhongCu());
        values.put(COL_PHONG_MOI, thongBao.getPhongMoi());

        return db.update(TABLE_THONG_BAO, values, COL_THONG_BAO_ID + " = ?",
                new String[]{String.valueOf(thongBao.getId())});
    }

    public int markThongBaoDaDoc(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DA_DOC, 1);

        return db.update(TABLE_THONG_BAO, values, COL_THONG_BAO_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public int markAllThongBaoDaDoc() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DA_DOC, 1);
        return db.update(TABLE_THONG_BAO, values, null, null);
    }

    public int deleteThongBao(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_THONG_BAO, COL_THONG_BAO_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public List<ThongBao> getAllThongBao() {
        List<ThongBao> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_THONG_BAO + " ORDER BY " + COL_THONG_BAO_ID + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                ThongBao item = new ThongBao(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_THONG_BAO_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TIEU_DE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NOI_DUNG)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NGAY_DANG)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_LOAI_THONG_BAO)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_DA_DOC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NGUON_TB)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONG_CU)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONG_MOI))
                );
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int getSoThongBaoChuaDoc() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_THONG_BAO + " WHERE " + COL_DA_DOC + " = 0", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // ==========================================
    // TÀI KHOẢN VÀ XÁC THỰC (AUTHENTICATION)
    // ==========================================

    public TaiKhoan authenticateUser(String identifier, String password) {
        if (identifier == null || password == null) return null;
        String idTrimmed = identifier.trim();
        String pwdTrimmed = password.trim();
        SQLiteDatabase db = this.getReadableDatabase();

        String emailCandidate = idTrimmed.contains("@") ? idTrimmed : idTrimmed + "@student.tdmu.edu.vn";
        Cursor cursor = db.query(TABLE_TAI_KHOAN, null,
                "(" + COL_TK_MSSV + " = ? COLLATE NOCASE OR " + COL_TK_EMAIL + " = ? COLLATE NOCASE OR " + COL_TK_EMAIL + " = ? COLLATE NOCASE) AND " + COL_TK_MAT_KHAU + " = ?",
                new String[]{idTrimmed, idTrimmed, emailCandidate, pwdTrimmed},
                null, null, null);

        TaiKhoan taiKhoan = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                taiKhoan = parseTaiKhoanFromCursor(cursor);
            }
            cursor.close();
        }
        return taiKhoan;
    }

    public boolean isUserExists(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return false;
        String idTrimmed = identifier.trim();
        String emailCandidate = idTrimmed.contains("@") ? idTrimmed : idTrimmed + "@student.tdmu.edu.vn";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_TAI_KHOAN + " WHERE " +
                        COL_TK_MSSV + " = ? COLLATE NOCASE OR " +
                        COL_TK_EMAIL + " = ? COLLATE NOCASE OR " +
                        COL_TK_EMAIL + " = ? COLLATE NOCASE",
                new String[]{idTrimmed, idTrimmed, emailCandidate});
        boolean exists = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                exists = cursor.getInt(0) > 0;
            }
            cursor.close();
        }
        return exists;
    }

    public TaiKhoan getTaiKhoanByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return null;
        String idTrimmed = identifier.trim();
        String emailCandidate = idTrimmed.contains("@") ? idTrimmed : idTrimmed + "@student.tdmu.edu.vn";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TAI_KHOAN, null,
                COL_TK_MSSV + " = ? COLLATE NOCASE OR " + COL_TK_EMAIL + " = ? COLLATE NOCASE OR " + COL_TK_EMAIL + " = ? COLLATE NOCASE",
                new String[]{idTrimmed, idTrimmed, emailCandidate},
                null, null, null);

        TaiKhoan taiKhoan = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                taiKhoan = parseTaiKhoanFromCursor(cursor);
            }
            cursor.close();
        }
        return taiKhoan;
    }

    public long insertTaiKhoan(TaiKhoan tk) {
        if (tk == null) return -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TK_MSSV, tk.getMssv() != null ? tk.getMssv().trim() : "");
        values.put(COL_TK_EMAIL, tk.getEmail() != null ? tk.getEmail().trim() : "");
        values.put(COL_TK_MAT_KHAU, tk.getMatKhau() != null ? tk.getMatKhau().trim() : "");
        values.put(COL_TK_HO_TEN, tk.getHoTen() != null ? tk.getHoTen().trim() : "");
        values.put(COL_TK_LOP_HOC, tk.getLopHoc() != null ? tk.getLopHoc().trim() : "");
        values.put(COL_TK_KHOA_VIEN, tk.getKhoaVien() != null ? tk.getKhoaVien().trim() : "Viện Kỹ thuật - Công nghệ");
        values.put(COL_TK_VAI_TRO, tk.getVaiTro() != null ? tk.getVaiTro().trim() : "student");
        values.put(COL_TK_NGAY_TAO, tk.getNgayTao() != null ? tk.getNgayTao() : "");
        return db.insert(TABLE_TAI_KHOAN, null, values);
    }

    public boolean updatePassword(String identifier, String newPassword) {
        if (identifier == null || newPassword == null) return false;
        String idTrimmed = identifier.trim();
        String emailCandidate = idTrimmed.contains("@") ? idTrimmed : idTrimmed + "@student.tdmu.edu.vn";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TK_MAT_KHAU, newPassword.trim());

        int rows = db.update(TABLE_TAI_KHOAN, values,
                COL_TK_MSSV + " = ? COLLATE NOCASE OR " + COL_TK_EMAIL + " = ? COLLATE NOCASE OR " + COL_TK_EMAIL + " = ? COLLATE NOCASE",
                new String[]{idTrimmed, idTrimmed, emailCandidate});
        return rows > 0;
    }

    public boolean updateTaiKhoanProfile(String identifier, String hoTen, String lopHoc, String khoaVien) {
        if (identifier == null) return false;
        String idTrimmed = identifier.trim();
        String emailCandidate = idTrimmed.contains("@") ? idTrimmed : idTrimmed + "@student.tdmu.edu.vn";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (hoTen != null) values.put(COL_TK_HO_TEN, hoTen.trim());
        if (lopHoc != null) values.put(COL_TK_LOP_HOC, lopHoc.trim());
        if (khoaVien != null) values.put(COL_TK_KHOA_VIEN, khoaVien.trim());

        int rows = db.update(TABLE_TAI_KHOAN, values,
                COL_TK_MSSV + " = ? COLLATE NOCASE OR " + COL_TK_EMAIL + " = ? COLLATE NOCASE OR " + COL_TK_EMAIL + " = ? COLLATE NOCASE",
                new String[]{idTrimmed, idTrimmed, emailCandidate});
        return rows > 0;
    }

    private TaiKhoan parseTaiKhoanFromCursor(Cursor cursor) {
        TaiKhoan tk = new TaiKhoan();
        tk.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TK_ID)));
        tk.setMssv(cursor.getString(cursor.getColumnIndexOrThrow(COL_TK_MSSV)));
        tk.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_TK_EMAIL)));
        tk.setMatKhau(cursor.getString(cursor.getColumnIndexOrThrow(COL_TK_MAT_KHAU)));
        tk.setHoTen(cursor.getString(cursor.getColumnIndexOrThrow(COL_TK_HO_TEN)));
        tk.setLopHoc(cursor.getString(cursor.getColumnIndexOrThrow(COL_TK_LOP_HOC)));
        tk.setKhoaVien(cursor.getString(cursor.getColumnIndexOrThrow(COL_TK_KHOA_VIEN)));
        tk.setVaiTro(cursor.getString(cursor.getColumnIndexOrThrow(COL_TK_VAI_TRO)));
        tk.setNgayTao(cursor.getString(cursor.getColumnIndexOrThrow(COL_TK_NGAY_TAO)));
        return tk;
    }
}
