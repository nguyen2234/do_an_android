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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_app.R;
import com.example.android_app.database.DanhMucDAO;
import com.example.android_app.database.GiaoDichDAO;
import com.example.android_app.database.ViTienDAO;
import com.example.android_app.model.DanhMuc;
import com.example.android_app.model.GiaoDich;
import com.example.android_app.model.ViTien;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ThemGiaoDichFragment extends Fragment {

    private boolean isExpense = true;
    private TextView btnExpenseTab, btnIncomeTab, tvNgayThang, tvAmountDisplay;
    private EditText etSoTien, etGhiChu;
    private Spinner spinnerCategory, spinnerWallet;
    private RecyclerView rvCategories;
    private Calendar selectedDate = Calendar.getInstance();

    private GiaoDichDAO transactionDAO;
    private ViTienDAO walletDAO;
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
        transactionDAO.open();
        walletDAO.open();

        btnExpenseTab = view.findViewById(R.id.btnExpenseTab);
        btnIncomeTab = view.findViewById(R.id.btnIncomeTab);
        tvNgayThang = view.findViewById(R.id.tvNgayThang);
        tvAmountDisplay = view.findViewById(R.id.tvAmountDisplay);
        etSoTien = view.findViewById(R.id.etSoTien);
        etGhiChu = view.findViewById(R.id.etGhiChu);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerWallet = view.findViewById(R.id.spinnerWallet);
        rvCategories = view.findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

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
        DanhMucDAO categoryDAO = new DanhMucDAO(getContext());
        categoryDAO.open();
        String type = isExpense ? "expense" : "income";
        List<DanhMuc> dbCategories = categoryDAO.getCategoriesByType(type);
        List<String> categoryNames = new ArrayList<>();

        if (dbCategories.isEmpty()) {
            String[] defaultCats = isExpense
                    ? new String[]{"Ăn uống", "Di chuyển", "Mua sắm", "Y tế", "Giải trí", "Giáo dục", "Khác"}
                    : new String[]{"Lương", "Thưởng", "Đầu tư", "Quà tặng", "Khác"};
            for (String cat : defaultCats) {
                categoryNames.add(cat);
                DanhMuc dm = new DanhMuc();
                dm.setName(cat);
                dm.setLoai(type);
                categoryDAO.addCategory(dm);
            }
        } else {
            for (DanhMuc cat : dbCategories) {
                categoryNames.add(cat.getName());
            }
        }
        categoryDAO.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        setupQuickCategories(categoryNames);
    }

    private void setupQuickCategories(List<String> categoryNames) {
        QuickCategoryAdapter adapter = new QuickCategoryAdapter(categoryNames, category -> {
            for (int i = 0; i < spinnerCategory.getCount(); i++) {
                if (spinnerCategory.getItemAtPosition(i).toString().equals(category)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        });
        rvCategories.setAdapter(adapter);
    }

    class QuickCategoryAdapter extends RecyclerView.Adapter<QuickCategoryAdapter.ViewHolder> {
        private List<String> categories;
        private OnCategorySelectedListener listener;

        public QuickCategoryAdapter(List<String> categories, OnCategorySelectedListener listener) {
            this.categories = categories;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 24, 0);
            tv.setLayoutParams(params);
            tv.setPadding(40, 20, 40, 20);
            tv.setBackgroundResource(R.drawable.bg_input);
            tv.setTextColor(Color.parseColor("#1A1A2E"));
            tv.setTextSize(14f);
            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String cat = categories.get(position);
            ((TextView)holder.itemView).setText(cat);
            holder.itemView.setOnClickListener(v -> listener.onSelected(cat));
        }

        @Override
        public int getItemCount() { return categories.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(@NonNull View itemView) { super(itemView); }
        }
    }

    interface OnCategorySelectedListener {
        void onSelected(String category);
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
            
            // Reset form
            etSoTien.setText("");
            etGhiChu.setText("");
            tvAmountDisplay.setText("0 ₫");

            // Chuyển sang màn hình thông báo thành công
            android.content.Intent intent = new android.content.Intent(getActivity(), com.example.android_app.KetQuaGiaoDichActivity.class);
            intent.putExtra("isSuccess", true);
            intent.putExtra("amount", amount);
            intent.putExtra("type", type);
            intent.putExtra("category", category);
            intent.putExtra("walletName", selectedWallet.getName());
            intent.putExtra("note", note);
            startActivity(intent);
        } else {
            // Chuyển sang màn hình thông báo thất bại
            android.content.Intent intent = new android.content.Intent(getActivity(), com.example.android_app.KetQuaGiaoDichActivity.class);
            intent.putExtra("isSuccess", false);
            intent.putExtra("amount", amount);
            intent.putExtra("type", type);
            intent.putExtra("category", category);
            intent.putExtra("walletName", selectedWallet.getName());
            intent.putExtra("note", note);
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
