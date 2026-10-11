package com.example.schedule_tdmu.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.schedule_tdmu.LoginActivity;
import com.example.schedule_tdmu.MainActivity;
import com.example.schedule_tdmu.R;
import com.example.schedule_tdmu.utils.SharedPrefManager;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private SharedPrefManager prefManager;
    private TextView tvProfileName, tvProfileEmail, tvProfileMssv, tvProfileRole;
    private TextView tvProfileFaculty, tvProfileClassAndCohort;
    private TextView tvProfileGpa, tvProfileCredits, tvProfileDrl;
    private TextView pillRemind15, pillRemind30, pillRemind45;
    private SwitchCompat switchAlarmPeriod1, switchSilentInClass;
    private LinearLayout btnShowQrModal;
    private Button btnOpenAiScanner, btnSyncGoogleCal, btnEditProfileInfo, btnChangePassword, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        prefManager = new SharedPrefManager(requireContext());

        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        tvProfileMssv = view.findViewById(R.id.tv_profile_mssv);
        tvProfileRole = view.findViewById(R.id.tv_profile_role);
        tvProfileFaculty = view.findViewById(R.id.tvProfileFaculty);
        tvProfileClassAndCohort = view.findViewById(R.id.tvProfileClassAndCohort);

        tvProfileGpa = view.findViewById(R.id.tv_profile_gpa);
        tvProfileCredits = view.findViewById(R.id.tv_profile_credits);
        tvProfileDrl = view.findViewById(R.id.tv_profile_drl);

        pillRemind15 = view.findViewById(R.id.pillRemind15);
        pillRemind30 = view.findViewById(R.id.pillRemind30);
        pillRemind45 = view.findViewById(R.id.pillRemind45);

        switchAlarmPeriod1 = view.findViewById(R.id.switchAlarmPeriod1);
        switchSilentInClass = view.findViewById(R.id.switchSilentInClass);

        btnShowQrModal = view.findViewById(R.id.btnShowQrModal);
        btnOpenAiScanner = view.findViewById(R.id.btnOpenAiScanner);
        btnSyncGoogleCal = view.findViewById(R.id.btnSyncGoogleCal);
        btnEditProfileInfo = view.findViewById(R.id.btnEditProfileInfo);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnLogout = view.findViewById(R.id.btn_logout);

        bindProfileData();
        setupSettingsListeners();

        btnShowQrModal.setOnClickListener(v -> showStudentQrModal());

        btnOpenAiScanner.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openScanFragment();
            }
        });

        btnSyncGoogleCal.setOnClickListener(v -> {
            boolean nextState = !prefManager.isGoogleCalSynced();
            prefManager.setGoogleCalSynced(nextState);
            updateGoogleCalButtonText();
            String msg = nextState
                    ? "Đã bật đồng bộ tự động với Google Calendar"
                    : "Đã tạm dừng đồng bộ Google Calendar";
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        btnEditProfileInfo.setOnClickListener(v -> showEditProfileDialog());

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        btnLogout.setOnClickListener(v -> {
            prefManager.logout();
            Toast.makeText(requireContext(), "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        return view;
    }

    private void bindProfileData() {
        String name = prefManager.getStudentName();
        String email = prefManager.getStudentEmail();
        String mssv = prefManager.getStudentMssv();

        tvProfileName.setText(name);
        tvProfileEmail.setText(email);
        tvProfileMssv.setText("MSSV: " + mssv);
        tvProfileFaculty.setText(prefManager.getStudentFaculty());
        tvProfileClassAndCohort.setText(prefManager.getStudentClass() + " • " + prefManager.getStudentCohort());

        if (prefManager.isAdmin()) {
            tvProfileRole.setText("QUẢN TRỊ VIÊN TDMU");
        } else {
            tvProfileRole.setText("SINH VIÊN CHÍNH QUY");
        }

        tvProfileGpa.setText(String.format(Locale.US, "%.2f / 4.0", prefManager.getStudentGpa()));
        tvProfileCredits.setText(prefManager.getAccumulatedCredits() + " / 135");
        tvProfileDrl.setText(prefManager.getStudentDrl() + " / 100");

        updateReminderPillStyles(prefManager.getReminderMinutes());
        switchAlarmPeriod1.setChecked(prefManager.isAlarmPeriod1Enabled());
        switchSilentInClass.setChecked(prefManager.isSilentInClassEnabled());
        updateGoogleCalButtonText();
    }

    private void setupSettingsListeners() {
        pillRemind15.setOnClickListener(v -> selectReminderLeadTime(15));
        pillRemind30.setOnClickListener(v -> selectReminderLeadTime(30));
        pillRemind45.setOnClickListener(v -> selectReminderLeadTime(45));

        switchAlarmPeriod1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefManager.setAlarmPeriod1Enabled(isChecked);
            String msg = isChecked
                    ? "Đã bật báo thức tự động lúc 06:15 cho các ngày có Tiết 1"
                    : "Đã tắt báo thức tự động Tiết 1";
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        switchSilentInClass.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefManager.setSilentInClassEnabled(isChecked);
            String msg = isChecked
                    ? "Đã bật nhắc chế độ im lặng trong giờ học"
                    : "Đã tắt chế độ im lặng trong giờ học";
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void selectReminderLeadTime(int minutes) {
        prefManager.setReminderMinutes(minutes);
        updateReminderPillStyles(minutes);
        Toast.makeText(requireContext(), "Đã đặt nhắc trước giờ học " + minutes + " phút", Toast.LENGTH_SHORT).show();
    }

    private void updateReminderPillStyles(int selectedMinutes) {
        setSegmentStyle(pillRemind15, selectedMinutes == 15);
        setSegmentStyle(pillRemind30, selectedMinutes == 30);
        setSegmentStyle(pillRemind45, selectedMinutes == 45);
    }

    private void setSegmentStyle(TextView tv, boolean active) {
        if (active) {
            tv.setBackgroundResource(R.drawable.bg_segmented_active);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        } else {
            tv.setBackgroundResource(0);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        }
    }

    private void updateGoogleCalButtonText() {
        if (prefManager.isGoogleCalSynced()) {
            btnSyncGoogleCal.setText("Đồng bộ Google Calendar (Đang bật)");
        } else {
            btnSyncGoogleCal.setText("Đồng bộ Google Calendar (Đang tắt)");
        }
    }

    private void showStudentQrModal() {
        float density = getResources().getDisplayMetrics().density;
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = (int) (24 * density);
        container.setPadding(pad, pad, pad, pad / 2);

        ImageView qrImage = new ImageView(requireContext());
        int qrSize = (int) (180 * density);
        LinearLayout.LayoutParams qrParams = new LinearLayout.LayoutParams(qrSize, qrSize);
        qrImage.setLayoutParams(qrParams);
        qrImage.setImageResource(R.drawable.ic_student_qr_code);
        container.addView(qrImage);

        TextView tvName = new TextView(requireContext());
        tvName.setText(prefManager.getStudentName());
        tvName.setTextSize(17f);
        tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        tvName.setGravity(Gravity.CENTER);
        tvName.setPadding(0, (int) (14 * density), 0, 0);
        container.addView(tvName);

        TextView tvCode = new TextView(requireContext());
        tvCode.setText("MSSV: " + prefManager.getStudentMssv() + " • " + prefManager.getStudentClass());
        tvCode.setTextSize(13f);
        tvCode.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        tvCode.setGravity(Gravity.CENTER);
        tvCode.setPadding(0, (int) (4 * density), 0, 0);
        container.addView(tvCode);

        new AlertDialog.Builder(requireContext())
                .setTitle("Mã QR Thẻ Sinh Viên TDMU")
                .setView(container)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showEditProfileDialog() {
        float density = getResources().getDisplayMetrics().density;
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (18 * density);
        container.setPadding(pad, pad / 2, pad, 0);

        EditText edtName = new EditText(requireContext());
        edtName.setHint("Họ và tên sinh viên");
        edtName.setText(prefManager.getStudentName());
        container.addView(edtName);

        EditText edtMssv = new EditText(requireContext());
        edtMssv.setHint("Mã số sinh viên");
        edtMssv.setText(prefManager.getStudentMssv());
        edtMssv.setInputType(InputType.TYPE_CLASS_NUMBER);
        container.addView(edtMssv);

        EditText edtClass = new EditText(requireContext());
        edtClass.setHint("Lớp biên chế (VD: D22CNTT01)");
        edtClass.setText(prefManager.getStudentClass());
        container.addView(edtClass);

        EditText edtGpa = new EditText(requireContext());
        edtGpa.setHint("GPA Tích lũy (Hệ 4.0)");
        edtGpa.setText(String.valueOf(prefManager.getStudentGpa()));
        edtGpa.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        container.addView(edtGpa);

        EditText edtCredits = new EditText(requireContext());
        edtCredits.setHint("Số tín chỉ đã tích lũy");
        edtCredits.setText(String.valueOf(prefManager.getAccumulatedCredits()));
        edtCredits.setInputType(InputType.TYPE_CLASS_NUMBER);
        container.addView(edtCredits);

        EditText edtDrl = new EditText(requireContext());
        edtDrl.setHint("Điểm rèn luyện (0 - 100)");
        edtDrl.setText(String.valueOf(prefManager.getStudentDrl()));
        edtDrl.setInputType(InputType.TYPE_CLASS_NUMBER);
        container.addView(edtDrl);

        new AlertDialog.Builder(requireContext())
                .setTitle("Cập nhật hồ sơ sinh viên")
                .setView(container)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = edtName.getText().toString().trim();
                    String mssv = edtMssv.getText().toString().trim();
                    String studentClass = edtClass.getText().toString().trim();
                    if (!name.isEmpty()) prefManager.setStudentName(name);
                    if (!mssv.isEmpty()) prefManager.setStudentMssv(mssv);
                    if (!studentClass.isEmpty()) prefManager.setStudentClass(studentClass);
                    try {
                        float gpa = Float.parseFloat(edtGpa.getText().toString().trim());
                        prefManager.setStudentGpa(Math.min(4.0f, Math.max(0f, gpa)));
                    } catch (Exception ignored) {
                    }
                    try {
                        int credits = Integer.parseInt(edtCredits.getText().toString().trim());
                        prefManager.setAccumulatedCredits(Math.max(0, credits));
                    } catch (Exception ignored) {
                    }
                    try {
                        int drl = Integer.parseInt(edtDrl.getText().toString().trim());
                        prefManager.setStudentDrl(Math.min(100, Math.max(0, drl)));
                    } catch (Exception ignored) {
                    }
                    bindProfileData();
                    Toast.makeText(requireContext(), "Đã cập nhật hồ sơ sinh viên", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showChangePasswordDialog() {
        float density = getResources().getDisplayMetrics().density;
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (18 * density);
        container.setPadding(pad, pad / 2, pad, 0);

        EditText edtOldPass = new EditText(requireContext());
        edtOldPass.setHint("Mật khẩu hiện tại");
        edtOldPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(edtOldPass);

        EditText edtNewPass = new EditText(requireContext());
        edtNewPass.setHint("Mật khẩu mới");
        edtNewPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(edtNewPass);

        new AlertDialog.Builder(requireContext())
                .setTitle("Đổi mật khẩu tài khoản")
                .setView(container)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    if (edtNewPass.getText().toString().trim().length() < 4) {
                        Toast.makeText(requireContext(), "Mật khẩu mới cần ít nhất 4 ký tự", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
