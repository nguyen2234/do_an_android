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
import com.google.android.material.card.MaterialCardView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ViTienAdapter extends RecyclerView.Adapter<ViTienAdapter.ViewHolder> {

    private final Context context;
    private final List<ViTien> wallets;
    private OnEditClickListener editListener;
    private OnDeleteClickListener deleteListener;
    private OnItemClickListener itemClickListener;
    
    private long selectedId = -1;
    private boolean isSelectionMode = false;

    public interface OnEditClickListener { void onEditClick(ViTien wallet); }
    public interface OnDeleteClickListener { void onDeleteClick(ViTien wallet); }
    public interface OnItemClickListener { void onItemClick(ViTien wallet); }

    private boolean isHorizontal = false;

    public ViTienAdapter(Context context, List<ViTien> wallets) {
        this.context = context;
        this.wallets = wallets;
    }

    public ViTienAdapter(Context context, List<ViTien> wallets, boolean isHorizontal) {
        this.context = context;
        this.wallets = wallets;
        this.isHorizontal = isHorizontal;
    }

    public void setOnEditClickListener(OnEditClickListener listener) { this.editListener = listener; }
    public void setOnDeleteClickListener(OnDeleteClickListener listener) { this.deleteListener = listener; }
    public void setOnItemClickListener(OnItemClickListener listener) { 
        this.itemClickListener = listener; 
        this.isSelectionMode = (listener != null);
    }

    public void setSelectedId(long id) {
        this.selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isHorizontal ? R.layout.item_vi_tien_horizontal : R.layout.item_vi_tien;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ViTien w = wallets.get(position);
        
        holder.tvName.setText(w.getName());
        if (holder.tvType != null) {
            holder.tvType.setText(getTypeName(w.getIcon()));
        }

        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvBalance.setText(fmt.format(w.getBalance()) + " ₫");

        // Giao diện khi chọn ví trong màn hình chuyển tiền
        if (isSelectionMode) {
            holder.ivEdit.setVisibility(View.GONE);
            holder.ivDelete.setVisibility(View.GONE);
            if (w.getId() == selectedId) {
                holder.cardView.setStrokeWidth(4);
                holder.cardView.setStrokeColor(context.getColor(R.color.colorPrimary));
                holder.cardView.setCardElevation(12f);
            } else {
                holder.cardView.setStrokeWidth(0);
                holder.cardView.setCardElevation(2f);
            }
        } else {
            holder.ivEdit.setVisibility(View.VISIBLE);
            holder.ivDelete.setVisibility(View.VISIBLE);
            holder.cardView.setStrokeWidth(0);
        }

        // Icon mapping - Using system icons since ic_cash, ic_bank, ic_savings are missing
        int iconRes = android.R.drawable.ic_menu_agenda;
        if ("bank".equals(w.getIcon())) {
            iconRes = android.R.drawable.ic_menu_myplaces;
        } else if ("saving".equals(w.getIcon())) {
            iconRes = android.R.drawable.ic_menu_save;
        }
        holder.ivIcon.setImageResource(iconRes);

        // Color & Background
        try {
            int colorInt = Color.parseColor(w.getColor());
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(Color.argb(30, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt)));
            holder.ivIcon.setBackground(shape);
            holder.ivIcon.setColorFilter(colorInt);
        } catch (Exception e) {
            holder.ivIcon.setColorFilter(context.getColor(R.color.colorPrimary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                selectedId = w.getId();
                itemClickListener.onItemClick(w);
                notifyDataSetChanged();
            }
        });

        holder.ivEdit.setOnClickListener(v -> { if (editListener != null) editListener.onEditClick(w); });
        holder.ivDelete.setOnClickListener(v -> { if (deleteListener != null) deleteListener.onDeleteClick(w); });
    }

    private String getTypeName(String type) {
        if ("bank".equals(type)) return "Ngân hàng";
        if ("saving".equals(type)) return "Tiết kiệm";
        return "Tiền mặt";
    }

    @Override
    public int getItemCount() { return wallets.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivEdit, ivDelete;
        TextView tvName, tvType, tvBalance;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            ivIcon = itemView.findViewById(R.id.ivWalletIcon);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvName = itemView.findViewById(R.id.tvWalletName);
            tvType = itemView.findViewById(R.id.tvWalletType);
            tvBalance = itemView.findViewById(R.id.tvWalletBalance);
        }
    }
}
