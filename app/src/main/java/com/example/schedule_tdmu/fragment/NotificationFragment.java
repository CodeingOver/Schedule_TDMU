package com.example.schedule_tdmu.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_tdmu.DatabaseHelper;
import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.adapter.ThongBaoAdapter;
import com.example.schedule_tdmu.model.ThongBao;

import java.util.List;

public class NotificationFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView rvAllNotifications;
    private ThongBaoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        if (getContext() != null) {
            dbHelper = new DatabaseHelper(getContext());
        }

        rvAllNotifications = view.findViewById(R.id.rv_all_notifications);
        rvAllNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        if (dbHelper == null) return;

        List<ThongBao> list = dbHelper.getAllThongBao();
        adapter = new ThongBaoAdapter(list, thongBao -> {
            dbHelper.markThongBaoDaDoc(thongBao.getId());
            loadNotifications();
        });
        rvAllNotifications.setAdapter(adapter);
    }
}
