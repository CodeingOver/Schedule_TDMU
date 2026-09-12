package com.example.schedule_tdmu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.schedule_tdmu.model.LichHoc;
import com.example.schedule_tdmu.model.LichThi;
import com.example.schedule_tdmu.model.MonHoc;
import com.example.schedule_tdmu.model.ThongBao;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ScheduleTDMU.db";
    private static final int DATABASE_VERSION = 1;

    // Bảng MonHoc
    public static final String TABLE_MON_HOC = "MonHoc";
    public static final String COL_MON_HOC_ID = "id";
    public static final String COL_MA_MON = "ma_mon";
    public static final String COL_TEN_MON = "ten_mon";
    public static final String COL_SO_TIN_CHI = "so_tin_chi";
    public static final String COL_MAU_SAC = "mau_sac";

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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_MON_HOC = "CREATE TABLE " + TABLE_MON_HOC + " (" +
                COL_MON_HOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MA_MON + " TEXT NOT NULL UNIQUE, " +
                COL_TEN_MON + " TEXT NOT NULL, " +
                COL_SO_TIN_CHI + " INTEGER DEFAULT 0, " +
                COL_MAU_SAC + " TEXT DEFAULT '#1976D2');";

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
                COL_DA_DOC + " INTEGER DEFAULT 0);";

        db.execSQL(CREATE_TABLE_MON_HOC);
        db.execSQL(CREATE_TABLE_LICH_HOC);
        db.execSQL(CREATE_TABLE_LICH_THI);
        db.execSQL(CREATE_TABLE_THONG_BAO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LICH_THI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LICH_HOC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MON_HOC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_THONG_BAO);
        onCreate(db);
    }

    // ==========================================
    // 1. QUẢN LÝ MÔN HỌC (CRUD)
    // ==========================================

    public long insertMonHoc(MonHoc monHoc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MA_MON, monHoc.getMaMon());
        values.put(COL_TEN_MON, monHoc.getTenMon());
        values.put(COL_SO_TIN_CHI, monHoc.getSoTinChi());
        values.put(COL_MAU_SAC, monHoc.getMauSac());

        return db.insert(TABLE_MON_HOC, null, values);
    }

    public int updateMonHoc(MonHoc monHoc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MA_MON, monHoc.getMaMon());
        values.put(COL_TEN_MON, monHoc.getTenMon());
        values.put(COL_SO_TIN_CHI, monHoc.getSoTinChi());
        values.put(COL_MAU_SAC, monHoc.getMauSac());

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
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MON_HOC + " ORDER BY " + COL_TEN_MON + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                MonHoc item = new MonHoc(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_MON_HOC_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_MA_MON)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TEN_MON)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_SO_TIN_CHI)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_MAU_SAC))
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

    // ==========================================
    // 3. QUẢN LÝ LỊCH THI (CRUD)
    // ==========================================

    public long insertLichThi(LichThi lichThi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LT_MON_HOC_ID, lichThi.getMonHocId());
        values.put(COL_NGAY_GIO_THI, lichThi.getNgayGioThi());
        values.put(COL_PHONG_THI, lichThi.getPhongThi());
        values.put(COL_HINH_THUC_THI, lichThi.getHinhThucThi());
        values.put(COL_SBD, lichThi.getSbd());
        values.put(COL_GHI_CHU_THI, lichThi.getGhiChu());

        return db.insert(TABLE_LICH_THI, null, values);
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
        ContentValues values = new ContentValues();
        values.put(COL_TIEU_DE, thongBao.getTieuDe());
        values.put(COL_NOI_DUNG, thongBao.getNoiDung());
        values.put(COL_NGAY_DANG, thongBao.getNgayDang());
        values.put(COL_LOAI_THONG_BAO, thongBao.getLoaiThongBao());
        values.put(COL_DA_DOC, thongBao.getDaDoc());

        return db.insert(TABLE_THONG_BAO, null, values);
    }

    public int markThongBaoDaDoc(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DA_DOC, 1);

        return db.update(TABLE_THONG_BAO, values, COL_THONG_BAO_ID + " = ?",
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
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_DA_DOC))
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
    // 5. DỮ LIỆU MẪU BAN ĐẦU (DÙNG ĐỂ TEST)
    // ==========================================

    public void insertSampleDataIfEmpty() {
        if (getAllMonHoc().isEmpty()) {
            long id1 = insertMonHoc(new MonHoc("LTTBDD", "Lập trình thiết bị di động", 3, "#1E88E5"));
            long id2 = insertMonHoc(new MonHoc("CSDL", "Cơ sở dữ liệu", 3, "#43A047"));
            long id3 = insertMonHoc(new MonHoc("MMT", "Mạng máy tính", 2, "#FB8C00"));

            // Thứ 2 (2): Tiết 1-3, Môn Lập trình di động, Phòng I.201
            insertLichHoc(new LichHoc((int) id1, 2, 1, 3, "I.201", "ThS. Nguyễn Văn A", 1, "2024-2025", "1-15"));
            // Thứ 4 (4): Tiết 7-9, Môn Cơ sở dữ liệu, Phòng B.102
            insertLichHoc(new LichHoc((int) id2, 4, 7, 3, "B.102", "TS. Trần Thị B", 1, "2024-2025", "1-15"));

            // Lịch thi mẫu
            insertLichThi(new LichThi((int) id1, "2024-12-20 08:00", "I.201", "Tự luận", "SBD123", "Mang theo thẻ sinh viên"));

            // Thông báo mẫu
            insertThongBao(new ThongBao("Thông báo nghỉ học", "Lớp Lập trình thiết bị di động nghỉ học ngày 15/10.", "2024-10-12", "Học tập"));
            insertThongBao(new ThongBao("Đóng học phí HK1", "Hạn chót đóng học phí học kỳ 1 là ngày 30/10/2024.", "2024-10-01", "Chung"));
        }
    }
}
