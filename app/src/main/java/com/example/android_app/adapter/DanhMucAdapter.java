package com.example.android_app.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_app.R;
import com.example.android_app.model.DanhMuc;
import java.util.List;

public class DanhMucAdapter extends RecyclerView.Adapter<DanhMucAdapter.ViewHolder> {

    private Context context;
    private List<DanhMuc> categoryList;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onEditClick(DanhMuc category);
        void onDeleteClick(DanhMuc category);
    }

    public DanhMucAdapter(Context context, List<DanhMuc> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_danh_muc, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DanhMuc category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getName());
        holder.tvCategoryType.setText(category.getLoai().equals("expense") ? "Chi tiêu" : "Thu nhập");

        if (category.getColor() != 0) {
            holder.ivCategoryIcon.setBackgroundTintList(ColorStateList.valueOf(category.getColor()));
        }

        holder.btnEditCategory.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(category);
        });

        holder.btnDeleteCategory.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvCategoryType;
        ImageView ivCategoryIcon, btnEditCategory, btnDeleteCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryType = itemView.findViewById(R.id.tvCategoryType);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            btnEditCategory = itemView.findViewById(R.id.btnEditCategory);
            btnDeleteCategory = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
