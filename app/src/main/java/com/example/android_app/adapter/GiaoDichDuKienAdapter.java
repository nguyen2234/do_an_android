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
import com.example.android_app.model.GiaoDichDuKien;

import java.util.List;


public class GiaoDichDuKienAdapter extends RecyclerView.Adapter<GiaoDichDuKienAdapter.ViewHolder> {

    public interface OnItemActionListener {
        void onDelete(GiaoDichDuKien item, int position);
        void onMarkComplete(GiaoDichDuKien item, int position);
    }

    private final Context context;
    private final List<GiaoDichDuKien> data;
    private OnItemActionListener listener;

    public GiaoDichDuKienAdapter(Context context, List<GiaoDichDuKien> data) {
        this.context = context;
        this.data = data;
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_du_kien, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GiaoDichDuKien item = data.get(position);

        holder.tvTieuDe.setText(item.getTitle());
        holder.tvNgayHan.setText("Đến hạn: " + item.getDueDate());
        holder.tvDanhMuc.setText(item.getCategory() != null ? item.getCategory() : "");

        
        String amountStr = String.format("%,.0f", item.getAmount()).replace(",", ".") + " ₫";
        if (item.isExpense()) {
            holder.tvSoTien.setText("- " + amountStr);
            holder.tvSoTien.setTextColor(Color.parseColor("#E74C3C"));
            holder.viewBadge.setBackgroundColor(Color.parseColor("#E74C3C"));
        } else {
            holder.tvSoTien.setText("+ " + amountStr);
            holder.tvSoTien.setTextColor(Color.parseColor("#27AE60"));
            holder.viewBadge.setBackgroundColor(Color.parseColor("#27AE60"));
        }

        
        if ("completed".equals(item.getStatus())) {
            holder.tvTrangThai.setText("✅ Xong");
            holder.tvTrangThai.setTextColor(Color.WHITE);
            holder.tvTrangThai.setBackgroundColor(Color.parseColor("#27AE60"));
        } else {
            holder.tvTrangThai.setText("⏳ Chờ");
            holder.tvTrangThai.setTextColor(Color.WHITE);
            holder.tvTrangThai.setBackgroundColor(Color.parseColor("#F39C12"));
        }

        
        holder.btnXoa.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item, holder.getAdapterPosition());
        });

        
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onMarkComplete(item, holder.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewBadge;
        TextView tvTieuDe, tvNgayHan, tvDanhMuc, tvSoTien, tvTrangThai;
        ImageView btnXoa;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewBadge = itemView.findViewById(R.id.viewTypeBadge);
            tvTieuDe = itemView.findViewById(R.id.tvTieuDe);
            tvNgayHan = itemView.findViewById(R.id.tvNgayHan);
            tvDanhMuc = itemView.findViewById(R.id.tvDanhMuc);
            tvSoTien = itemView.findViewById(R.id.tvSoTien);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            btnXoa = itemView.findViewById(R.id.btnXoa);
        }
    }
}
