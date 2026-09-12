package com.example.schedule_tdmu.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.model.LichHoc;

import java.util.List;

public class LichHocAdapter extends RecyclerView.Adapter<LichHocAdapter.LichHocViewHolder> {

    private List<LichHoc> listLichHoc;

    public LichHocAdapter(List<LichHoc> listLichHoc) {
        this.listLichHoc = listLichHoc;
    }

    public void updateData(List<LichHoc> newList) {
        this.listLichHoc = newList;
        notifyDataSetChanged();
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
        
        String phongGv = "Phòng: " + (item.getPhongHoc() != null ? item.getPhongHoc() : "---") +
                " | GV: " + (item.getGiangVien() != null ? item.getGiangVien() : "N/A");
        holder.tvPhongGiangVien.setText(phongGv);

        int tietKetThuc = item.getTietBatDau() + item.getSoTiet() - 1;
        String tietHoc = "Tiết " + item.getTietBatDau() + " - " + tietKetThuc + " (" + item.getSoTiet() + " tiết)";
        holder.tvTietHoc.setText(tietHoc);

        String thuText = item.getThu() == 8 ? "Chủ Nhật" : "Thứ " + item.getThu();
        holder.tvThu.setText(thuText);

        if (item.getMauSac() != null && !item.getMauSac().isEmpty()) {
            try {
                holder.viewColorStrip.setBackgroundColor(Color.parseColor(item.getMauSac()));
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public int getItemCount() {
        return listLichHoc != null ? listLichHoc.size() : 0;
    }

    static class LichHocViewHolder extends RecyclerView.ViewHolder {
        View viewColorStrip;
        TextView tvTenMon, tvMaMon, tvPhongGiangVien, tvTietHoc, tvThu;

        public LichHocViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColorStrip = itemView.findViewById(R.id.view_color_strip);
            tvTenMon = itemView.findViewById(R.id.tv_ten_mon);
            tvMaMon = itemView.findViewById(R.id.tv_ma_mon);
            tvPhongGiangVien = itemView.findViewById(R.id.tv_phong_giang_vien);
            tvTietHoc = itemView.findViewById(R.id.tv_tiet_hoc);
            tvThu = itemView.findViewById(R.id.tv_thu);
        }
    }
}
