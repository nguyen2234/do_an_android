package com.example.android_app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_app.ChiTietNganSachActivity;
import com.example.android_app.R;
import com.example.android_app.ThemNganSachActivity;
import com.example.android_app.adapter.NganSachAdapter;
import com.example.android_app.database.NganSachDAO;
import com.example.android_app.model.NganSach;
import java.util.List;

public class NganSachFragment extends Fragment {

    private RecyclerView rvBudgets;
    private NganSachAdapter adapter;
    private NganSachDAO budgetDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ngan_sach, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvBudgets = view.findViewById(R.id.rvBudgets);
        rvBudgets.setLayoutManager(new LinearLayoutManager(getContext()));

        budgetDAO = new NganSachDAO(getContext());

        Button btnAddBudget = view.findViewById(R.id.btnAddBudget);
        btnAddBudget.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ThemNganSachActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBudgets();
    }

    private void loadBudgets() {
        budgetDAO.open();
        List<NganSach> list = budgetDAO.getAllBudgets();
        adapter = new NganSachAdapter(getContext(), list);

        // Wire click listener: mở màn hình Chi tiết khi bấm vào một thẻ ngân sách
        adapter.setOnItemClickListener(budget -> {
            Intent intent = new Intent(getContext(), ChiTietNganSachActivity.class);
            intent.putExtra(ChiTietNganSachActivity.EXTRA_BUDGET_ID, budget.getId());
            startActivity(intent);
        });

        rvBudgets.setAdapter(adapter);
        budgetDAO.close();
    }
}

