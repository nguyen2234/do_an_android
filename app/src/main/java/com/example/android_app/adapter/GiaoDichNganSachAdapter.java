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

/**
 * Adapter để hiển thị danh sách giao dịch (GiaoDich) bên trong màn hình Chi tiết Ngân sách.
 *
 * Điểm khác biệt so với GiaoDichAdapter:
 * - Nhận thêm map walletNames để dịch walletId -> tên ví
 * - Luôn hiển thị màu đỏ (expense) vì ngân sách chỉ track chi tiêu
 * - Layout sử dụng item_giao_dich_ngan_sach.xml (có thêm dòng tên ví)
 */
public class GiaoDichNganSachAdapter extends RecyclerView.Adapter<GiaoDichNganSachAdapter.ViewHolder> {

    private final Context context;
    private final List<GiaoDich> transactionList;
    /**
     * Map chứa ánh xạ walletId -> walletName.
     * Được nạp trước từ ViTienDAO để tránh truy vấn DB trong quá trình bind view.
     */
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

        // -- Tiêu đề giao dịch --
        holder.tvTitle.setText(t.getTitle() != null ? t.getTitle() : "Giao dịch");

        // -- Tên danh mục --
        holder.tvCategory.setText(t.getCategory() != null ? t.getCategory() : "Khác");

        // -- Tên ví: tra cứu từ map, fallback về "Không rõ" nếu không tìm thấy --
        String walletName = walletNames.get(t.getWalletId());
        holder.tvWallet.setText(walletName != null ? walletName : "Không rõ");

        // -- Ngày giao dịch --
        holder.tvDate.setText(t.getNgay() != null ? t.getNgay() : "");

        // -- Số tiền: luôn âm (chi tiêu) và màu đỏ --
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvAmount.setText("-" + fmt.format(t.getSoTien()) + " ₫");
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    /**
     * ViewHolder giữ tham chiếu tới các View con trong layout item_giao_dich_ngan_sach.xml
     */
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
