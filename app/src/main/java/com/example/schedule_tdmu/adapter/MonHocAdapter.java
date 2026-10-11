package com.example.schedule_tdmu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.model.MonHoc;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MonHocAdapter extends RecyclerView.Adapter<MonHocAdapter.MonHocViewHolder> {

    public interface OnMonHocActionListener {
        void onEditMonHoc(MonHoc monHoc);
        void onDeleteMonHoc(MonHoc monHoc);
    }

    private final Context context;
    private List<MonHoc> monHocList;
    private Map<Integer, String> scheduleSummaryMap;
    private final OnMonHocActionListener listener;

    public MonHocAdapter(Context context, List<MonHoc> monHocList,
                         Map<Integer, String> scheduleSummaryMap,
                         OnMonHocActionListener listener) {
        this.context = context;
        this.monHocList = monHocList;
        this.scheduleSummaryMap = scheduleSummaryMap;
        this.listener = listener;
    }

    public void updateData(List<MonHoc> newList, Map<Integer, String> newScheduleMap) {
        this.monHocList = newList;
        this.scheduleSummaryMap = newScheduleMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MonHocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mon_hoc, parent, false);
        return new MonHocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonHocViewHolder holder, int position) {
        MonHoc monHoc = monHocList.get(position);

        holder.tvMaMon.setText(monHoc.getMaMon() + " • " + monHoc.getLopHoc());
        holder.tvSoTinChi.setText(monHoc.getSoTinChi() + " TC");
        holder.tvTenMon.setText(monHoc.getTenMon());
        holder.tvGiangVien.setText(monHoc.getGiangVien());

        String scheduleInfo = (scheduleSummaryMap != null && scheduleSummaryMap.containsKey(monHoc.getId()))
                ? scheduleSummaryMap.get(monHoc.getId())
                : "Chưa xếp phòng • Học kỳ 1";
        holder.tvPhongVaLich.setText(scheduleInfo);

        int tiLeChuyenCan = monHoc.getTiLeChuyenCan();
        if (monHoc.getSoBuoiVang() == 0) {
            holder.layoutChuyenCanBadge.setBackgroundResource(R.drawable.bg_pill_tertiary);
            holder.ivChuyenCanIcon.setImageResource(R.drawable.ic_check_circle);
            holder.ivChuyenCanIcon.setColorFilter(ContextCompat.getColor(context, R.color.on_tertiary_fixed));
            holder.tvChuyenCan.setText("Chuyên cần: 100%");
            holder.tvChuyenCan.setTextColor(ContextCompat.getColor(context, R.color.on_tertiary_fixed));
        } else {
            holder.layoutChuyenCanBadge.setBackgroundResource(R.drawable.bg_pill_error);
            holder.ivChuyenCanIcon.setImageResource(R.drawable.ic_warning);
            holder.ivChuyenCanIcon.setColorFilter(ContextCompat.getColor(context, R.color.on_error_container));
            holder.tvChuyenCan.setText("Vắng " + monHoc.getSoBuoiVang() + " buổi (" + tiLeChuyenCan + "%)");
            holder.tvChuyenCan.setTextColor(ContextCompat.getColor(context, R.color.on_error_container));
        }

        int soTuanDaHoc = monHoc.getSoTuanDaHoc();
        int tongSoTuan = monHoc.getTongSoTuan();
        int phanTram = monHoc.getTienDoPhanTram();
        holder.tvTienDoTuan.setText("Tuần " + soTuanDaHoc + "/" + tongSoTuan + " (" + phanTram + "%)");
        holder.progressTuan.setMax(tongSoTuan);
        holder.progressTuan.setProgress(soTuanDaHoc);

        if (monHoc.hasDiemQuaTrinh()) {
            holder.tvDiemQuaTrinh.setText(String.format(Locale.US, "Điểm QT: %.1f", monHoc.getDiemQuaTrinh()));
        } else {
            holder.tvDiemQuaTrinh.setText("Điểm QT: Chưa cập nhật");
        }

        holder.cardMonHoc.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditMonHoc(monHoc);
            }
        });

        holder.btnCapNhatMon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditMonHoc(monHoc);
            }
        });

        holder.btnXoaMon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteMonHoc(monHoc);
            }
        });
    }

    @Override
    public int getItemCount() {
        return monHocList != null ? monHocList.size() : 0;
    }

    static class MonHocViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardMonHoc;
        TextView tvMaMon, tvSoTinChi, tvChuyenCan, tvTenMon, tvGiangVien, tvPhongVaLich;
        TextView tvTienDoTuan, tvDiemQuaTrinh;
        LinearLayout layoutChuyenCanBadge, btnCapNhatMon;
        ImageView ivChuyenCanIcon, btnXoaMon;
        ProgressBar progressTuan;

        public MonHocViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMonHoc = itemView.findViewById(R.id.cardMonHoc);
            tvMaMon = itemView.findViewById(R.id.tvMaMon);
            tvSoTinChi = itemView.findViewById(R.id.tvSoTinChi);
            tvChuyenCan = itemView.findViewById(R.id.tvChuyenCan);
            tvTenMon = itemView.findViewById(R.id.tvTenMon);
            tvGiangVien = itemView.findViewById(R.id.tvGiangVien);
            tvPhongVaLich = itemView.findViewById(R.id.tvPhongVaLich);
            tvTienDoTuan = itemView.findViewById(R.id.tvTienDoTuan);
            tvDiemQuaTrinh = itemView.findViewById(R.id.tvDiemQuaTrinh);
            layoutChuyenCanBadge = itemView.findViewById(R.id.layoutChuyenCanBadge);
            btnCapNhatMon = itemView.findViewById(R.id.btnCapNhatMon);
            ivChuyenCanIcon = itemView.findViewById(R.id.ivChuyenCanIcon);
            btnXoaMon = itemView.findViewById(R.id.btnXoaMon);
            progressTuan = itemView.findViewById(R.id.progressTuan);
        }
    }
}
