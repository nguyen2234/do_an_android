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
import com.example.android_app.database.TransactionDAO;
import com.example.android_app.database.WalletDAO;
import com.example.android_app.model.Transaction;
import com.example.android_app.model.Wallet;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTransactionFragment extends Fragment {

    private boolean isExpense = true;
    private TextView btnExpenseTab, btnIncomeTab, tvDate, tvAmountDisplay;
    private EditText etAmount, etNote;
    private Spinner spinnerCategory, spinnerWallet;
    private Calendar selectedDate = Calendar.getInstance();

    private TransactionDAO transactionDAO;
    private WalletDAO walletDAO;
    private List<Wallet> walletList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionDAO = new TransactionDAO(getContext());
        walletDAO = new WalletDAO(getContext());
        transactionDAO.open();
        walletDAO.open();

        btnExpenseTab = view.findViewById(R.id.btnExpenseTab);
        btnIncomeTab = view.findViewById(R.id.btnIncomeTab);
        tvDate = view.findViewById(R.id.tvDate);
        tvAmountDisplay = view.findViewById(R.id.tvAmountDisplay);
        etAmount = view.findViewById(R.id.etAmount);
        etNote = view.findViewById(R.id.etNote);
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
        etAmount.addTextChangedListener(new android.text.TextWatcher() {
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
        MaterialButton btnSave = view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveTransaction());
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
        String[] categories = isExpense
                ? new String[]{"Ăn uống", "Di chuyển", "Mua sắm", "Y tế", "Giải trí", "Giáo dục", "Khác"}
                : new String[]{"Lương", "Thưởng", "Đầu tư", "Quà tặng", "Khác"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupWalletSpinner() {
        walletList = walletDAO.getAllWallets();
        List<String> walletNames = new ArrayList<>();
        
        if (walletList.isEmpty()) {
            // Thêm ví mặc định nếu trống
            walletDAO.addWallet(new Wallet(0, "Tiền mặt", 0, "cash", "VND"));
            walletList = walletDAO.getAllWallets();
        }
        
        for (Wallet w : walletList) {
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
        tvDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String category = spinnerCategory.getSelectedItem().toString();
        String note = etNote.getText().toString();
        String date = tvDate.getText().toString();
        String type = isExpense ? "expense" : "income";
        
        // Lấy ID ví được chọn
        int selectedWalletIndex = spinnerWallet.getSelectedItemPosition();
        Wallet selectedWallet = walletList.get(selectedWalletIndex);

        // Tạo đối tượng Transaction
        Transaction transaction = new Transaction(0, category, amount, category, type, date, note, selectedWallet.getId());
        
        // Lưu vào CSDL
        long id = transactionDAO.addTransaction(transaction);
        if (id > 0) {
            // Cập nhật số dư ví
            double newBalance = isExpense ? selectedWallet.getBalance() - amount : selectedWallet.getBalance() + amount;
            walletDAO.updateBalance(selectedWallet.getId(), newBalance);
            
            // Reset form
            etAmount.setText("");
            etNote.setText("");
            tvAmountDisplay.setText("0 ₫");

            // Chuyển sang màn hình thông báo thành công
            android.content.Intent intent = new android.content.Intent(getActivity(), com.example.android_app.TransactionResultActivity.class);
            intent.putExtra("isSuccess", true);
            intent.putExtra("amount", amount);
            intent.putExtra("type", type);
            startActivity(intent);
        } else {
            // Chuyển sang màn hình thông báo thất bại
            android.content.Intent intent = new android.content.Intent(getActivity(), com.example.android_app.TransactionResultActivity.class);
            intent.putExtra("isSuccess", false);
            intent.putExtra("amount", amount);
            intent.putExtra("type", type);
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (transactionDAO != null) transactionDAO.close();
        if (walletDAO != null) walletDAO.close();
    }
}
