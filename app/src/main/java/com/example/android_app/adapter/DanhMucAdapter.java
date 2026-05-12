package com.example.android_app.adapter;

import android.content.Context;
import android.graphics.Color;
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
        holder.tvCategoryType.setText("income".equals(category.getLoai()) ? "Thu nhập" : "Chi tiêu");
        
        if (category.getNote() != null && !category.getNote().isEmpty()) {
            holder.tvCategoryNote.setVisibility(View.VISIBLE);
            holder.tvCategoryNote.setText(category.getNote());
        } else {
            holder.tvCategoryNote.setVisibility(View.GONE);
        }

        // Set color
        try {
            holder.vCategoryColor.setBackgroundColor(category.getColor());
        } catch (Exception e) {
            holder.vCategoryColor.setBackgroundColor(Color.parseColor("#4CAF50")); // default
        }

        // Set icon (Simple mapping based on string for now, could be dynamic)
        int iconRes = getIconResource(category.getIcon());
        holder.ivCategoryIcon.setImageResource(iconRes);

        // Click listeners
        holder.btnEditCategory.setOnClickListener(v -> listener.onEditClick(category));
        holder.btnDeleteCategory.setOnClickListener(v -> listener.onDeleteClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    public void updateData(List<DanhMuc> newList) {
        this.categoryList = newList;
        notifyDataSetChanged();
    }

    private int getIconResource(String iconName) {
        if (iconName == null) return android.R.drawable.ic_menu_agenda;
        
        switch (iconName) {
            case "food": return android.R.drawable.ic_menu_directions;
            case "shopping": return android.R.drawable.ic_menu_myplaces;
            case "transport": return android.R.drawable.ic_menu_mapmode;
            case "health": return android.R.drawable.ic_menu_add;
            case "salary": return android.R.drawable.ic_menu_sort_by_size;
            case "entertainment": return android.R.drawable.ic_menu_camera;
            default: return android.R.drawable.ic_menu_agenda;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View vCategoryColor;
        ImageView ivCategoryIcon, btnEditCategory, btnDeleteCategory;
        TextView tvCategoryName, tvCategoryType, tvCategoryNote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vCategoryColor = itemView.findViewById(R.id.vCategoryColor);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryType = itemView.findViewById(R.id.tvCategoryType);
            tvCategoryNote = itemView.findViewById(R.id.tvCategoryNote);
            btnEditCategory = itemView.findViewById(R.id.btnEditCategory);
            btnDeleteCategory = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
