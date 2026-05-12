package com.example.android_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ThongKeDanhMucAdapter extends RecyclerView.Adapter<ThongKeDanhMucAdapter.StatCategoryViewHolder> {

    private List<Map.Entry<String, Double>> categoryList;
    private double maxAmount = 0;

    public ThongKeDanhMucAdapter(List<Map.Entry<String, Double>> categoryList) {
        this.categoryList = categoryList;
        for (Map.Entry<String, Double> entry : categoryList) {
            if (entry.getValue() > maxAmount) {
                maxAmount = entry.getValue();
            }
        }
    }

    @NonNull
    @Override
    public StatCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thong_ke_danh_muc, parent, false);
        return new StatCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatCategoryViewHolder holder, int position) {
        Map.Entry<String, Double> item = categoryList.get(position);
        holder.tvStatCatName.setText(item.getKey());
        
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvStatCatAmount.setText(fmt.format(item.getValue()) + " ₫");

        if (maxAmount > 0) {
            int progress = (int) ((item.getValue() / maxAmount) * 100);
            holder.pbStatCat.setProgress(progress);
        } else {
            holder.pbStatCat.setProgress(0);
        }
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    static class StatCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatCatName;
        TextView tvStatCatAmount;
        ProgressBar pbStatCat;

        public StatCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatCatName = itemView.findViewById(R.id.tvStatCatName);
            tvStatCatAmount = itemView.findViewById(R.id.tvStatCatAmount);
            pbStatCat = itemView.findViewById(R.id.pbStatCat);
        }
    }
}
