package com.example.android_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.R;
import com.example.android_app.model.GiaoDich;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class GiaoDichNganSachAdapter extends RecyclerView.Adapter<GiaoDichNganSachAdapter.ViewHolder> {

    private final Context context;
    private final List<GiaoDich> transactionList;
    
    private final Map<Long, String> walletNames;

    public GiaoDichNganSachAdapter(Context context, List<GiaoDich> transactionList, Map<Long, String> walletNames) {
        this.context = context;
        this.transactionList = transactionList;
        this.walletNames = walletNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_giao_dich_ngan_sach, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GiaoDich t = transactionList.get(position);

        
        holder.tvTitle.setText(t.getTitle() != null ? t.getTitle() : "Giao dịch");

        
        holder.tvCategory.setText(t.getCategory() != null ? t.getCategory() : "Khác");

        
        String walletName = walletNames.get(t.getWalletId());
        holder.tvWallet.setText(walletName != null ? walletName : "Không rõ");

        
        holder.tvDate.setText(t.getNgay() != null ? t.getNgay() : "");

        
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvAmount.setText("-" + fmt.format(t.getSoTien()) + " ₫");
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvCategory, tvWallet, tvDate, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivBudgetTransIcon);
            tvTitle = itemView.findViewById(R.id.tvBudgetTransTitle);
            tvCategory = itemView.findViewById(R.id.tvBudgetTransCategory);
            tvWallet = itemView.findViewById(R.id.tvBudgetTransWallet);
            tvDate = itemView.findViewById(R.id.tvBudgetTransDate);
            tvAmount = itemView.findViewById(R.id.tvBudgetTransAmount);
        }
    }
}
