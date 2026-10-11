package com.example.schedule_tdmu.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.DatabaseHelper;
import com.example.schedule_tdmu.MainActivity;
import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.adapter.ThongBaoAdapter;
import com.example.schedule_tdmu.model.ThongBao;
import com.example.schedule_tdmu.utils.NotificationHelper;
import com.example.schedule_tdmu.utils.SharedPrefManager;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationFragment extends Fragment {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_URGENT = 1;
    private static final int FILTER_EXAM = 2;
    private static final int FILTER_CAMPUS = 3;

    private DatabaseHelper dbHelper;
    private SharedPrefManager prefManager;
    private RecyclerView rvAllNotifications;
    private ThongBaoAdapter adapter;

    private TextView tvUnreadSummary;
    private LinearLayout btnMarkAllRead;
    private Button btnAddNotification;
    private TextView pillNotifAll, pillNotifUrgent, pillNotifExam, pillNotifCampus;
    private MaterialCardView cardUrgentBanner;
    private TextView tvUrgentTitle, tvUrgentOldRoom, tvUrgentNewRoom, btnUrgentViewSchedule;
    private TextView btnSyncNow;

    private int currentFilter = FILTER_ALL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        prefManager = new SharedPrefManager(requireContext());

        tvUnreadSummary = view.findViewById(R.id.tvUnreadSummary);
        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);
        btnAddNotification = view.findViewById(R.id.btn_add_notification);

        pillNotifAll = view.findViewById(R.id.pillNotifAll);
        pillNotifUrgent = view.findViewById(R.id.pillNotifUrgent);
        pillNotifExam = view.findViewById(R.id.pillNotifExam);
        pillNotifCampus = view.findViewById(R.id.pillNotifCampus);

        cardUrgentBanner = view.findViewById(R.id.cardUrgentBanner);
        tvUrgentTitle = view.findViewById(R.id.tvUrgentTitle);
        tvUrgentOldRoom = view.findViewById(R.id.tvUrgentOldRoom);
        tvUrgentNewRoom = view.findViewById(R.id.tvUrgentNewRoom);
        btnUrgentViewSchedule = view.findViewById(R.id.btnUrgentViewSchedule);
        btnSyncNow = view.findViewById(R.id.btnSyncNow);

        rvAllNotifications = view.findViewById(R.id.rv_all_notifications);
        rvAllNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ThongBaoAdapter(new ArrayList<>(), this::showNotificationDetailDialog);
        rvAllNotifications.setAdapter(adapter);

        pillNotifAll.setOnClickListener(v -> applyFilter(FILTER_ALL));
        pillNotifUrgent.setOnClickListener(v -> applyFilter(FILTER_URGENT));
        pillNotifExam.setOnClickListener(v -> applyFilter(FILTER_EXAM));
        pillNotifCampus.setOnClickListener(v -> applyFilter(FILTER_CAMPUS));

        btnMarkAllRead.setOnClickListener(v -> {
            dbHelper.markAllThongBaoDaDoc();
            loadNotifications();
            notifyHeaderBadgeChanged();
            Toast.makeText(requireContext(), "Đã đánh dấu đọc tất cả thông báo", Toast.LENGTH_SHORT).show();
        });

        btnAddNotification.setOnClickListener(v -> showAddNotificationDialog());

        btnUrgentViewSchedule.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_schedule);
            }
        });

        btnSyncNow.setOnClickListener(v -> {
            loadNotifications();
            Toast.makeText(requireContext(), "Đã đồng bộ thông báo & phòng học mới nhất với TKB", Toast.LENGTH_SHORT).show();
        });

        loadNotifications();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void applyFilter(int filter) {
        currentFilter = filter;
        setPillStyle(pillNotifAll, currentFilter == FILTER_ALL);
        setPillStyle(pillNotifUrgent, currentFilter == FILTER_URGENT);
        setPillStyle(pillNotifExam, currentFilter == FILTER_EXAM);
        setPillStyle(pillNotifCampus, currentFilter == FILTER_CAMPUS);
        loadNotifications();
    }

    private void setPillStyle(TextView pill, boolean active) {
        if (active) {
            pill.setBackgroundResource(R.drawable.bg_pill_primary);
            pill.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            pill.setBackgroundResource(R.drawable.bg_pill_surface_high);
            pill.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        }
    }

    private void showNotificationDetailDialog(ThongBao thongBao) {
        if (!thongBao.isDaDoc()) {
            dbHelper.markThongBaoDaDoc(thongBao.getId());
            loadNotifications();
            notifyHeaderBadgeChanged();
        }

        StringBuilder msg = new StringBuilder();
        msg.append("Nguồn: ").append(thongBao.getNguonThongBao()).append("\n");
        msg.append("Thời gian: ").append(thongBao.getNgayDang()).append("\n\n");
        msg.append(thongBao.getNoiDung());
        if (!thongBao.getPhongCu().isEmpty() || !thongBao.getPhongMoi().isEmpty()) {
            msg.append("\n\nThay đổi phòng: ")
                    .append(thongBao.getPhongCu())
                    .append(" -> ")
                    .append(thongBao.getPhongMoi());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(thongBao.getTieuDe())
                .setMessage(msg.toString())
                .setPositiveButton("Đóng", null)
                .setNeutralButton("Xóa thông báo", (d, w) -> {
                    dbHelper.deleteThongBao(thongBao.getId());
                    loadNotifications();
                    notifyHeaderBadgeChanged();
                    Toast.makeText(requireContext(), "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showAddNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_notification, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.et_notification_title);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_notification_type);
        EditText etContent = dialogView.findViewById(R.id.et_notification_content);
        TextView tvSelectedReminderTime = dialogView.findViewById(R.id.tvSelectedReminderTime);
        Button btnPickReminderDateTime = dialogView.findViewById(R.id.btnPickReminderDateTime);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_notif);
        Button btnSend = dialogView.findViewById(R.id.btn_send_notif);

        String[] types = {"Khẩn cấp & Đổi lịch", "Lịch thi", "Lịch học", "Khoa & Trường"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(typeAdapter);

        final long[] scheduledMillis = {0L};
        final Calendar selectedCal = Calendar.getInstance();

        btnPickReminderDateTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                selectedCal.set(Calendar.YEAR, year);
                selectedCal.set(Calendar.MONTH, month);
                selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                new TimePickerDialog(requireContext(), (timeView, hourOfDay, minute) -> {
                    selectedCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedCal.set(Calendar.MINUTE, minute);
                    selectedCal.set(Calendar.SECOND, 0);
                    scheduledMillis[0] = selectedCal.getTimeInMillis();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    tvSelectedReminderTime.setText("Hẹn lúc: " + sdf.format(selectedCal.getTime()));
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
        });

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            int selectedPos = spinnerType.getSelectedItemPosition();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            String loaiCode;
            String nguon;
            if (selectedPos == 0) {
                loaiCode = "URGENT";
                nguon = "Phòng Đào tạo TDMU";
            } else if (selectedPos == 1) {
                loaiCode = "EXAM";
                nguon = "Phòng Khảo thí TDMU";
            } else if (selectedPos == 2) {
                loaiCode = "SCHEDULE";
                nguon = "Nhắc nhở học tập";
            } else {
                loaiCode = "GENERAL";
                nguon = prefManager != null && prefManager.isAdmin() ? "Ban Quản trị TDMU" : "Viện Kỹ thuật - Công nghệ";
            }

            String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            ThongBao newNotif = new ThongBao(title, content, currentDate, loaiCode, nguon, "", "");
            dbHelper.insertThongBao(newNotif);

            long triggerAt = scheduledMillis[0] > System.currentTimeMillis()
                    ? scheduledMillis[0]
                    : (System.currentTimeMillis() + 1500);

            NotificationHelper.scheduleNotification(
                    requireContext(),
                    title,
                    content,
                    triggerAt,
                    (int) (System.currentTimeMillis() & 0x7fffffff)
            );

            Toast.makeText(requireContext(), "Đã lưu thông báo và đặt lịch nhắc nhở", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadNotifications();
            notifyHeaderBadgeChanged();
        });

        dialog.show();
    }

    private void loadNotifications() {
        if (dbHelper == null) return;

        List<ThongBao> allList = dbHelper.getAllThongBao();
        int unreadCount = dbHelper.getSoThongBaoChuaDoc();

        if (unreadCount > 0) {
            tvUnreadSummary.setText("Bạn có " + unreadCount + " thông báo chưa đọc");
        } else {
            tvUnreadSummary.setText("Tất cả thông báo đã được đọc");
        }

        // Tìm thông báo khẩn cấp đổi phòng để hiển thị trên Pinned Urgent Banner
        ThongBao urgentRoomItem = null;
        for (ThongBao tb : allList) {
            if (!tb.getPhongMoi().isEmpty() || "URGENT".equalsIgnoreCase(tb.getLoaiThongBao())) {
                urgentRoomItem = tb;
                break;
            }
        }

        if (urgentRoomItem != null && (currentFilter == FILTER_ALL || currentFilter == FILTER_URGENT)) {
            cardUrgentBanner.setVisibility(View.VISIBLE);
            tvUrgentTitle.setText(urgentRoomItem.getTieuDe());
            tvUrgentOldRoom.setText(urgentRoomItem.getPhongCu().isEmpty() ? "Phòng cũ" : urgentRoomItem.getPhongCu());
            tvUrgentNewRoom.setText("Phòng mới: " + (urgentRoomItem.getPhongMoi().isEmpty() ? "E1-302" : urgentRoomItem.getPhongMoi()));
        } else {
            cardUrgentBanner.setVisibility(View.GONE);
        }

        List<ThongBao> filtered = new ArrayList<>();
        for (ThongBao tb : allList) {
            String loai = tb.getLoaiThongBao().toUpperCase();
            if (currentFilter == FILTER_ALL) {
                filtered.add(tb);
            } else if (currentFilter == FILTER_URGENT) {
                if (loai.contains("URGENT") || loai.contains("SCHEDULE") || loai.contains("KHẨN") || loai.contains("HỌC")) {
                    filtered.add(tb);
                }
            } else if (currentFilter == FILTER_EXAM) {
                if (loai.contains("EXAM") || loai.contains("THI")) {
                    filtered.add(tb);
                }
            } else if (currentFilter == FILTER_CAMPUS) {
                if (loai.contains("GENERAL") || loai.contains("CHUNG") || loai.contains("KHOA")) {
                    filtered.add(tb);
                }
            }
        }

        adapter.updateData(filtered);
        View emptyView = getView() != null ? getView().findViewById(R.id.layoutEmptyNotifications) : null;
        if (emptyView != null) {
            emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
        rvAllNotifications.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void notifyHeaderBadgeChanged() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshHeaderBadge();
        }
    }
}
