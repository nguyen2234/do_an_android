package com.example.android_app.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.R;
import com.example.android_app.adapter.DanhMucAdapter;
import com.example.android_app.database.DanhMucDAO;
import com.example.android_app.model.DanhMuc;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class DanhMucFragment extends Fragment {

    private RecyclerView rvCategories;
    private DanhMucAdapter adapter;
    private DanhMucDAO categoryDAO;
    private MaterialButtonToggleGroup toggleGroupType;
    private FloatingActionButton fabAddCategory;

    private String currentFilter = "all"; // all, expense, income

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_danh_muc, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCategories = view.findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        toggleGroupType = view.findViewById(R.id.toggleGroupType);
        fabAddCategory = view.findViewById(R.id.fabAddCategory);

        categoryDAO = new DanhMucDAO(getContext());
        categoryDAO.open();

        loadCategories();

        toggleGroupType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnFilterAll) {
                    currentFilter = "all";
                } else if (checkedId == R.id.btnFilterExpense) {
                    currentFilter = "expense";
                } else if (checkedId == R.id.btnFilterIncome) {
                    currentFilter = "income";
                }
                loadCategories();
            }
        });

        fabAddCategory.setOnClickListener(v -> showCategoryDialog(null));
    }

    private void loadCategories() {
        List<DanhMuc> list;
        if (currentFilter.equals("expense")) {
            list = categoryDAO.getCategoriesByType("expense");
        } else if (currentFilter.equals("income")) {
            list = categoryDAO.getCategoriesByType("income");
        } else {
            list = categoryDAO.getAllCategories();
        }

        // Thêm danh mục mặc định nếu chưa có (chỉ check khi all)
        if (currentFilter.equals("all") && list.isEmpty()) {
            categoryDAO.addCategory(new DanhMuc(0, "Ăn uống", "ic_food", "expense", Color.parseColor("#E74C3C")));
            categoryDAO.addCategory(new DanhMuc(0, "Di chuyển", "ic_transport", "expense", Color.parseColor("#3498DB")));
            categoryDAO.addCategory(new DanhMuc(0, "Lương", "ic_salary", "income", Color.parseColor("#2ECC71")));
            list = categoryDAO.getAllCategories();
        }

        adapter = new DanhMucAdapter(getContext(), list, new DanhMucAdapter.OnCategoryClickListener() {
            @Override
            public void onEditClick(DanhMuc category) {
                showCategoryDialog(category);
            }

            @Override
            public void onDeleteClick(DanhMuc category) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Xóa Danh Mục")
                        .setMessage("Bạn có chắc chắn muốn xóa danh mục này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            categoryDAO.deleteCategory(category.getId());
                            Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                            loadCategories();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
        rvCategories.setAdapter(adapter);
    }

    private void showCategoryDialog(DanhMuc existingCategory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_them_danh_muc, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etName = dialogView.findViewById(R.id.etCategoryName);
        RadioGroup rgType = dialogView.findViewById(R.id.rgCategoryType);
        RadioButton rbExpense = dialogView.findViewById(R.id.rbExpense);
        RadioButton rbIncome = dialogView.findViewById(R.id.rbIncome);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelCategory);
        Button btnSave = dialogView.findViewById(R.id.btnSaveCategory);

        View[] colorViews = {
                dialogView.findViewById(R.id.color1),
                dialogView.findViewById(R.id.color2),
                dialogView.findViewById(R.id.color3),
                dialogView.findViewById(R.id.color4),
                dialogView.findViewById(R.id.color5)
        };
        int[] colors = {Color.parseColor("#8B5CF6"), Color.parseColor("#E74C3C"), Color.parseColor("#2ECC71"), Color.parseColor("#F1C40F"), Color.parseColor("#3498DB")};
        final int[] selectedColor = {colors[0]};

        for (int i = 0; i < colorViews.length; i++) {
            final int index = i;
            colorViews[i].setOnClickListener(v -> {
                selectedColor[0] = colors[index];
                for (View cv : colorViews) {
                    cv.setAlpha(0.3f);
                }
                v.setAlpha(1.0f);
            });
            // Init alpha
            if (i == 0) colorViews[i].setAlpha(1.0f);
            else colorViews[i].setAlpha(0.3f);
        }

        if (existingCategory != null) {
            tvTitle.setText("Sửa Danh mục");
            etName.setText(existingCategory.getName());
            if (existingCategory.getLoai().equals("income")) {
                rbIncome.setChecked(true);
            } else {
                rbExpense.setChecked(true);
            }
            selectedColor[0] = existingCategory.getColor();
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            String type = rbExpense.isChecked() ? "expense" : "income";

            if (existingCategory == null) {
                DanhMuc newCat = new DanhMuc(0, name, "ic_menu_sort_by_size", type, selectedColor[0]);
                long id = categoryDAO.addCategory(newCat);
                if (id > 0) Toast.makeText(getContext(), "Đã thêm danh mục", Toast.LENGTH_SHORT).show();
            } else {
                existingCategory.setName(name);
                existingCategory.setLoai(type);
                existingCategory.setColor(selectedColor[0]);
                categoryDAO.updateCategory(existingCategory);
                Toast.makeText(getContext(), "Đã sửa danh mục", Toast.LENGTH_SHORT).show();
            }

            loadCategories();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (categoryDAO != null) {
            categoryDAO.close();
        }
    }
}
