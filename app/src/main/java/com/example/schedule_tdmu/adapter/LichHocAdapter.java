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
    private boolean showStatusTags = true;
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
            holder.tvPhongBadge.setText(item.getPhongHoc() != null && !item.getPhongHoc().isEmpty() ? item.getPhongHoc() : "Phòng TDMU");
        }

        if (holder.tvGiangVien != null) {
            String gv = item.getGiangVien() != null && !item.getGiangVien().isEmpty() ? item.getGiangVien() : "Giảng viên TDMU";
            holder.tvGiangVien.setText(gv + " • Khoa CNTT");
        }

        int tietStart = item.getTietBatDau();
        int tietEnd = tietStart + item.getSoTiet() - 1;

        if (holder.tvTietHoc != null) {
            holder.tvTietHoc.setText(getTdmuPeriodTime(tietStart, tietEnd));
        }

        if (holder.tvBuoiCaBadge != null) {
            String caText;
            if (tietStart <= 3) {
                caText = "Tiết " + tietStart + " - " + tietEnd + " • Ca 1 Sáng";
            } else if (tietStart <= 6) {
                caText = "Tiết " + tietStart + " - " + tietEnd + " • Ca 2 Sáng";
            } else if (tietStart <= 9) {
                caText = "Tiết " + tietStart + " - " + tietEnd + " • Ca 3 Chiều";
            } else if (tietStart <= 12) {
                caText = "Tiết " + tietStart + " - " + tietEnd + " • Ca 4 Chiều";
            } else {
                caText = "Tiết " + tietStart + " - " + tietEnd + " • Ca Tối";
            }
            holder.tvBuoiCaBadge.setText(caText);
        }

        String thuText = item.getThu() == 8 ? "Chủ Nhật" : "Thứ " + item.getThu();
        Date classDate = null;
        if (weekStartDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(weekStartDate);
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                cal.add(Calendar.DAY_OF_YEAR, -1);
            }
            int dayOffset = item.getThu() == 8 ? 6 : Math.max(0, item.getThu() - 2);
            cal.add(Calendar.DAY_OF_YEAR, dayOffset);
            classDate = cal.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            thuText += " (" + sdf.format(classDate) + ")";
        }
        if (holder.tvThu != null) {
            holder.tvThu.setVisibility(View.VISIBLE);
            holder.tvThu.setText(thuText);
        }

        if (item.getMauSac() != null && !item.getMauSac().isEmpty()) {
            try {
                holder.viewColorStrip.setBackgroundColor(Color.parseColor(item.getMauSac()));
            } catch (Exception ignored) {
            }
        }

        if (holder.tvStatusBadge != null) {
            if (!showStatusTags) {
                holder.tvStatusBadge.setVisibility(View.GONE);
            } else {
                holder.tvStatusBadge.setVisibility(View.VISIBLE);
                Calendar calNow = Calendar.getInstance();
                long todayStart = LichHoc.startOfDay(calNow.getTime());
                long classDayStart = (classDate != null) ? LichHoc.startOfDay(classDate) : todayStart;

                int nowMinutes = calNow.get(Calendar.HOUR_OF_DAY) * 60 + calNow.get(Calendar.MINUTE);
                int startMinutes = getPeriodStartMinutes(tietStart);
                int endMinutes = getPeriodEndMinutes(tietEnd);

                if (classDayStart < todayStart) {
                    // Buổi học đã diễn ra ở ngày trong quá khứ -> Đã qua
                    holder.tvStatusBadge.setText("Đã hoàn thành");
                    holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_surface_high);
                    holder.tvStatusBadge.setTextColor(Color.parseColor("#464555"));
                } else if (classDayStart > todayStart) {
                    // Buổi học diễn ra ở ngày trong tương lai (ngày mai, tuần sau,...) -> Không thể là "Đang diễn ra"
                    if (tietStart >= 13) {
                        holder.tvStatusBadge.setText("Buổi tối");
                        holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_surface_high);
                        holder.tvStatusBadge.setTextColor(Color.parseColor("#464555"));
                    } else if (tietStart >= 7) {
                        holder.tvStatusBadge.setText("Buổi chiều");
                        holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_secondary);
                        holder.tvStatusBadge.setTextColor(Color.parseColor("#0058BE"));
                    } else {
                        holder.tvStatusBadge.setText("Buổi sáng");
                        holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_surface_high);
                        holder.tvStatusBadge.setTextColor(Color.parseColor("#3525CD"));
                    }
                } else {
                    // Buổi học diễn ra CHÍNH XÁC HÔM NAY (classDayStart == todayStart)
                    if (nowMinutes > endMinutes) {
                        // Tiết học hôm nay đã kết thúc
                        holder.tvStatusBadge.setText("Đã hoàn thành");
                        holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_surface_high);
                        holder.tvStatusBadge.setTextColor(Color.parseColor("#464555"));
                    } else if (nowMinutes >= startMinutes && nowMinutes <= endMinutes) {
                        // Tiết học đang diễn ra ngay lúc này!
                        holder.tvStatusBadge.setText("Đang diễn ra");
                        holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_tertiary);
                        holder.tvStatusBadge.setTextColor(Color.parseColor("#005338"));
                    } else {
                        // nowMinutes < startMinutes: Chưa tới giờ học hôm nay
                        int diff = startMinutes - nowMinutes;
                        if (diff <= 60) {
                            holder.tvStatusBadge.setText("Sắp diễn ra (" + diff + "p)");
                            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_error);
                            holder.tvStatusBadge.setTextColor(Color.parseColor("#BA1A1A"));
                        } else if (tietStart >= 13) {
                            holder.tvStatusBadge.setText("Buổi tối");
                            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_surface_high);
                            holder.tvStatusBadge.setTextColor(Color.parseColor("#464555"));
                        } else if (tietStart >= 7) {
                            holder.tvStatusBadge.setText("Buổi chiều");
                            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_secondary);
                            holder.tvStatusBadge.setTextColor(Color.parseColor("#0058BE"));
                        } else {
                            holder.tvStatusBadge.setText("Sắp diễn ra");
                            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_secondary);
                            holder.tvStatusBadge.setTextColor(Color.parseColor("#0058BE"));
                        }
                    }
                }
            }
        }

        if (isSelectionMode) {
            holder.cbSelect.setVisibility(View.VISIBLE);
            boolean isChecked = selectedIds.contains(item.getId());
            holder.cbSelect.setChecked(isChecked);
            holder.cardItemLich.setStrokeWidth(isChecked ? 4 : 2);
            holder.cardItemLich.setStrokeColor(Color.parseColor(isChecked ? "#3525CD" : "#E2E7FF"));
        } else {
            holder.cbSelect.setVisibility(View.GONE);
            holder.cardItemLich.setStrokeWidth(2);
            holder.cardItemLich.setStrokeColor(Color.parseColor("#E2E7FF"));
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

    public static int getPeriodStartMinutes(int start) {
        if (start == 4) return 9 * 60 + 30;
        if (start == 7) return 13 * 60;
        if (start == 10) return 15 * 60 + 30;
        if (start >= 13) return 18 * 60;
        return 7 * 60;
    }

    public static int getPeriodEndMinutes(int end) {
        if (end <= 3) return 9 * 60 + 15;
        if (end <= 6) return 11 * 60 + 45;
        if (end <= 9) return 15 * 60 + 30;
        if (end <= 12) return 17 * 60 + 45;
        return 20 * 60 + 15;
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

    public static String getTdmuPeriodTime(int start, int end) {
        String startTime = "07:00";
        if (start == 4) startTime = "09:30";
        else if (start == 7) startTime = "13:00";
        else if (start == 10) startTime = "15:30";
        else if (start >= 13) startTime = "18:00";

        String endTime = "09:15";
        if (end <= 3) endTime = "09:15";
        else if (end <= 6) endTime = "11:45";
        else if (end <= 9) endTime = "15:30";
        else if (end <= 12) endTime = "17:45";
        else endTime = "20:15";

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
