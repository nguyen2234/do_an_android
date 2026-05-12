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

/**
 * Adapter dùng để hiển thị danh sách các Giao dịch (GiaoDich) lên RecyclerView.
 * Kế thừa từ RecyclerView.Adapter
 */
public class GiaoDichAdapter extends RecyclerView.Adapter<GiaoDichAdapter.ViewHolder> {

    private final Context context;
    private final List<GiaoDich> transactions; // Danh sách dữ liệu giao dịch
    private OnItemClickListener listener;

    // Interface để xử lý sự kiện click vào một giao dịch trong danh sách
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

    /**
     * Tạo ra view (giao diện) cho từng dòng trong danh sách.
     * Hàm này chỉ gọi khi RecyclerView cần tạo mới một dòng.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp layout item_giao_dich.xml thành View
        View view = LayoutInflater.from(context).inflate(R.layout.item_giao_dich, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Gắn dữ liệu của giao dịch vào các thành phần giao diện của dòng tương ứng.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Lấy giao dịch tại vị trí hiện tại
        GiaoDich t = transactions.get(position);

        // Hiển thị tiêu đề, ngày, danh mục
        holder.tvTitle.setText(t.getTitle());
        holder.tvNgayThang.setText(t.getNgay());
        holder.tvDanhMuc.setText(t.getCategory());

        // Định dạng số tiền sang chuẩn Việt Nam (VD: 1.000.000 ₫)
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        String amountValue = fmt.format(Math.abs(t.getSoTien()));
        String amountFormatted;

        // Kiểm tra loại giao dịch là chi tiêu (Expense) hay thu nhập (Income)
        if (t.isExpense()) {
            amountFormatted = context.getString(R.string.format_currency_expense, amountValue);
            holder.tvAmount.setText(amountFormatted);
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.colorExpense));
            holder.ivIcon.setImageResource(android.R.drawable.arrow_down_float);
            holder.ivIcon.setBackgroundResource(R.drawable.bg_icon_expense);
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorExpense));
        } else {
            amountFormatted = context.getString(R.string.format_currency_income, amountValue);
            holder.tvAmount.setText(amountFormatted);
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.colorIncome));
            holder.ivIcon.setImageResource(android.R.drawable.arrow_up_float);
            holder.ivIcon.setBackgroundResource(R.drawable.bg_icon_income);
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorIncome));
        }

        // Bắt sự kiện click vào dòng hiện tại
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(t);
        });
    }

    /**
     * Trả về tổng số lượng giao dịch có trong danh sách.
     */
    @Override
    public int getItemCount() {
        return transactions.size();
    }

    /**
     * Lớp ViewHolder giúp giữ tham chiếu tới các thành phần giao diện (TextView, ImageView)
     * của một dòng để tái sử dụng, giúp tăng hiệu năng cho RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvNgayThang, tvAmount, tvDanhMuc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ id từ XML sang Java
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvNgayThang = itemView.findViewById(R.id.tvTransactionDate);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvDanhMuc = itemView.findViewById(R.id.tvTransactionCategory);
        }
    }
}
