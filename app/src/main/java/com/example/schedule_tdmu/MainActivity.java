package com.example.schedule_tdmu;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.schedule_tdmu.fragment.NotificationFragment;
import com.example.schedule_tdmu.fragment.ProfileFragment;
import com.example.schedule_tdmu.fragment.ScanFragment;
import com.example.schedule_tdmu.fragment.ScheduleFragment;
import com.example.schedule_tdmu.fragment.SubjectsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private BottomNavigationView bottomNav;
    private TextView tvHeaderSubtitle, tvWeekBadge;
    private View viewHeaderUnreadDot;
    private String currentDateBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        currentDateBadge = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        tvHeaderSubtitle = findViewById(R.id.tv_header_subtitle);
        tvWeekBadge = findViewById(R.id.tv_week_badge);
        viewHeaderUnreadDot = findViewById(R.id.view_header_unread_dot);
        FrameLayout btnHeaderBell = findViewById(R.id.btn_header_bell);
        MaterialCardView btnHeaderProfile = findViewById(R.id.btn_header_profile);
        bottomNav = findViewById(R.id.bottom_navigation);

        if (bottomNav != null) {
            // Ngăn Material3 BottomNavigationView tự cộng thêm bottom system bar inset bên trong Card
            ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
                v.setPadding(0, 0, 0, 0);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        if (btnHeaderBell != null) {
            btnHeaderBell.setOnClickListener(v -> navigateToTab(R.id.nav_notifications));
        }

        if (btnHeaderProfile != null) {
            btnHeaderProfile.setOnClickListener(v -> navigateToTab(R.id.nav_profile));
        }

        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_schedule) {
                    selectedFragment = new ScheduleFragment();
                    updateHeaderTitle("Thời Khóa Biểu", currentDateBadge);
                } else if (itemId == R.id.nav_notifications) {
                    selectedFragment = new NotificationFragment();
                    updateHeaderTitle("Thông Báo Học Tập", currentDateBadge);
                } else if (itemId == R.id.nav_subjects) {
                    selectedFragment = new SubjectsFragment();
                    updateHeaderTitle("Môn Học & Tín Chỉ", currentDateBadge);
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                    updateHeaderTitle("Hồ Sơ Sinh Viên", currentDateBadge);
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    refreshHeaderBadge();
                    return true;
                }
                return false;
            });
        }

        if (savedInstanceState == null) {
            loadFragment(new ScheduleFragment());
            updateHeaderTitle("Thời Khóa Biểu", currentDateBadge);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_schedule);
            }
        }

        refreshHeaderBadge();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHeaderBadge();
    }

    public void navigateToTab(int itemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }

    public void openScanFragment() {
        updateHeaderTitle("Quét Ảnh TKB (AI OCR)", "ML Kit");
        loadFragment(new ScanFragment());
    }

    public void refreshHeaderBadge() {
        if (dbHelper != null && viewHeaderUnreadDot != null) {
            int unread = dbHelper.getSoThongBaoChuaDoc();
            viewHeaderUnreadDot.setVisibility(unread > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void updateHeaderTitle(String subtitle, String badgeText) {
        if (tvHeaderSubtitle != null) {
            tvHeaderSubtitle.setText(subtitle);
        }
        if (tvWeekBadge != null) {
            tvWeekBadge.setText(badgeText);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
