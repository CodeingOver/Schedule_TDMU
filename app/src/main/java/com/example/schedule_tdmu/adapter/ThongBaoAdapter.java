package com.example.schedule_tdmu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.model.ThongBao;

import java.util.List;

public class ThongBaoAdapter extends RecyclerView.Adapter<ThongBaoAdapter.ThongBaoViewHolder> {

    private List<ThongBao> listThongBao;
    private OnThongBaoClickListener clickListener;

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

        holder.tvTieuDe.setText(item.getTieuDe());
        holder.tvNoiDung.setText(item.getNoiDung());
        holder.tvLoaiThongBao.setText(item.getLoaiThongBao() != null ? item.getLoaiThongBao() : "Chung");
        holder.tvNgayDang.setText(item.getNgayDang() != null ? item.getNgayDang() : "");

        if (item.isDaDoc()) {
            holder.tvBadgeDaDoc.setVisibility(View.GONE);
        } else {
            holder.tvBadgeDaDoc.setVisibility(View.VISIBLE);
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
        TextView tvTieuDe, tvNoiDung, tvLoaiThongBao, tvNgayDang, tvBadgeDaDoc;

        public ThongBaoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTieuDe = itemView.findViewById(R.id.tv_tieu_de);
            tvNoiDung = itemView.findViewById(R.id.tv_noi_dung);
            tvLoaiThongBao = itemView.findViewById(R.id.tv_loai_thong_bao);
            tvNgayDang = itemView.findViewById(R.id.tv_ngay_dang);
            tvBadgeDaDoc = itemView.findViewById(R.id.tv_badge_da_doc);
        }
    }
}
