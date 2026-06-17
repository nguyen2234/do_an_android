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
import com.example.android_app.database.NguoiDungDAO;
import com.example.android_app.model.GiaoDich;
import com.example.android_app.model.NganSach;
import com.example.android_app.model.ViTien;
import com.example.android_app.model.DanhMuc;
import com.example.android_app.model.NguoiDung;
import com.example.android_app.utils.NotificationHelper;
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
    private View layoutCategoryInput;
    private Calendar selectedDate = Calendar.getInstance();

    private long prefilledReminderId = -1;

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
        layoutCategoryInput = view.findViewById(R.id.layoutCategoryInput);

        
        Bundle args = getArguments();
        if (args != null) {
            double prefilledAmount = args.getDouble("prefilled_amount", -1);
            prefilledReminderId = args.getLong("reminder_id", -1);
            String prefilledTitle = args.getString("prefilled_title", null);

            if (prefilledAmount > 0 && etSoTien != null) {
                etSoTien.setText(String.valueOf((int) prefilledAmount));
                if (tvAmountDisplay != null) {
                    tvAmountDisplay.setText(String.format("%,.0f", prefilledAmount).replace(",", ".") + " ₫");
                }
            }
            if (prefilledTitle != null && etGhiChu != null) {
                etGhiChu.setText("Thanh toán tiền " + prefilledTitle);
            }
        }

        
        updateDateDisplay();

        
        setupCategorySpinner();
        setupWalletSpinner();

        
        btnExpenseTab.setOnClickListener(v -> setTransactionType(true));
        btnIncomeTab.setOnClickListener(v -> setTransactionType(false));

        
        setTransactionType(true);

        
        view.findViewById(R.id.btnDate).setOnClickListener(v -> showDatePicker());

        
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
            if (layoutCategoryInput != null) {
                layoutCategoryInput.setVisibility(View.VISIBLE);
            }
        } else {
            btnIncomeTab.setBackgroundResource(R.drawable.bg_card_primary);
            btnIncomeTab.setTextColor(Color.WHITE);
            btnExpenseTab.setBackgroundResource(android.R.color.transparent);
            btnExpenseTab.setTextColor(Color.parseColor("#634832"));
            if (layoutCategoryInput != null) {
                layoutCategoryInput.setVisibility(View.GONE);
            }
        }
        setupCategorySpinner();
    }

    private void setupCategorySpinner() {
        List<DanhMuc> categories = categoryDAO.getAllCategories();
        List<String> categoryNames = new ArrayList<>();
        
        if (categories.isEmpty()) {
            categoryDAO.addCategory(new DanhMuc(0, "Ăn uống", "ic_food", "general", Color.parseColor("#E74C3C")));
            categoryDAO.addCategory(new DanhMuc(0, "Di chuyển", "ic_transport", "general", Color.parseColor("#3498DB")));
            categoryDAO.addCategory(new DanhMuc(0, "Lương", "ic_salary", "general", Color.parseColor("#2ECC71")));
            categories = categoryDAO.getAllCategories();
        }

        for (DanhMuc c : categories) {
            categoryNames.add(c.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.item_spinner, categoryNames);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerCategory.setAdapter(adapter);

        
        Bundle args = getArguments();
        if (args != null) {
            String prefilledCategory = args.getString("prefilled_category", null);
            if (prefilledCategory != null) {
                int index = categoryNames.indexOf(prefilledCategory);
                if (index != -1) {
                    spinnerCategory.setSelection(index);
                }
            }
        }
    }

    private void setupWalletSpinner() {
        walletList = walletDAO.getAllWallets();
        List<String> walletNames = new ArrayList<>();
        
        if (walletList.isEmpty()) {
            
            walletDAO.addWallet(new ViTien(0, "Tiền mặt", 0, "cash", "VND"));
            walletList = walletDAO.getAllWallets();
        }
        
        for (ViTien w : walletList) {
            walletNames.add(w.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.item_spinner, walletNames);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
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
        String category = isExpense ? (spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "Chi tiêu") : "Nạp tiền";
        String note = etGhiChu.getText().toString();

        
        String date = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi")).format(Calendar.getInstance().getTime());
        String type = isExpense ? "expense" : "income";
        
        
        int selectedWalletIndex = spinnerWallet.getSelectedItemPosition();
        if (selectedWalletIndex < 0 || walletList == null || walletList.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ví", Toast.LENGTH_SHORT).show();
            return;
        }
        ViTien selectedWallet = walletList.get(selectedWalletIndex);

        
        if (isExpense) {
            ViTien currentWallet = walletDAO.getWalletById(selectedWallet.getId());
            double currentBalance = (currentWallet != null) ? currentWallet.getBalance() : 0;
            if (amount > currentBalance) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Số dư không đủ")
                        .setMessage("Số dư ví không đủ. Vui lòng nạp thêm tiền vào ví này để tiếp tục!")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }
        }

        
        if (isExpense) {
            NganSach activeBudget = getActiveBudgetForCategoryAndDate(category, date);
            if (activeBudget != null) {
                double newTotalSpent = activeBudget.getSpentAmount() + amount;
                if (newTotalSpent > activeBudget.getAmount()) {
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Cảnh báo vượt ngân sách")
                            .setMessage("Mức chi tiêu của bạn vào danh mục sẽ bị vượt quá ngân sách tương đương")
                            .setPositiveButton("Xác nhận", (dialog, which) -> {
                                
                                showPinVerificationDialog(amount, category, note, date, type, selectedWallet);
                            })
                            .setNegativeButton("Trở về", null)
                            .show();
                    return;
                }
            }
        }

        
        showPinVerificationDialog(amount, category, note, date, type, selectedWallet);
    }

    
    private NganSach getActiveBudgetForCategoryAndDate(String categoryName, String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
            Date transDate = sdf.parse(dateStr);

            List<NganSach> list = budgetDAO.getAllBudgets();
            for (NganSach b : list) {
                Date start = sdf.parse(b.getStartDate());
                Date end = sdf.parse(b.getEndDate());

                if (!transDate.before(start) && !transDate.after(end)) {
                    if (b.getCategoryIds() != null && b.getCategoryIds().contains(categoryName)) {
                        return b;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showPinVerificationDialog(final double amount, final String category, final String note, final String date, final String type, final ViTien selectedWallet) {
        if (getContext() == null) return;

        android.content.SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
        long userId = prefs.getLong("user_id", -1);
        if (userId == -1) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy người dùng hiện tại", Toast.LENGTH_SHORT).show();
            return;
        }

        NguoiDungDAO uDAO = new NguoiDungDAO(getContext());
        uDAO.open();
        NguoiDung user = uDAO.getUserById(userId);
        uDAO.close();

        final String correctPin = (user != null) ? user.getTransactionPin() : "";

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_verify_pin, null);
        final EditText etVerifyPin = dialogView.findViewById(R.id.etVerifyPin);
        android.widget.Button btnCancelVerify = dialogView.findViewById(R.id.btnCancelVerify);
        android.widget.Button btnConfirmVerify = dialogView.findViewById(R.id.btnConfirmVerify);

        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnCancelVerify.setOnClickListener(v -> dialog.dismiss());

        btnConfirmVerify.setOnClickListener(v -> {
            String enteredPin = etVerifyPin.getText().toString().trim();
            if (enteredPin.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập mã PIN", Toast.LENGTH_SHORT).show();
                return;
            }

            if (enteredPin.equals(correctPin)) {
                dialog.dismiss();
                executeSaveTransaction(amount, category, note, date, type, selectedWallet);
            } else {
                Toast.makeText(getContext(), "Mã PIN giao dịch không chính xác!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void executeSaveTransaction(double amount, String category, String note, String date, String type, ViTien selectedWallet) {
        
        GiaoDich transaction = new GiaoDich(0, category, amount, category, type, date, note, selectedWallet.getId());
        
        
        long id = transactionDAO.addTransaction(transaction);
        if (id > 0) {
            
            double newBalance = isExpense ? selectedWallet.getBalance() - amount : selectedWallet.getBalance() + amount;
            walletDAO.updateBalance(selectedWallet.getId(), newBalance);

            
            if (isExpense) {
                NotificationHelper.showExpenseNotification(getContext(), selectedWallet.getName(), amount, category);
                capNhatNganSach(date, category, amount);
                ViTien updatedWallet = walletDAO.getWalletById(selectedWallet.getId());
                if (updatedWallet != null && updatedWallet.getMinBalance() > 0
                        && newBalance < updatedWallet.getMinBalance()) {
                    NotificationHelper.showLowBalanceNotification(
                            getContext(), selectedWallet.getName(), newBalance);
                }
            } else {
                NotificationHelper.showTopUpNotification(getContext(), selectedWallet.getName(), amount);
            }

            
            if (prefilledReminderId > 0) {
                com.example.android_app.database.ReminderDAO rDAO = new com.example.android_app.database.ReminderDAO(getContext());
                rDAO.open();
                rDAO.markAsPaid(prefilledReminderId);
                rDAO.close();

                Toast.makeText(getContext(), "✅ Đã thanh toán nhắc hẹn thành công!", Toast.LENGTH_SHORT).show();
                prefilledReminderId = -1; 
            }
            
            
            etSoTien.setText("");
            etGhiChu.setText("");
            tvAmountDisplay.setText("0 ₫");

            
            android.content.Intent intent = new android.content.Intent(getActivity(), com.example.android_app.KetQuaGiaoDichActivity.class);
            intent.putExtra("isSuccess", true);
            intent.putExtra("amount", amount);
            intent.putExtra("type", type);
            startActivity(intent);
        } else {
            
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
