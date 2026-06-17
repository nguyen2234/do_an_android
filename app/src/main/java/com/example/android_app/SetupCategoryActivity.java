package com.example.android_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.adapter.DanhMucAdapter;
import com.example.android_app.database.DanhMucDAO;
import com.example.android_app.model.DanhMuc;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;


public class SetupCategoryActivity extends AppCompatActivity {

    
    
    private static final String PREF_ONBOARDING_DONE = "isOnboardingDone";

    
    private TextInputEditText etCategoryName;
    private MaterialButton btnAddCategory, btnContinue;
    private RecyclerView rvCategories;
    private TextView tvCategoryCount;

    
    private View[] colorViews;
    private final int[] colors = {
            Color.parseColor("#8B5CF6"), 
            Color.parseColor("#E74C3C"), 
            Color.parseColor("#2ECC71"), 
            Color.parseColor("#F1C40F"), 
            Color.parseColor("#3498DB")  
    };
    
    private int selectedColor;

    
    private DanhMucDAO categoryDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_category);

        
        categoryDAO = new DanhMucDAO(this);
        categoryDAO.open();

        
        selectedColor = colors[0];

        initViews();
        setupColorPicker();
        setupClickListeners();
        refreshList();  
    }

    
    private void initViews() {
        etCategoryName = findViewById(R.id.etCategoryNameSetup);
        btnAddCategory = findViewById(R.id.btnAddCategorySetup);
        btnContinue = findViewById(R.id.btnContinueSetupCategory);
        rvCategories = findViewById(R.id.rvCategoriesSetup);
        tvCategoryCount = findViewById(R.id.tvCategoryCountSetup);

        rvCategories.setLayoutManager(new LinearLayoutManager(this));
    }

    
    private void setupColorPicker() {
        colorViews = new View[]{
                findViewById(R.id.colorSetup1),
                findViewById(R.id.colorSetup2),
                findViewById(R.id.colorSetup3),
                findViewById(R.id.colorSetup4),
                findViewById(R.id.colorSetup5)
        };

        
        colorViews[0].setAlpha(1.0f); 
        for (int i = 1; i < colorViews.length; i++) {
            colorViews[i].setAlpha(0.35f);
        }

        
        for (int i = 0; i < colorViews.length; i++) {
            final int index = i;
            colorViews[i].setOnClickListener(v -> {
                selectedColor = colors[index];
                
                for (View cv : colorViews) cv.setAlpha(0.35f);
                v.setAlpha(1.0f);
            });
        }
    }

    
    private void setupClickListeners() {
        
        btnAddCategory.setOnClickListener(v -> addCategory());

        
        
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, SetupWalletActivity.class);
            startActivity(intent);
            finish();
        });
    }

    
    private void addCategory() {
        String name = etCategoryName.getText() != null
                ? etCategoryName.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        
        DanhMuc newCategory = new DanhMuc(0, name, "ic_menu_sort_by_size", "general", selectedColor);
        long id = categoryDAO.addCategory(newCategory);

        if (id > 0) {
            Toast.makeText(this, "✅ Đã thêm danh mục \"" + name + "\"", Toast.LENGTH_SHORT).show();
            etCategoryName.setText(""); 
            refreshList();              
        } else {
            Toast.makeText(this, "Lỗi khi thêm danh mục", Toast.LENGTH_SHORT).show();
        }
    }

    
    private void refreshList() {
        List<DanhMuc> list = categoryDAO.getAllCategories();
        int count = list.size();

        
        tvCategoryCount.setText(count + " danh mục");

        
        DanhMucAdapter adapter = new DanhMucAdapter(this, list, new DanhMucAdapter.OnCategoryClickListener() {
            @Override
            public void onEditClick(DanhMuc category) {
                
            }

            @Override
            public void onDeleteClick(DanhMuc category) {
                categoryDAO.deleteCategory(category.getId());
                refreshList(); 
            }
        });
        rvCategories.setAdapter(adapter);

        
        if (count >= 1) {
            
            btnContinue.setEnabled(true);
            btnContinue.setAlpha(1.0f);
        } else {
            
            btnContinue.setEnabled(false);
            btnContinue.setAlpha(0.5f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (categoryDAO != null) categoryDAO.close();
    }
}
