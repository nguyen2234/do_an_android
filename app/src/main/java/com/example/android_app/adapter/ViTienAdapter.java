package com.example.android_app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_app.R;
import com.example.android_app.model.ViTien;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;


public class ViTienAdapter extends RecyclerView.Adapter<ViTienAdapter.ViewHolder> {

    private final Context context;
    private final List<ViTien> wallets; 
    
    private OnEditClickListener editListener;
    private OnDeleteClickListener deleteListener;

    public interface OnEditClickListener {
        void onEditClick(ViTien wallet);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(ViTien wallet);
    }

    public ViTienAdapter(Context context, List<ViTien> wallets) {
        this.context = context;
        this.wallets = wallets;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vi_tien, parent, false);
        return new ViewHolder(view);
    }

    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ViTien w = wallets.get(position);
        
        
        holder.tvName.setText(w.getName());
        holder.tvType.setText(getTypeName(w.getIcon())); 

        
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvBalance.setText(fmt.format(w.getBalance()) + " ₫");

        
        String icon = w.getIcon() != null ? w.getIcon() : "cash";
        switch (icon) {
            case "bank":
                holder.ivIcon.setImageResource(R.drawable.ic_bank_modern);
                break;
            case "saving":
                holder.ivIcon.setImageResource(R.drawable.ic_piggy_bank);
                break;
            default: 
                holder.ivIcon.setImageResource(R.drawable.ic_wallet_modern);
                break;
        }

        
        String colorStr = w.getColor() != null ? w.getColor() : "#4CAF50";
        try {
            int colorInt = Color.parseColor(colorStr);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            
            int bgColor = Color.argb(40, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt));
            shape.setColor(bgColor);
            holder.ivIcon.setBackground(shape);
            holder.ivIcon.setColorFilter(colorInt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        holder.ivEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onEditClick(w);
        });

        
        holder.ivDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(w);
        });
    }

    
    private String getTypeName(String type) {
        if (type == null) return "Tiền mặt";
        switch (type) {
            case "bank": return "Ngân hàng";
            case "saving": return "Tiết kiệm";
            default: return "Tiền mặt";
        }
    }

    @Override
    public int getItemCount() { return wallets.size(); }

    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivEdit, ivDelete;
        TextView tvName, tvType, tvBalance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivWalletIcon);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvName = itemView.findViewById(R.id.tvWalletName);
            tvType = itemView.findViewById(R.id.tvWalletType);
            tvBalance = itemView.findViewById(R.id.tvWalletBalance);
        }
    }
}

