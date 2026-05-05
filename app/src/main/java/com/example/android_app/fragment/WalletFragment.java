package com.example.android_app.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.example.android_app.adapter.WalletAdapter;
import com.example.android_app.database.WalletDAO;
import com.example.android_app.model.Wallet;
import java.util.ArrayList;
import java.util.List;

public class WalletFragment extends Fragment {

    private RecyclerView rvWallets;
    private TextView tvTotalBalance;
    private WalletDAO walletDAO;
    private WalletAdapter adapter;
    private List<Wallet> wallets;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        rvWallets = view.findViewById(R.id.rvWallets);

        rvWallets.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWallets.setNestedScrollingEnabled(false);

        walletDAO = new WalletDAO(getContext());
        walletDAO.open();

        loadWallets();

        // Nút thêm ví
        view.findViewById(R.id.btnAddWallet).setOnClickListener(v -> showWalletDialog(null));
    }

    private void loadWallets() {
        wallets = walletDAO.getAllWallets();
        
        // Nếu chưa có ví nào trong DB, tạo ví mặc định
        if (wallets.isEmpty()) {
            walletDAO.addWallet(new Wallet(0, "Tiền mặt", 0, "cash", "VND", "cash", "#4CAF50"));
            walletDAO.addWallet(new Wallet(0, "Ngân hàng", 0, "bank", "VND", "bank", "#2196F3"));
            wallets = walletDAO.getAllWallets();
        }

        adapter = new WalletAdapter(getContext(), wallets);
        
        // Xử lý sự kiện Sửa ví bằng icon
        adapter.setOnEditClickListener(wallet -> showWalletDialog(wallet));
        
        // Xử lý sự kiện Xóa ví bằng icon
        adapter.setOnDeleteClickListener(wallet -> showDeleteConfirmDialog(wallet));

        rvWallets.setAdapter(adapter);

        // Tính tổng số dư
        double total = wallets.stream().mapToDouble(Wallet::getBalance).sum();
        tvTotalBalance.setText(formatMoney(total) + " ₫");
    }

    /**
     * Hiển thị hộp thoại Thêm mới hoặc Cập nhật ví
     * @param walletToEdit Nếu null => Thêm mới, Nếu có giá trị => Cập nhật
     */
    private void showWalletDialog(@Nullable Wallet walletToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_wallet, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etName = dialogView.findViewById(R.id.etWalletName);
        EditText etBalance = dialogView.findViewById(R.id.etWalletBalance);
        Spinner spinnerIcon = dialogView.findViewById(R.id.spinnerWalletIcon);
        Spinner spinnerColor = dialogView.findViewById(R.id.spinnerWalletColor);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelWallet);
        Button btnSave = dialogView.findViewById(R.id.btnSaveWallet);

        // Setup Spinners
        String[] icons = {"Tiền mặt (Cash)", "Ngân hàng (Bank)", "Tiết kiệm (Saving)"};
        String[] iconKeys = {"cash", "bank", "saving"};
        ArrayAdapter<String> iconAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, icons);
        iconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIcon.setAdapter(iconAdapter);

        String[] colors = {"Xanh lá", "Xanh dương", "Đỏ", "Cam", "Tím"};
        String[] colorHexes = {"#4CAF50", "#2196F3", "#F44336", "#FF9800", "#9C27B0"};
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, colors);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(colorAdapter);

        // Nạp dữ liệu nếu là Edit
        if (walletToEdit != null) {
            tvTitle.setText("Sửa thông tin ví");
            etName.setText(walletToEdit.getName());
            etBalance.setText(String.valueOf((long) walletToEdit.getBalance()));
            
            // Tìm vị trí của icon trong spinner
            for (int i = 0; i < iconKeys.length; i++) {
                if (iconKeys[i].equals(walletToEdit.getIcon())) {
                    spinnerIcon.setSelection(i);
                    break;
                }
            }

            // Tìm vị trí của màu trong spinner
            for (int i = 0; i < colorHexes.length; i++) {
                if (colorHexes[i].equalsIgnoreCase(walletToEdit.getColor())) {
                    spinnerColor.setSelection(i);
                    break;
                }
            }
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String balanceStr = etBalance.getText().toString().trim();

            if (name.isEmpty() || balanceStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            double balance = Double.parseDouble(balanceStr);
            String selectedIcon = iconKeys[spinnerIcon.getSelectedItemPosition()];
            String selectedColor = colorHexes[spinnerColor.getSelectedItemPosition()];
            String type = selectedIcon; // Dùng icon làm type cơ bản luôn

            if (walletToEdit == null) {
                // Thêm mới
                Wallet newWallet = new Wallet(0, name, balance, type, "VND", selectedIcon, selectedColor);
                walletDAO.addWallet(newWallet);
                Toast.makeText(getContext(), "Đã thêm ví", Toast.LENGTH_SHORT).show();
            } else {
                // Cập nhật
                walletToEdit.setName(name);
                walletToEdit.setBalance(balance);
                walletToEdit.setType(type);
                walletToEdit.setIcon(selectedIcon);
                walletToEdit.setColor(selectedColor);
                walletDAO.updateWallet(walletToEdit);
                Toast.makeText(getContext(), "Đã cập nhật ví", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
            loadWallets(); // Tải lại danh sách
        });

        dialog.show();
    }

    private void showDeleteConfirmDialog(Wallet wallet) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa ví")
                .setMessage("Bạn có chắc chắn muốn xóa ví '" + wallet.getName() + "' không? Các giao dịch liên quan có thể bị ảnh hưởng.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    walletDAO.deleteWallet(wallet.getId());
                    Toast.makeText(getContext(), "Đã xóa ví", Toast.LENGTH_SHORT).show();
                    loadWallets();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (walletDAO != null) {
            walletDAO.close();
        }
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f", amount).replace(",", ".");
    }
}

