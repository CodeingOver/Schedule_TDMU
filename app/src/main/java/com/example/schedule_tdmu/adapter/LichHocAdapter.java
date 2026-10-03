package com.example.schedule_tdmu.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.model.LichHoc;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LichHocAdapter extends RecyclerView.Adapter<LichHocAdapter.LichHocViewHolder> {

    private List<LichHoc> listLichHoc;
    private OnLichHocClickListener clickListener;
    private OnSelectionChangedListener selectionListener;
    private Date weekStartDate;
    private boolean isSelectionMode = false;
    private boolean showStatusTags = false;
    private final Set<Integer> selectedIds = new HashSet<>();

    public interface OnLichHocClickListener {
        void onItemClick(LichHoc lichHoc);
        void onItemLongClick(LichHoc lichHoc);
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    public LichHocAdapter(List<LichHoc> listLichHoc) {
        this.listLichHoc = listLichHoc;
    }

    public LichHocAdapter(List<LichHoc> listLichHoc, OnLichHocClickListener clickListener) {
        this.listLichHoc = listLichHoc;
        this.clickListener = clickListener;
    }

    public LichHocAdapter(List<LichHoc> listLichHoc, Date weekStartDate, OnLichHocClickListener clickListener) {
        this.listLichHoc = listLichHoc;
        this.weekStartDate = weekStartDate;
        this.clickListener = clickListener;
    }

    public void setShowStatusTags(boolean showStatusTags) {
        this.showStatusTags = showStatusTags;
        notifyDataSetChanged();
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    public void setWeekStartDate(Date weekStartDate) {
        this.weekStartDate = weekStartDate;
        notifyDataSetChanged();
    }

    public void updateData(List<LichHoc> newList) {
        this.listLichHoc = newList;
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
        if (!selectionMode) {
            selectedIds.clear();
        }
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedIds.size());
        }
    }

    public void selectAll() {
        selectedIds.clear();
        if (listLichHoc != null) {
            for (LichHoc item : listLichHoc) {
                selectedIds.add(item.getId());
            }
        }
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedIds.size());
        }
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedIds.size());
        }
    }

    public Set<Integer> getSelectedIds() {
        return selectedIds;
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    @NonNull
    @Override
    public LichHocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lich_hoc, parent, false);
        return new LichHocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LichHocViewHolder holder, int position) {
        LichHoc item = listLichHoc.get(position);

        holder.tvTenMon.setText(item.getTenMon() != null ? item.getTenMon() : "Môn học");
        holder.tvMaMon.setText(item.getMaMon() != null ? item.getMaMon() : "");

        if (holder.tvPhongBadge != null) {
            holder.tvPhongBadge.setText("Phòng " + (item.getPhongHoc() != null ? item.getPhongHoc() : "TDMU"));
        }

        if (holder.tvGiangVien != null) {
            holder.tvGiangVien.setText("GV: " + (item.getGiangVien() != null ? item.getGiangVien() : "N/A"));
        }

        int tietStart = item.getTietBatDau();
        int tietEnd = tietStart + item.getSoTiet() - 1;

        if (holder.tvTietHoc != null) {
            holder.tvTietHoc.setText(getTdmuPeriodTime(tietStart, tietEnd));
        }

        // TDMU Buổi & Ca Học Badge
        if (holder.tvBuoiCaBadge != null) {
            holder.tvBuoiCaBadge.setVisibility(View.VISIBLE);
            if (tietStart <= 3) {
                holder.tvBuoiCaBadge.setText("☀️ Sáng • Ca 1");
                holder.tvBuoiCaBadge.setBackgroundColor(Color.parseColor("#FFF3E0"));
                holder.tvBuoiCaBadge.setTextColor(Color.parseColor("#E65100"));
            } else if (tietStart <= 6) {
                holder.tvBuoiCaBadge.setText("☀️ Sáng • Ca 2");
                holder.tvBuoiCaBadge.setBackgroundColor(Color.parseColor("#FFF3E0"));
                holder.tvBuoiCaBadge.setTextColor(Color.parseColor("#E65100"));
            } else if (tietStart <= 9) {
                holder.tvBuoiCaBadge.setText("⛅ Chiều • Ca 3");
                holder.tvBuoiCaBadge.setBackgroundColor(Color.parseColor("#E3F2FD"));
                holder.tvBuoiCaBadge.setTextColor(Color.parseColor("#0D47A1"));
            } else if (tietStart <= 12) {
                holder.tvBuoiCaBadge.setText("⛅ Chiều • Ca 4");
                holder.tvBuoiCaBadge.setBackgroundColor(Color.parseColor("#E3F2FD"));
                holder.tvBuoiCaBadge.setTextColor(Color.parseColor("#0D47A1"));
            } else {
                holder.tvBuoiCaBadge.setText("🌙 Tối • Ca 5");
                holder.tvBuoiCaBadge.setBackgroundColor(Color.parseColor("#F3E5F5"));
                holder.tvBuoiCaBadge.setTextColor(Color.parseColor("#4A148C"));
            }
        }

        String thuText = item.getThu() == 8 ? "Chủ Nhật" : "Thứ " + item.getThu();
        if (weekStartDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(weekStartDate);
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                cal.add(Calendar.DAY_OF_YEAR, -1);
            }
            int dayOffset = item.getThu() == 8 ? 6 : Math.max(0, item.getThu() - 2);
            cal.add(Calendar.DAY_OF_YEAR, dayOffset);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            thuText += " (" + sdf.format(cal.getTime()) + ")";
        }
        if (holder.tvThu != null) {
            holder.tvThu.setText(thuText);
        }

        if (item.getMauSac() != null && !item.getMauSac().isEmpty()) {
            try {
                holder.viewColorStrip.setBackgroundColor(Color.parseColor(item.getMauSac()));
            } catch (Exception ignored) {
            }
        }

        // Tag Trạng Thái Thực Tế: Sắp diễn ra / Đang diễn ra / Đã qua
        if (showStatusTags && holder.tvStatusBadge != null) {
            holder.tvStatusBadge.setVisibility(View.VISIBLE);
            Calendar calNow = Calendar.getInstance();
            int nowMinutes = calNow.get(Calendar.HOUR_OF_DAY) * 60 + calNow.get(Calendar.MINUTE);

            int startMinutes = getPeriodStartMinutes(tietStart);
            int endMinutes = getPeriodEndMinutes(tietEnd);

            if (nowMinutes < startMinutes) {
                holder.tvStatusBadge.setText("Sắp diễn ra");
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#1976D2"));
            } else if (nowMinutes <= endMinutes) {
                holder.tvStatusBadge.setText("Đang diễn ra");
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#2E7D32"));
            } else {
                holder.tvStatusBadge.setText("Đã qua");
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#757575"));
            }
        } else if (holder.tvStatusBadge != null) {
            holder.tvStatusBadge.setVisibility(View.GONE);
        }

        // Chế độ Chọn Hàng Loạt
        if (isSelectionMode) {
            holder.cbSelect.setVisibility(View.VISIBLE);
            boolean isChecked = selectedIds.contains(item.getId());
            holder.cbSelect.setChecked(isChecked);

            if (isChecked) {
                holder.cardItemLich.setStrokeWidth(4);
            } else {
                holder.cardItemLich.setStrokeWidth(0);
            }
        } else {
            holder.cbSelect.setVisibility(View.GONE);
            holder.cardItemLich.setStrokeWidth(0);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(item.getId());
            } else if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                setSelectionMode(true);
                toggleSelection(item.getId());
            } else if (clickListener != null) {
                clickListener.onItemLongClick(item);
            }
            return true;
        });
    }

    private int getPeriodStartMinutes(int start) {
        if (start == 4) return 9 * 60 + 45;
        if (start == 7) return 13 * 60;
        if (start == 10) return 15 * 60 + 45;
        return 7 * 60;
    }

    private int getPeriodEndMinutes(int end) {
        if (end == 3) return 9 * 60 + 30;
        if (end == 5) return 11 * 60 + 25;
        if (end == 6) return 12 * 60 + 15;
        if (end == 9) return 15 * 60 + 30;
        if (end == 11) return 17 * 60 + 25;
        if (end == 12) return 18 * 60 + 15;
        return 11 * 60 + 25;
    }

    private void toggleSelection(int id) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id);
        } else {
            selectedIds.add(id);
        }
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedIds.size());
        }
    }

    private String getTdmuPeriodTime(int start, int end) {
        String startTime = "07:00";
        if (start == 4) startTime = "09:45";
        else if (start == 7) startTime = "13:00";
        else if (start == 10) startTime = "15:45";

        String endTime = "09:30";
        if (end == 3) endTime = "09:30";
        else if (end == 5) endTime = "11:25";
        else if (end == 6) endTime = "12:15";
        else if (end == 9) endTime = "15:30";
        else if (end == 11) endTime = "17:25";
        else if (end == 12) endTime = "18:15";

        return startTime + " - " + endTime;
    }

    @Override
    public int getItemCount() {
        return listLichHoc != null ? listLichHoc.size() : 0;
    }

    static class LichHocViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardItemLich;
        View viewColorStrip;
        CheckBox cbSelect;
        TextView tvTenMon, tvStatusBadge, tvBuoiCaBadge, tvMaMon, tvPhongBadge, tvGiangVien, tvTietHoc, tvThu;

        public LichHocViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItemLich = itemView.findViewById(R.id.card_item_lich);
            viewColorStrip = itemView.findViewById(R.id.view_color_strip);
            cbSelect = itemView.findViewById(R.id.cb_select);
            tvTenMon = itemView.findViewById(R.id.tv_ten_mon);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvBuoiCaBadge = itemView.findViewById(R.id.tv_buoi_ca_badge);
            tvMaMon = itemView.findViewById(R.id.tv_ma_mon);
            tvPhongBadge = itemView.findViewById(R.id.tv_phong_badge);
            tvGiangVien = itemView.findViewById(R.id.tv_giang_vien);
            tvTietHoc = itemView.findViewById(R.id.tv_tiet_hoc);
            tvThu = itemView.findViewById(R.id.tv_thu);
        }
    }
}
