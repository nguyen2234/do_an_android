package com.example.android_app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_app.R;
import com.example.android_app.model.GiaoDich;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;


public class GiaoDichAdapter extends RecyclerView.Adapter<GiaoDichAdapter.ViewHolder> {

    private final Context context;
    private final List<GiaoDich> transactions; 
    private OnItemClickListener listener;

    
    public interface OnItemClickListener {
        void onItemClick(GiaoDich transaction);
    }

    public GiaoDichAdapter(Context context, List<GiaoDich> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        
        View view = LayoutInflater.from(context).inflate(R.layout.item_giao_dich, parent, false);
        return new ViewHolder(view);
    }

    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        
        GiaoDich t = transactions.get(position);

        
        holder.tvTitle.setText(t.getTitle());
        holder.tvNgayThang.setText(t.getNgay());
        holder.tvDanhMuc.setText(t.getCategory());

        
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        String amountValue = fmt.format(Math.abs(t.getSoTien()));
        String amountFormatted;

        
        if (t.isExpense()) {
            amountFormatted = context.getString(R.string.format_currency_expense, amountValue);
            holder.tvAmount.setText(amountFormatted);
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.colorExpense));
            holder.ivIcon.setImageResource(R.drawable.ic_expense_modern);
            holder.ivIcon.setBackgroundResource(R.drawable.bg_icon_expense);
            holder.ivIcon.setColorFilter(null);
        } else {
            amountFormatted = context.getString(R.string.format_currency_income, amountValue);
            holder.tvAmount.setText(amountFormatted);
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.colorIncome));
            holder.ivIcon.setImageResource(R.drawable.ic_income_modern);
            holder.ivIcon.setBackgroundResource(R.drawable.bg_icon_income);
            holder.ivIcon.setColorFilter(null);
        }

        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(t);
        });
    }

    
    @Override
    public int getItemCount() {
        return transactions.size();
    }

    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvNgayThang, tvAmount, tvDanhMuc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvNgayThang = itemView.findViewById(R.id.tvTransactionDate);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvDanhMuc = itemView.findViewById(R.id.tvTransactionCategory);
        }
    }
}
