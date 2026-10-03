package com.example.schedule_tdmu.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.R;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WeekStripAdapter extends RecyclerView.Adapter<WeekStripAdapter.WeekDayViewHolder> {

    public static class DayItem {
        public int thuVal; // 2: T2, 3: T3, ... 8: CN, -1: Tất cả
        public String dayName;
        public String dateNum;
        public Date fullDate;
        public boolean hasClass;

        public DayItem(int thuVal, String dayName, String dateNum, Date fullDate, boolean hasClass) {
            this.thuVal = thuVal;
            this.dayName = dayName;
            this.dateNum = dateNum;
            this.fullDate = fullDate;
            this.hasClass = hasClass;
        }
    }

    public interface OnDaySelectedListener {
        void onDaySelected(DayItem dayItem);
    }

    private List<DayItem> dayList;
    private int selectedPosition = 0; // Mặc định T2 hoặc ngày chọn
    private OnDaySelectedListener listener;

    public WeekStripAdapter(List<DayItem> dayList, OnDaySelectedListener listener) {
        this.dayList = dayList;
        this.listener = listener;
    }

    public void updateDays(List<DayItem> newList, int defaultSelectedThu) {
        this.dayList = newList;
        this.selectedPosition = 0;
        if (newList != null) {
            for (int i = 0; i < newList.size(); i++) {
                if (newList.get(i).thuVal == defaultSelectedThu) {
                    selectedPosition = i;
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    public DayItem getSelectedDay() {
        if (dayList != null && selectedPosition >= 0 && selectedPosition < dayList.size()) {
            return dayList.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public WeekDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_week_day_chip, parent, false);
        return new WeekDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekDayViewHolder holder, int position) {
        DayItem item = dayList.get(position);
        boolean isSelected = (position == selectedPosition);

        holder.tvDayName.setText(item.dayName);
        holder.tvDateNum.setText(item.dateNum);

        if (item.hasClass) {
            holder.viewDot.setVisibility(View.VISIBLE);
            holder.viewDot.setBackgroundColor(isSelected ? Color.WHITE : Color.parseColor("#1976D2"));
        } else {
            holder.viewDot.setVisibility(View.INVISIBLE);
        }

        if (isSelected) {
            holder.cardDayChip.setCardBackgroundColor(Color.parseColor("#1976D2"));
            holder.tvDayName.setTextColor(Color.WHITE);
            holder.tvDateNum.setTextColor(Color.WHITE);
        } else {
            holder.cardDayChip.setCardBackgroundColor(Color.WHITE);
            holder.tvDayName.setTextColor(Color.parseColor("#757575"));
            holder.tvDateNum.setTextColor(Color.parseColor("#212121"));
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);

            if (listener != null && selectedPosition >= 0 && selectedPosition < dayList.size()) {
                listener.onDaySelected(dayList.get(selectedPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dayList != null ? dayList.size() : 0;
    }

    static class WeekDayViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardDayChip;
        TextView tvDayName, tvDateNum;
        View viewDot;

        public WeekDayViewHolder(@NonNull View itemView) {
            super(itemView);
            cardDayChip = itemView.findViewById(R.id.card_day_chip);
            tvDayName = itemView.findViewById(R.id.tv_chip_day_name);
            tvDateNum = itemView.findViewById(R.id.tv_chip_date_num);
            viewDot = itemView.findViewById(R.id.view_has_class_dot);
        }
    }
}
