package com.example.android_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.R;
import com.example.android_app.adapter.ViTienAdapter;
import com.example.android_app.database.ViTienDAO;
import com.example.android_app.model.ViTien;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChuyenTienFragment extends Fragment {

    private EditText etInternalTarget, etExternalTarget, etAmount;
    private AutoCompleteTextView autoCompleteBank;
    private TextView tvSourceBankName, tvSourceAccountNumber;
    private ImageView ivSourceIcon;
    private MaterialButton btnTransfer;
    private ViTienDAO viTienDAO;
    private ViTien sourceWallet, selectedTargetWallet;
    
    private TabLayout tabLayout;
    private View cardInternal, cardExternal;
    private RecyclerView rvTargetWallets;
    private ViTienAdapter targetAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chuyen_tien, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        tabLayout = view.findViewById(R.id.tabLayoutTransfer);
        cardInternal = view.findViewById(R.id.cardInternalTransfer);
        cardExternal = view.findViewById(R.id.cardExternalTransfer);
        rvTargetWallets = view.findViewById(R.id.rvTargetWallets);

        etInternalTarget = view.findViewById(R.id.etInternalTarget);
        etExternalTarget = view.findViewById(R.id.etExternalTarget);
        autoCompleteBank = view.findViewById(R.id.autoCompleteBank);
        etAmount = view.findViewById(R.id.etAmount);
        btnTransfer = view.findViewById(R.id.btnTransfer);

        tvSourceBankName = view.findViewById(R.id.tvSourceBankName);
        tvSourceAccountNumber = view.findViewById(R.id.tvSourceAccountNumber);
        ivSourceIcon = view.findViewById(R.id.ivSourceIcon);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (isAdded()) getParentFragmentManager().popBackStack();
            });
        }

        viTienDAO = new ViTienDAO(requireContext());
        viTienDAO.open();

        setupTabs();
        setupSourceWallet();
        setupBankDropdown();
        setupInternalTargetWallets();

        if (btnTransfer != null) {
            btnTransfer.setOnClickListener(v -> performTransfer());
        }
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    cardInternal.setVisibility(View.VISIBLE);
                    cardExternal.setVisibility(View.GONE);
                } else {
                    cardInternal.setVisibility(View.GONE);
                    cardExternal.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSourceWallet() {
        if (viTienDAO == null) return;
        List<ViTien> wallets = viTienDAO.getAllWallets();
        if (!wallets.isEmpty()) {
            sourceWallet = wallets.get(0);
            updateSourceUI();
        }
    }

    private void updateSourceUI() {
        if (sourceWallet == null) return;
        tvSourceBankName.setText(sourceWallet.getName());
        tvSourceAccountNumber.setText("Số dư: " + String.format(Locale.GERMANY, "%,.0f", sourceWallet.getBalance()) + " ₫");
        if (ivSourceIcon != null) ivSourceIcon.setImageResource(android.R.drawable.ic_menu_agenda);
    }

    private void setupInternalTargetWallets() {
        if (viTienDAO == null) return;
        List<ViTien> allWallets = viTienDAO.getAllWallets();
        List<ViTien> otherWallets = new ArrayList<>();
        
        for (ViTien w : allWallets) {
            if (sourceWallet == null || w.getId() != sourceWallet.getId()) {
                otherWallets.add(w);
            }
        }

        targetAdapter = new ViTienAdapter(requireContext(), otherWallets, true);
        targetAdapter.setOnItemClickListener(wallet -> {
            selectedTargetWallet = wallet;
            targetAdapter.setSelectedId(wallet.getId());
            etInternalTarget.setText(wallet.getName());
        });
        
        rvTargetWallets.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTargetWallets.setAdapter(targetAdapter);
    }

    private void setupBankDropdown() {
        String[] banks = {"Vietcombank", "Techcombank", "MB Bank", "Agribank", "BIDV", "VPBank", "Sacombank", "ACB"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, banks);
        if (autoCompleteBank != null) {
            autoCompleteBank.setAdapter(adapter);
        }
    }

    private void performTransfer() {
        if (etAmount == null) return;
        
        String amountStr = etAmount.getText().toString();
        if (amountStr.isEmpty()) {
            etAmount.setError("Vui lòng nhập số tiền");
            return;
        }

        double amount = Double.parseDouble(amountStr);
        if (sourceWallet == null) return;
        if (sourceWallet.getBalance() < amount) {
            Toast.makeText(getContext(), "Số dư không đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tabLayout.getSelectedTabPosition() == 0) {
            // Chuyển tiền nội bộ
            String targetText = etInternalTarget.getText().toString().trim();
            if (targetText.isEmpty()) {
                etInternalTarget.setError("Vui lòng nhập STK hoặc chọn ví");
                return;
            }

            ViTien target = selectedTargetWallet;
            if (target == null || (!target.getName().equals(targetText) && (target.getAccountNumber() == null || !target.getAccountNumber().equals(targetText)))) {
                // Check if manually typed text matches any wallet
                List<ViTien> wallets = viTienDAO.getAllWallets();
                for (ViTien w : wallets) {
                    if (w.getId() != sourceWallet.getId() && (w.getName().equalsIgnoreCase(targetText) || (w.getAccountNumber() != null && w.getAccountNumber().equals(targetText)))) {
                        target = w;
                        break;
                    }
                }
            }

            if (target != null) {
                if (viTienDAO.transferMoney(sourceWallet.getId(), target.getId(), amount)) {
                    Toast.makeText(getContext(), "Chuyển tiền nội bộ thành công!", Toast.LENGTH_SHORT).show();
                    if (isAdded()) getParentFragmentManager().popBackStack();
                }
            } else {
                Toast.makeText(getContext(), "Không tìm thấy ví nhận phù hợp", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Chuyển tiền liên ngân hàng
            String targetSTK = etExternalTarget.getText().toString().trim();
            String bank = autoCompleteBank.getText().toString().trim();

            if (targetSTK.isEmpty() || bank.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin ngân hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            if (viTienDAO.updateBalance(sourceWallet.getId(), sourceWallet.getBalance() - amount) > 0) {
                Toast.makeText(getContext(), "Chuyển tiền liên ngân hàng thành công!", Toast.LENGTH_SHORT).show();
                if (isAdded()) getParentFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viTienDAO != null) viTienDAO.close();
    }
}
