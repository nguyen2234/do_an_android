package com.example.android_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_app.R;
import com.example.android_app.model.NganSach;
import java.util.List;

public class NganSachAdapter extends RecyclerView.Adapter<NganSachAdapter.ViewHolder> {

    private Context context;
    private List<NganSach> budgetList;

    public NganSachAdapter(Context context, List<NganSach> budgetList) {
        this.context = context;
        this.budgetList = budgetList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ngan_sach, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NganSach budget = budgetList.get(position);
        
        holder.tvBudgetNameItem.setText(budget.getName());
        holder.tvBudgetDatesItem.setText(budget.getStartDate() + " - " + budget.getEndDate());
        holder.tvBudgetCategoryItem.setText("Áp dụng cho: " + budget.getCategoryIds());

        holder.tvBudgetSpentItem.setText(dinhDangTien(budget.getSpentAmount()) + " ₫");
        holder.tvBudgetAmountItem.setText(dinhDangTien(budget.getAmount()) + " ₫");

        double percentage = 0;
        if (budget.getAmount() > 0) {
            percentage = (budget.getSpentAmount() / budget.getAmount()) * 100.0;
        }
        int progress = (int) percentage;
        if (holder.tvBudgetPercentItem != null) {
            holder.tvBudgetPercentItem.setText(progress + "%");
        }
        if (progress > 100) progress = 100;
        holder.pbBudgetItem.setProgress(progress);

        // Đổi màu ProgressBar tự động theo phần trăm ngân sách đã sử dụng
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int color;
            if (percentage < 80.0) {
                color = android.graphics.Color.parseColor("#4CAF50"); // Xanh lá - An toàn
            } else if (percentage < 100.0) {
                color = android.graphics.Color.parseColor("#FFC107"); // Vàng - Cảnh báo
            } else {
                color = android.graphics.Color.parseColor("#F44336"); // Đỏ - Vượt hạn mức
            }
            holder.pbBudgetItem.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
            if (holder.tvBudgetPercentItem != null) {
                holder.tvBudgetPercentItem.setTextColor(color);
            }
        }
    }

    @Override
    public int getItemCount() {
        return budgetList != null ? budgetList.size() : 0;
    }

    private String dinhDangTien(double amount) {
        return String.format("%,.0f", amount).replace(",", ".");
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBudgetNameItem, tvBudgetDatesItem, tvBudgetSpentItem, tvBudgetAmountItem, tvBudgetCategoryItem, tvBudgetPercentItem;
        ProgressBar pbBudgetItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBudgetNameItem = itemView.findViewById(R.id.tvBudgetNameItem);
            tvBudgetDatesItem = itemView.findViewById(R.id.tvBudgetDatesItem);
            tvBudgetSpentItem = itemView.findViewById(R.id.tvBudgetSpentItem);
            tvBudgetAmountItem = itemView.findViewById(R.id.tvBudgetAmountItem);
            tvBudgetCategoryItem = itemView.findViewById(R.id.tvBudgetCategoryItem);
            tvBudgetPercentItem = itemView.findViewById(R.id.tvBudgetPercentItem);
            pbBudgetItem = itemView.findViewById(R.id.pbBudgetItem);
        }
    }
}
