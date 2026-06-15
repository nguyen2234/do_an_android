package com.example.android_app.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.MainActivity;
import com.example.android_app.R;
import com.example.android_app.adapter.PaymentReminderAdapter;
import com.example.android_app.database.DanhMucDAO;
import com.example.android_app.database.ReminderDAO;
import com.example.android_app.model.DanhMuc;
import com.example.android_app.model.Recurrence;
import com.example.android_app.model.Reminder;
import com.example.android_app.model.ReminderStatus;
import com.example.android_app.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Fragment quản lý danh sách nhắc hẹn thanh toán.
 */
public class PaymentReminderFragment extends Fragment {

    private ReminderDAO dao;
    private DanhMucDAO categoryDAO;
    private PaymentReminderAdapter adapter;
    private List<Reminder> dataList;
    private RecyclerView rv;
    private View layoutEmpty;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_reminder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dao = new ReminderDAO(getContext());
        categoryDAO = new DanhMucDAO(getContext());
        dao.open();
        categoryDAO.open();

        rv = view.findViewById(R.id.rvReminder);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        loadData();

        // Nút quay lại
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        // FAB thêm mới nhắc hẹn
        View fabThemReminder = view.findViewById(R.id.fabThemReminder);
        if (fabThemReminder != null) {
            fabThemReminder.setOnClickListener(v -> showReminderDialog(null, -1));
        }
    }

    private void loadData() {
        dataList = dao.getAllReminders();
        adapter = new PaymentReminderAdapter(getContext(), dataList);

        adapter.setOnItemActionListener(new PaymentReminderAdapter.OnItemActionListener() {
            @Override
            public void onDelete(Reminder item, int position) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa nhắc hẹn")
                        .setMessage("Bạn muốn xóa nhắc hẹn \"" + item.getTitle() + "\"?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            dao.deleteReminder(item.getId());
                            dataList.remove(position);
                            adapter.notifyItemRemoved(position);
                            updateEmptyState();
                            Toast.makeText(getContext(), "❌ Đã xóa nhắc hẹn!", Toast.LENGTH_SHORT).show();
                            
                            NotificationHelper.showReminderNotification(
                                    getContext(),
                                    (int) item.getId(),
                                    "Xóa nhắc hẹn thành công",
                                    "Đã xóa nhắc hẹn \"" + item.getTitle() + "\""
                            );
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onEdit(Reminder item, int position) {
                showReminderDialog(item, position);
            }

            @Override
            public void onPay(Reminder item, int position) {
                // Luồng thanh toán: Chuyển sang ThanhToanReminderActivity để xác nhận
                android.content.Intent intent = new android.content.Intent(getContext(), com.example.android_app.ThanhToanReminderActivity.class);
                intent.putExtra("prefilled_amount", item.getEstimatedAmount());
                intent.putExtra("prefilled_category", item.getCategory());
                intent.putExtra("prefilled_title", item.getTitle());
                intent.putExtra("reminder_id", item.getId());
                startActivity(intent);
            }
        });

        rv.setAdapter(adapter);
        updateEmptyState();
    }

    private void showReminderDialog(@Nullable Reminder item, int position) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reminder, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etTitle = dialogView.findViewById(R.id.etDialogTitle);
        EditText etAmount = dialogView.findViewById(R.id.etDialogAmount);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerDialogCategory);
        Spinner spinnerRecurrence = dialogView.findViewById(R.id.spinnerDialogRecurrence);
        EditText etDueDate = dialogView.findViewById(R.id.etDialogDueDate);
        EditText etOffsetDays = dialogView.findViewById(R.id.etDialogOffsetDays);

        // Thiết lập Danh mục Spinner
        List<DanhMuc> categories = categoryDAO.getAllCategories();
        List<String> categoryNames = new ArrayList<>();
        for (DanhMuc c : categories) {
            categoryNames.add(c.getName());
        }
        if (categoryNames.isEmpty()) {
            categoryNames.add("Ăn uống");
            categoryNames.add("Di chuyển");
            categoryNames.add("Giải trí");
        }
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_spinner, categoryNames);
        catAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerCategory.setAdapter(catAdapter);

        // Thiết lập Tần suất lặp Spinner
        String[] recOptions = {"Hàng tháng", "Hàng quý", "Hàng năm"};
        ArrayAdapter<String> recAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_spinner, recOptions);
        recAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerRecurrence.setAdapter(recAdapter);

        // Xử lý Date & Time picker cho Due Date
        Calendar cal = Calendar.getInstance();
        final boolean[] isDateSelected = {false};
        
        etDueDate.setOnClickListener(v -> {
            DatePickerDialog dp = new DatePickerDialog(requireContext(),
                    (picker, y, m, d) -> {
                        cal.set(Calendar.YEAR, y);
                        cal.set(Calendar.MONTH, m);
                        cal.set(Calendar.DAY_OF_MONTH, d);

                        new TimePickerDialog(requireContext(),
                                (timePicker, h, min) -> {
                                    cal.set(Calendar.HOUR_OF_DAY, h);
                                    cal.set(Calendar.MINUTE, min);
                                    cal.set(Calendar.SECOND, 0);
                                    cal.set(Calendar.MILLISECOND, 0);
                                    isDateSelected[0] = true;

                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi"));
                                    etDueDate.setText(sdf.format(cal.getTime()));
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                true
                        ).show();
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
            dp.show();
        });

        // Điền dữ liệu nếu là sửa (Edit Mode)
        if (item != null) {
            tvTitle.setText("Sửa nhắc hẹn thanh toán");
            etTitle.setText(item.getTitle());
            etAmount.setText(String.valueOf((int) item.getEstimatedAmount()));
            
            int catIdx = categoryNames.indexOf(item.getCategory());
            if (catIdx != -1) spinnerCategory.setSelection(catIdx);

            if (item.getRecurrence() == Recurrence.QUARTERLY) {
                spinnerRecurrence.setSelection(1);
            } else if (item.getRecurrence() == Recurrence.YEARLY) {
                spinnerRecurrence.setSelection(2);
            } else {
                spinnerRecurrence.setSelection(0);
            }

            if (item.getDueDate() != null) {
                etDueDate.setText(item.getDueDate().format(dtf));
                cal.set(item.getDueDate().getYear(),
                        item.getDueDate().getMonthValue() - 1,
                        item.getDueDate().getDayOfMonth(),
                        item.getDueDate().getHour(),
                        item.getDueDate().getMinute());
                isDateSelected[0] = true;
            }
            etOffsetDays.setText(String.valueOf(item.getReminderOffsetDays()));
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton(item == null ? "Thêm" : "Cập nhật", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String offsetStr = etOffsetDays.getText().toString().trim();

            if (title.isEmpty() || amountStr.isEmpty() || !isDateSelected[0]) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            int offset = 0;
            if (!offsetStr.isEmpty()) {
                try {
                    offset = Integer.parseInt(offsetStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Số ngày nhắc nhở trước không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Recurrence recurrence = Recurrence.MONTHLY;
            int recSel = spinnerRecurrence.getSelectedItemPosition();
            if (recSel == 1) recurrence = Recurrence.QUARTERLY;
            else if (recSel == 2) recurrence = Recurrence.YEARLY;

            String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "Khác";

            LocalDateTime localDateTime = LocalDateTime.of(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    0
            );

            if (item == null) {
                // Thêm mới nhắc hẹn
                Reminder newReminder = new Reminder(
                        0, title, amount, localDateTime, recurrence, offset,
                        ReminderStatus.PENDING, category, 0
                );
                long id = dao.addReminder(newReminder);
                if (id > 0) {
                    newReminder.setId(id);
                    dataList.add(0, newReminder);
                    adapter.notifyItemInserted(0);
                    rv.smoothScrollToPosition(0);
                    updateEmptyState();
                    Toast.makeText(getContext(), "✅ Đã thêm nhắc hẹn mới thành công!", Toast.LENGTH_SHORT).show();
                    
                    NotificationHelper.showReminderNotification(
                            getContext(),
                            (int) id,
                            "Tạo nhắc hẹn thành công",
                            "Đã tạo nhắc hẹn \"" + newReminder.getTitle() + "\" lúc " + newReminder.getDueDate().format(dtf)
                    );
                    dialog.dismiss();
                }
            } else {
                // Sửa nhắc hẹn
                item.setTitle(title);
                item.setEstimatedAmount(amount);
                item.setDueDate(localDateTime);
                item.setRecurrence(recurrence);
                item.setReminderOffsetDays(offset);
                item.setCategory(category);

                int rows = dao.updateReminder(item);
                if (rows > 0) {
                    adapter.notifyItemChanged(position);
                    Toast.makeText(getContext(), "✅ Đã cập nhật nhắc hẹn thành công!", Toast.LENGTH_SHORT).show();
                    
                    NotificationHelper.showReminderNotification(
                            getContext(),
                            (int) item.getId(),
                            "Cập nhật nhắc hẹn thành công",
                            "Đã sửa nhắc hẹn \"" + item.getTitle() + "\""
                    );
                    dialog.dismiss();
                }
            }
        });
    }

    private void updateEmptyState() {
        if (dataList.isEmpty()) {
            rv.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rv.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dao != null) dao.close();
        if (categoryDAO != null) categoryDAO.close();
    }
}
