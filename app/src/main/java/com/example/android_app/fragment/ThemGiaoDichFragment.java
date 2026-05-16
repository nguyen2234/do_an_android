package com.example.android_app.fragment;

import android.app.DatePickerDialog;
import android.graphics.Color;
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
import androidx.fragment.app.Fragment;
import com.example.android_app.R;
import com.example.android_app.database.GiaoDichDAO;
import com.example.android_app.database.NganSachDAO;
import com.example.android_app.database.ViTienDAO;
import com.example.android_app.database.DanhMucDAO;
import com.example.android_app.model.GiaoDich;
import com.example.android_app.model.NganSach;
import com.example.android_app.model.ViTien;
import com.example.android_app.model.DanhMuc;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.List;
import java.util.Locale;

public class ThemGiaoDichFragment extends Fragment {

    private boolean isExpense = true;
    private TextView btnExpenseTab, btnIncomeTab, tvNgayThang, tvAmountDisplay;
    private EditText etSoTien, etGhiChu;
    private Spinner spinnerCategory, spinnerWallet;
    private Calendar selectedDate = Calendar.getInstance();

    private GiaoDichDAO transactionDAO;
    private ViTienDAO walletDAO;
    private NganSachDAO budgetDAO;
    private DanhMucDAO categoryDAO;
    private List<ViTien> walletList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_them_giao_dich, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionDAO = new GiaoDichDAO(getContext());
        walletDAO = new ViTienDAO(getContext());
        budgetDAO = new NganSachDAO(getContext());
        categoryDAO = new DanhMucDAO(getContext());
        transactionDAO.open();
        walletDAO.open();
        budgetDAO.open();
        categoryDAO.open();

        btnExpenseTab = view.findViewById(R.id.btnExpenseTab);
        btnIncomeTab = view.findViewById(R.id.btnIncomeTab);
        tvNgayThang = view.findViewById(R.id.tvNgayThang);
        tvAmountDisplay = view.findViewById(R.id.tvAmountDisplay);
        etSoTien = view.findViewById(R.id.etSoTien);
        etGhiChu = view.findViewById(R.id.etGhiChu);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerWallet = view.findViewById(R.id.spinnerWallet);

        // Set current date
        updateDateDisplay();

        // Setup Spinners
        setupCategorySpinner();
        setupWalletSpinner();

        // Tab switch
        btnExpenseTab.setOnClickListener(v -> setTransactionType(true));
        btnIncomeTab.setOnClickListener(v -> setTransactionType(false));

        // Date picker
        view.findViewById(R.id.btnDate).setOnClickListener(v -> showDatePicker());

        // Amount update
        etSoTien.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = s.toString().trim();
                if (!val.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(val);
                        String formatted = String.format("%,.0f", amount).replace(",", ".") + " ₫";
                        tvAmountDisplay.setText(formatted);
                    } catch (NumberFormatException ignored) {}
                } else {
                    tvAmountDisplay.setText("0 ₫");
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // Save button
        MaterialButton btnLuu = view.findViewById(R.id.btnLuu);
        btnLuu.setOnClickListener(v -> saveTransaction());
    }

    private void setTransactionType(boolean expense) {
        isExpense = expense;
        if (expense) {
            btnExpenseTab.setBackgroundResource(R.drawable.bg_card_primary);
            btnExpenseTab.setTextColor(Color.WHITE);
            btnIncomeTab.setBackgroundResource(android.R.color.transparent);
            btnIncomeTab.setTextColor(Color.parseColor("#634832"));
        } else {
            btnIncomeTab.setBackgroundResource(R.drawable.bg_card_primary);
            btnIncomeTab.setTextColor(Color.WHITE);
            btnExpenseTab.setBackgroundResource(android.R.color.transparent);
            btnExpenseTab.setTextColor(Color.parseColor("#634832"));
        }
        setupCategorySpinner();
    }

    private void setupCategorySpinner() {
        String type = isExpense ? "expense" : "income";
        List<DanhMuc> categories = categoryDAO.getCategoriesByType(type);
        List<String> categoryNames = new ArrayList<>();
        
        if (categories.isEmpty()) {
            if (isExpense) {
                categoryDAO.addCategory(new DanhMuc(0, "Ăn uống", "ic_food", "expense", Color.parseColor("#E74C3C")));
                categoryDAO.addCategory(new DanhMuc(0, "Di chuyển", "ic_transport", "expense", Color.parseColor("#3498DB")));
                categoryDAO.addCategory(new DanhMuc(0, "Khác", "ic_other", "expense", Color.parseColor("#95A5A6")));
            } else {
                categoryDAO.addCategory(new DanhMuc(0, "Lương", "ic_salary", "income", Color.parseColor("#2ECC71")));
                categoryDAO.addCategory(new DanhMuc(0, "Khác", "ic_other", "income", Color.parseColor("#95A5A6")));
            }
            categories = categoryDAO.getCategoriesByType(type);
        }

        for (DanhMuc c : categories) {
            categoryNames.add(c.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupWalletSpinner() {
        walletList = walletDAO.getAllWallets();
        List<String> walletNames = new ArrayList<>();
        
        if (walletList.isEmpty()) {
            // Thêm ví mặc định nếu trống
            walletDAO.addWallet(new ViTien(0, "Tiền mặt", 0, "cash", "VND"));
            walletList = walletDAO.getAllWallets();
        }
        
        for (ViTien w : walletList) {
            walletNames.add(w.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, walletNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWallet.setAdapter(adapter);
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                (picker, year, month, day) -> {
                    selectedDate.set(year, month, day);
                    updateDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
        tvNgayThang.setText(sdf.format(selectedDate.getTime()));
    }

    private void saveTransaction() {
        String amountStr = etSoTien.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String category = spinnerCategory.getSelectedItem().toString();
        String note = etGhiChu.getText().toString();
        String date = tvNgayThang.getText().toString();
        String type = isExpense ? "expense" : "income";
        
        // Lấy ID ví được chọn
        int selectedWalletIndex = spinnerWallet.getSelectedItemPosition();
        ViTien selectedWallet = walletList.get(selectedWalletIndex);

        // Tạo đối tượng GiaoDich
        GiaoDich transaction = new GiaoDich(0, category, amount, category, type, date, note, selectedWallet.getId());
        
        // Lưu vào CSDL
        long id = transactionDAO.addTransaction(transaction);
        if (id > 0) {
            // Cập nhật số dư ví
            double newBalance = isExpense ? selectedWallet.getBalance() - amount : selectedWallet.getBalance() + amount;
            walletDAO.updateBalance(selectedWallet.getId(), newBalance);
            
            if (isExpense) {
                capNhatNganSach(date, category, amount);
            }
            
            // Reset form
            etSoTien.setText("");
            etGhiChu.setText("");
            tvAmountDisplay.setText("0 ₫");

            // Chuyển sang màn hình thông báo thành công
            android.content.Intent intent = new android.content.Intent(getActivity(), com.example.android_app.KetQuaGiaoDichActivity.class);
            intent.putExtra("isSuccess", true);
            intent.putExtra("amount", amount);
            intent.putExtra("type", type);
            startActivity(intent);
        } else {
            // Chuyển sang màn hình thông báo thất bại
            android.content.Intent intent = new android.content.Intent(getActivity(), com.example.android_app.KetQuaGiaoDichActivity.class);
            intent.putExtra("isSuccess", false);
            intent.putExtra("amount", amount);
            intent.putExtra("type", type);
            startActivity(intent);
        }
    }

    private void capNhatNganSach(String transDateStr, String category, double amount) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
            Date transDate = sdf.parse(transDateStr);

            List<NganSach> list = budgetDAO.getAllBudgets();
            for (NganSach b : list) {
                Date start = sdf.parse(b.getStartDate());
                Date end = sdf.parse(b.getEndDate());

                if (!transDate.before(start) && !transDate.after(end)) {
                    if (b.getCategoryIds() != null && b.getCategoryIds().contains(category)) {
                        budgetDAO.updateSpentAmount(b.getId(), b.getSpentAmount() + amount);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (transactionDAO != null) transactionDAO.close();
        if (walletDAO != null) walletDAO.close();
        if (budgetDAO != null) budgetDAO.close();
        if (categoryDAO != null) categoryDAO.close();
    }
}
