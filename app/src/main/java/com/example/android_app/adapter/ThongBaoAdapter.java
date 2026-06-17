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
import com.example.android_app.model.ThongBao;
import java.util.List;


public class ThongBaoAdapter extends RecyclerView.Adapter<ThongBaoAdapter.ViewHolder> {

    private Context context;
    private List<ThongBao> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ThongBao thongBao);
    }

    public ThongBaoAdapter(Context context, List<ThongBao> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void updateData(List<ThongBao> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_thong_bao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThongBao tb = list.get(position);
        if (tb == null) return;

        holder.tvTitle.setText(tb.getTitle());
        holder.tvContent.setText(tb.getContent());
        holder.tvDate.setText(tb.getDate());

        
        if (tb.isRead()) {
            holder.viewUnreadDot.setVisibility(View.GONE);
            holder.tvTitle.setAlpha(0.7f);
            holder.tvContent.setAlpha(0.7f);
        } else {
            holder.viewUnreadDot.setVisibility(View.VISIBLE);
            holder.tvTitle.setAlpha(1.0f);
            holder.tvContent.setAlpha(1.0f);
        }

        
        String type = tb.getType();
        if ("warning".equalsIgnoreCase(type)) {
            holder.imgIcon.setImageResource(R.drawable.ic_info);
            holder.imgIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorExpense));
            holder.viewIconBg.setBackgroundResource(R.drawable.bg_badge_red);
            holder.viewIconBg.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorExpenseLight));
        } else if ("transaction".equalsIgnoreCase(type)) {
            
            if (tb.getTitle().contains("Nạp")) {
                holder.imgIcon.setImageResource(R.drawable.ic_topup_modern);
            } else if (tb.getTitle().contains("Chi")) {
                holder.imgIcon.setImageResource(R.drawable.ic_expense_modern);
            } else {
                holder.imgIcon.setImageResource(R.drawable.ic_transfer_modern);
            }
            holder.imgIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.viewIconBg.setBackgroundResource(R.drawable.bg_badge_red);
            holder.viewIconBg.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorIncomeLight));
        } else if ("reminder".equalsIgnoreCase(type)) {
            holder.imgIcon.setImageResource(R.drawable.ic_calendar);
            holder.imgIcon.setColorFilter(Color.parseColor("#3B82F6")); 
            holder.viewIconBg.setBackgroundResource(R.drawable.bg_badge_red);
            holder.viewIconBg.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorIncomeLight)); 
        } else {
            
            holder.imgIcon.setImageResource(R.drawable.ic_notification);
            holder.imgIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorTextSecondary));
            holder.viewIconBg.setBackgroundResource(R.drawable.bg_badge_red);
            holder.viewIconBg.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorBackground));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(tb);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        View viewIconBg;
        TextView tvTitle, tvContent, tvDate;
        View viewUnreadDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgNotifIcon);
            viewIconBg = itemView.findViewById(R.id.viewIconBg);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvContent = itemView.findViewById(R.id.tvNotifContent);
            tvDate = itemView.findViewById(R.id.tvNotifDate);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}
