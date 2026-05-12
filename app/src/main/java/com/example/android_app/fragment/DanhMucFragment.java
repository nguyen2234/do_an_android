package com.example.android_app.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class DanhMucFragment extends Fragment {

    private RecyclerView rvCategories;
    private View layoutEmpty;
    private FloatingActionButton fabAddCategory;
    private TabLayout tabLayout;
    
    private DanhMucAdapter adapter;
    private DanhMucDAO categoryDAO;
    private List<DanhMuc> fullCategoryList;
    
    private String currentTypeFilter = "expense"; // "expense" for Chi tiêu, "income" for Thu nhập

    // Data arrays
    private final String[] iconValues = {"food", "shopping", "transport", "health", "salary", "entertainment", "other"};
    private final int[] iconDrawables = {
        android.R.drawable.ic_menu_directions,
        android.R.drawable.ic_menu_myplaces,
        android.R.drawable.ic_menu_mapmode,
        android.R.drawable.ic_menu_add,
        android.R.drawable.ic_menu_sort_by_size,
        android.R.drawable.ic_menu_camera,
        android.R.drawable.ic_menu_agenda
    };
    
    private final int[] colorValues = {
        Color.parseColor("#4CAF50"), 
        Color.parseColor("#F44336"), 
        Color.parseColor("#2196F3"), 
        Color.parseColor("#FFEB3B"), 
        Color.parseColor("#FF9800"), 
        Color.parseColor("#9C27B0"),
        Color.parseColor("#9E9E9E"),
        Color.parseColor("#00BCD4"),
        Color.parseColor("#E91E63")
    };

    private String selectedIcon = "other";
    private int selectedColor = Color.parseColor("#9E9E9E");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_danh_muc, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCategories = view.findViewById(R.id.rvCategories);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        fabAddCategory = view.findViewById(R.id.fabAddCategory);
        tabLayout = view.findViewById(R.id.tabLayout);

        categoryDAO = new DanhMucDAO(getContext());
        categoryDAO.open();

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTypeFilter = tab.getPosition() == 0 ? "expense" : "income";
                updateListUI();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadCategories();

        fabAddCategory.setOnClickListener(v -> showCategoryDialog(null));
    }

    private void loadCategories() {
        fullCategoryList = categoryDAO.getAllCategories();
        updateListUI();
    }

    private void updateListUI() {
        if (fullCategoryList == null) return;

        List<DanhMuc> filteredList = new ArrayList<>();
        for (DanhMuc c : fullCategoryList) {
            if (currentTypeFilter.equals(c.getLoai())) {
                filteredList.add(c);
            }
        }

        if (filteredList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvCategories.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvCategories.setVisibility(View.VISIBLE);
            
            if (adapter == null) {
                adapter = new DanhMucAdapter(getContext(), filteredList, new DanhMucAdapter.OnCategoryClickListener() {
                    @Override
                    public void onEditClick(DanhMuc category) {
                        showCategoryDialog(category);
                    }

                    @Override
                    public void onDeleteClick(DanhMuc category) {
                        showDeleteConfirmDialog(category);
                    }
                });
                rvCategories.setAdapter(adapter);
            } else {
                adapter.updateData(filteredList);
            }
        }
    }

    private void showCategoryDialog(DanhMuc categoryToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_danh_muc, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Init views
        android.widget.TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        EditText etName = view.findViewById(R.id.etCategoryName);
        EditText etNote = view.findViewById(R.id.etCategoryNote);
        RadioGroup rgType = view.findViewById(R.id.rgCategoryType);
        RadioButton rbExpense = view.findViewById(R.id.rbExpense);
        RadioButton rbIncome = view.findViewById(R.id.rbIncome);
        LinearLayout layoutIcons = view.findViewById(R.id.layoutCategoryIcons);
        LinearLayout layoutColors = view.findViewById(R.id.layoutCategoryColors);
        Button btnCancel = view.findViewById(R.id.btnCancelDialog);
        Button btnSave = view.findViewById(R.id.btnSaveCategory);

        // Default selections
        selectedIcon = categoryToEdit != null ? categoryToEdit.getIcon() : "other";
        selectedColor = categoryToEdit != null ? categoryToEdit.getColor() : colorValues[0];
        
        setupIconPicker(layoutIcons);
        setupColorPicker(layoutColors);

        if (categoryToEdit != null) {
            tvTitle.setText("Sửa danh mục");
            etName.setText(categoryToEdit.getName());
            if (categoryToEdit.getNote() != null) {
                etNote.setText(categoryToEdit.getNote());
            }
            if ("income".equals(categoryToEdit.getLoai())) {
                rbIncome.setChecked(true);
            } else {
                rbExpense.setChecked(true);
            }
        } else {
            // Default to current tab
            if ("income".equals(currentTypeFilter)) {
                rbIncome.setChecked(true);
            } else {
                rbExpense.setChecked(true);
            }
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String note = etNote.getText().toString().trim();
            String type = rbIncome.isChecked() ? "income" : "expense";

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            if (categoryToEdit == null) {
                // Add new
                DanhMuc newCategory = new DanhMuc();
                newCategory.setName(name);
                newCategory.setNote(note);
                newCategory.setLoai(type);
                newCategory.setIcon(selectedIcon);
                newCategory.setColor(selectedColor);
                
                long result = categoryDAO.addCategory(newCategory);
                if (result > 0) {
                    Toast.makeText(getContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                    loadCategories();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi thêm danh mục", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Update
                categoryToEdit.setName(name);
                categoryToEdit.setNote(note);
                categoryToEdit.setLoai(type);
                categoryToEdit.setIcon(selectedIcon);
                categoryToEdit.setColor(selectedColor);
                
                int result = categoryDAO.updateCategory(categoryToEdit);
                if (result > 0) {
                    Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    loadCategories();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi cập nhật danh mục", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void setupIconPicker(LinearLayout layoutIcons) {
        layoutIcons.removeAllViews();
        int size = dpToPx(48);
        int margin = dpToPx(8);
        int padding = dpToPx(12);

        for (int i = 0; i < iconValues.length; i++) {
            final String iconVal = iconValues[i];
            ImageView iv = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(0, 0, margin, 0);
            iv.setLayoutParams(params);
            iv.setPadding(padding, padding, padding, padding);
            iv.setImageResource(iconDrawables[i]);

            if (iconVal.equals(selectedIcon)) {
                iv.setBackgroundResource(R.drawable.bg_input); // Highlight selected
                iv.setColorFilter(Color.parseColor("#4CAF50")); // Highlight color
            } else {
                iv.setBackgroundResource(android.R.color.transparent);
                iv.setColorFilter(Color.parseColor("#757575"));
            }

            iv.setOnClickListener(v -> {
                selectedIcon = iconVal;
                setupIconPicker(layoutIcons); // Refresh
            });

            layoutIcons.addView(iv);
        }
    }

    private void setupColorPicker(LinearLayout layoutColors) {
        layoutColors.removeAllViews();
        int size = dpToPx(40);
        int margin = dpToPx(8);

        for (int i = 0; i < colorValues.length; i++) {
            final int colorVal = colorValues[i];
            View colorView = new View(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(0, 0, margin, 0);
            colorView.setLayoutParams(params);

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(colorVal);

            if (colorVal == selectedColor) {
                shape.setStroke(dpToPx(3), Color.BLACK); // Highlight selected
            } else {
                shape.setStroke(0, Color.TRANSPARENT);
            }
            
            colorView.setBackground(shape);

            colorView.setOnClickListener(v -> {
                selectedColor = colorVal;
                setupColorPicker(layoutColors); // Refresh
            });

            layoutColors.addView(colorView);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void showDeleteConfirmDialog(DanhMuc category) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục '" + category.getName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int result = categoryDAO.deleteCategory(category.getId());
                    if (result > 0) {
                        Toast.makeText(getContext(), "Đã xóa danh mục", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (categoryDAO != null) {
            categoryDAO.close();
        }
    }
}
