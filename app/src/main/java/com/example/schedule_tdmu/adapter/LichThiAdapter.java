package com.example.schedule_tdmu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.model.LichThi;

import java.util.List;

public class LichThiAdapter extends RecyclerView.Adapter<LichThiAdapter.LichThiViewHolder> {

    private List<LichThi> listLichThi;
    private OnLichThiClickListener clickListener;

    public interface OnLichThiClickListener {
        void onItemClick(LichThi lichThi);
        void onItemLongClick(LichThi lichThi);
    }

    public LichThiAdapter(List<LichThi> listLichThi) {
        this.listLichThi = listLichThi;
    }

    public LichThiAdapter(List<LichThi> listLichThi, OnLichThiClickListener clickListener) {
        this.listLichThi = listLichThi;
        this.clickListener = clickListener;
    }

    public void updateData(List<LichThi> newList) {
        this.listLichThi = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LichThiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lich_thi, parent, false);
        return new LichThiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LichThiViewHolder holder, int position) {
        LichThi item = listLichThi.get(position);

        holder.tvTenMonThi.setText(item.getTenMon() != null ? item.getTenMon() : "Môn thi");
        holder.tvHinhThucThi.setText(item.getHinhThucThi() != null ? item.getHinhThucThi() : "Thi");
        holder.tvNgayGioThi.setText(item.getNgayGioThi() != null ? item.getNgayGioThi() : "---");

        String phongSbd = "Phòng: " + (item.getPhongThi() != null ? item.getPhongThi() : "---") +
                " | SBD: " + (item.getSbd() != null ? item.getSbd() : "---");
        holder.tvPhongSbd.setText(phongSbd);

        if (item.getGhiChu() != null && !item.getGhiChu().isEmpty()) {
            holder.tvGhiChu.setVisibility(View.VISIBLE);
            holder.tvGhiChu.setText("Ghi chú: " + item.getGhiChu());
        } else {
            holder.tvGhiChu.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemLongClick(item);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listLichThi != null ? listLichThi.size() : 0;
    }

    static class LichThiViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenMonThi, tvHinhThucThi, tvNgayGioThi, tvPhongSbd, tvGhiChu;

        public LichThiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenMonThi = itemView.findViewById(R.id.tv_ten_mon_thi);
            tvHinhThucThi = itemView.findViewById(R.id.tv_hinh_thuc_thi);
            tvNgayGioThi = itemView.findViewById(R.id.tv_ngay_gio_thi);
            tvPhongSbd = itemView.findViewById(R.id.tv_phong_sbd);
            tvGhiChu = itemView.findViewById(R.id.tv_ghi_chu);
        }
    }
}
