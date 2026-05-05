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
import com.example.android_app.model.Wallet;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter dùng để hiển thị danh sách các Ví tiền (Wallet) lên RecyclerView.
 */
public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> {

    private final Context context;
    private final List<Wallet> wallets; // Danh sách dữ liệu ví
    
    private OnEditClickListener editListener;
    private OnDeleteClickListener deleteListener;

    public interface OnEditClickListener {
        void onEditClick(Wallet wallet);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Wallet wallet);
    }

    public WalletAdapter(Context context, List<Wallet> wallets) {
        this.context = context;
        this.wallets = wallets;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    /**
     * Tạo giao diện cho một dòng ví.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wallet, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Gắn dữ liệu của một ví cụ thể vào giao diện dòng.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wallet w = wallets.get(position);
        
        // Hiển thị tên ví và loại ví
        holder.tvName.setText(w.getName());
        holder.tvType.setText(getTypeName(w.getIcon())); // Dùng icon làm type

        // Định dạng số dư ví
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvBalance.setText(fmt.format(w.getBalance()) + " ₫");

        // Chọn icon tùy theo wallet.getIcon()
        String icon = w.getIcon() != null ? w.getIcon() : "cash";
        switch (icon) {
            case "bank":
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_send);
                break;
            case "saving":
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_save);
                break;
            default: // cash
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_myplaces);
                break;
        }

        // Tạo hình nền bo góc cho icon với màu sắc tương ứng wallet.getColor()
        String colorStr = w.getColor() != null ? w.getColor() : "#4CAF50";
        try {
            int colorInt = Color.parseColor(colorStr);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            // Pha thêm màu trắng để làm nền sáng hơn (ví dụ: alpha 30%)
            int bgColor = Color.argb(40, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt));
            shape.setColor(bgColor);
            holder.ivIcon.setBackground(shape);
            holder.ivIcon.setColorFilter(colorInt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Bắt sự kiện click sửa
        holder.ivEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onEditClick(w);
        });

        // Bắt sự kiện click xóa
        holder.ivDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(w);
        });
    }

    /**
     * Dịch loại ví từ tiếng Anh sang tiếng Việt để hiển thị lên giao diện
     */
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

    /**
     * Lớp ánh xạ các thành phần UI của dòng.
     */
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

