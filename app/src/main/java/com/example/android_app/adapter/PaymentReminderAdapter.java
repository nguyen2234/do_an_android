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
import com.example.android_app.model.Reminder;
import com.example.android_app.model.ReminderStatus;
import com.google.android.material.button.MaterialButton;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Adapter hiển thị danh sách nhắc hẹn thanh toán.
 */
public class PaymentReminderAdapter extends RecyclerView.Adapter<PaymentReminderAdapter.ViewHolder> {

    public interface OnItemActionListener {
        void onDelete(Reminder item, int position);
        void onEdit(Reminder item, int position);
        void onPay(Reminder item, int position);
    }

    private final Context context;
    private final List<Reminder> data;
    private OnItemActionListener listener;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PaymentReminderAdapter(Context context, List<Reminder> data) {
        this.context = context;
        this.data = data;
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder item = data.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvAmount.setText(dinhDangTien(item.getEstimatedAmount()) + " ₫");
        
        if (item.getDueDate() != null) {
            holder.tvDueDate.setText("⏰ Hạn: " + item.getDueDate().format(dtf));
        } else {
            holder.tvDueDate.setText("⏰ Hạn: Chưa rõ");
        }

        if (item.getCategory() != null) {
            holder.tvRecurrence.setText("Danh mục: " + item.getCategory());
        } else {
            holder.tvRecurrence.setText("Danh mục: Khác");
        }

        // Thiết lập trạng thái hiển thị
        if (item.getStatus() == ReminderStatus.PAID) {
            holder.tvStatus.setText("Đã trả");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_red); // Reuse or tint green
            holder.tvStatus.setBackgroundColor(Color.parseColor("#27AE60")); // Green
            holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#27AE60"));
            holder.btnPay.setVisibility(View.GONE); // Đã trả thì ẩn nút thanh toán
        } else {
            holder.tvStatus.setText("Chờ trả");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#F39C12")); // Orange
            holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#F39C12"));
            holder.btnPay.setVisibility(View.VISIBLE);
        }

        if (item.getReminderOffsetDays() > 0) {
            holder.tvOffsetWarning.setText("Nhắc trước " + item.getReminderOffsetDays() + " ngày");
            holder.tvOffsetWarning.setVisibility(View.VISIBLE);
        } else {
            holder.tvOffsetWarning.setVisibility(View.GONE);
        }

        // Event click nút sửa
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item, holder.getAdapterPosition());
        });

        // Event click nút xoá
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item, holder.getAdapterPosition());
        });

        // Event click nút thanh toán
        holder.btnPay.setOnClickListener(v -> {
            if (listener != null) listener.onPay(item, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private String dinhDangTien(double amount) {
        return String.format("%,.0f", amount).replace(",", ".");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewStatusIndicator;
        TextView tvTitle, tvAmount, tvDueDate, tvRecurrence, tvStatus, tvOffsetWarning;
        ImageView btnEdit, btnDelete;
        MaterialButton btnPay;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewStatusIndicator = itemView.findViewById(R.id.viewStatusIndicator);
            tvTitle = itemView.findViewById(R.id.tvReminderTitle);
            tvAmount = itemView.findViewById(R.id.tvReminderAmount);
            tvDueDate = itemView.findViewById(R.id.tvReminderDueDate);
            tvRecurrence = itemView.findViewById(R.id.tvReminderRecurrence);
            tvStatus = itemView.findViewById(R.id.tvReminderStatus);
            tvOffsetWarning = itemView.findViewById(R.id.tvOffsetDaysWarning);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnPay = itemView.findViewById(R.id.btnPay);
        }
    }
}
