package com.example.schedule_tdmu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.model.ThongBao;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ThongBaoAdapter extends RecyclerView.Adapter<ThongBaoAdapter.ThongBaoViewHolder> {

    private List<ThongBao> listThongBao;
    private final OnThongBaoClickListener clickListener;

    public interface OnThongBaoClickListener {
        void onItemClick(ThongBao thongBao);
    }

    public ThongBaoAdapter(List<ThongBao> listThongBao, OnThongBaoClickListener clickListener) {
        this.listThongBao = listThongBao;
        this.clickListener = clickListener;
    }

    public void updateData(List<ThongBao> newList) {
        this.listThongBao = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ThongBaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thong_bao, parent, false);
        return new ThongBaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThongBaoViewHolder holder, int position) {
        ThongBao item = listThongBao.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTieuDe.setText(item.getTieuDe());
        holder.tvNoiDung.setText(item.getNoiDung());
        holder.tvNguonThongBao.setText(item.getNguonThongBao());
        holder.tvNgayDang.setText(item.getNgayDang() != null ? item.getNgayDang() : "");

        String loai = item.getLoaiThongBao() != null ? item.getLoaiThongBao().toUpperCase() : "GENERAL";

        if (loai.contains("URGENT") || loai.contains("KHẨN") || loai.contains("ĐỔI")) {
            holder.tvLoaiThongBao.setText("KHẨN CẤP");
            holder.tvLoaiThongBao.setBackgroundResource(R.drawable.bg_pill_error);
            holder.tvLoaiThongBao.setTextColor(ContextCompat.getColor(context, R.color.on_error_container));
            holder.layoutCategoryIcon.setBackgroundResource(R.drawable.bg_pill_error);
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_warning);
            holder.ivCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.on_error_container));
        } else if (loai.contains("EXAM") || loai.contains("THI")) {
            holder.tvLoaiThongBao.setText("LỊCH THI");
            holder.tvLoaiThongBao.setBackgroundResource(R.drawable.bg_badge_primary_fixed);
            holder.tvLoaiThongBao.setTextColor(ContextCompat.getColor(context, R.color.on_primary_fixed));
            holder.layoutCategoryIcon.setBackgroundResource(R.drawable.bg_badge_primary_fixed);
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_assignment_late);
            holder.ivCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.on_primary_fixed));
        } else if (loai.contains("SCHEDULE") || loai.contains("HỌC") || loai.contains("LỊCH")) {
            holder.tvLoaiThongBao.setText("LỊCH HỌC");
            holder.tvLoaiThongBao.setBackgroundResource(R.drawable.bg_pill_secondary);
            holder.tvLoaiThongBao.setTextColor(ContextCompat.getColor(context, R.color.on_secondary_fixed));
            holder.layoutCategoryIcon.setBackgroundResource(R.drawable.bg_pill_secondary);
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_event_busy);
            holder.ivCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.on_secondary_fixed));
        } else {
            holder.tvLoaiThongBao.setText("KHOA & TRƯỜNG");
            holder.tvLoaiThongBao.setBackgroundResource(R.drawable.bg_pill_tertiary);
            holder.tvLoaiThongBao.setTextColor(ContextCompat.getColor(context, R.color.on_tertiary_fixed));
            holder.layoutCategoryIcon.setBackgroundResource(R.drawable.bg_pill_tertiary);
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_campaign);
            holder.ivCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.on_tertiary_fixed));
        }

        if (!item.getPhongCu().isEmpty() || !item.getPhongMoi().isEmpty()) {
            holder.layoutRoomChange.setVisibility(View.VISIBLE);
            holder.tvPhongCu.setText(item.getPhongCu().isEmpty() ? "Phòng cũ" : item.getPhongCu());
            holder.tvPhongMoi.setText("Phòng mới: " + item.getPhongMoi());
        } else {
            holder.layoutRoomChange.setVisibility(View.GONE);
        }

        if (item.isDaDoc()) {
            holder.tvBadgeDaDoc.setVisibility(View.GONE);
            holder.cardThongBao.setStrokeColor(ContextCompat.getColor(context, R.color.surface_container_high));
        } else {
            holder.tvBadgeDaDoc.setVisibility(View.VISIBLE);
            holder.cardThongBao.setStrokeColor(ContextCompat.getColor(context, R.color.primary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listThongBao != null ? listThongBao.size() : 0;
    }

    static class ThongBaoViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardThongBao;
        FrameLayout layoutCategoryIcon;
        ImageView ivCategoryIcon;
        TextView tvTieuDe, tvNoiDung, tvLoaiThongBao, tvNguonThongBao, tvNgayDang, tvBadgeDaDoc;
        LinearLayout layoutRoomChange;
        TextView tvPhongCu, tvPhongMoi;

        public ThongBaoViewHolder(@NonNull View itemView) {
            super(itemView);
            cardThongBao = itemView.findViewById(R.id.card_thong_bao);
            layoutCategoryIcon = itemView.findViewById(R.id.layoutCategoryIcon);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvTieuDe = itemView.findViewById(R.id.tv_tieu_de);
            tvNoiDung = itemView.findViewById(R.id.tv_noi_dung);
            tvLoaiThongBao = itemView.findViewById(R.id.tv_loai_thong_bao);
            tvNguonThongBao = itemView.findViewById(R.id.tv_nguon_thong_bao);
            tvNgayDang = itemView.findViewById(R.id.tv_ngay_dang);
            tvBadgeDaDoc = itemView.findViewById(R.id.tv_badge_da_doc);
            layoutRoomChange = itemView.findViewById(R.id.layoutRoomChange);
            tvPhongCu = itemView.findViewById(R.id.tvPhongCu);
            tvPhongMoi = itemView.findViewById(R.id.tvPhongMoi);
        }
    }
}
