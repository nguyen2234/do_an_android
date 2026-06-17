package com.example.android_app.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.R;
import com.example.android_app.adapter.GiaoDichDuKienAdapter;
import com.example.android_app.database.GiaoDichDuKienDAO;
import com.example.android_app.model.GiaoDichDuKien;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class GiaoDichDuKienFragment extends Fragment {

    private GiaoDichDuKienDAO dao;
    private GiaoDichDuKienAdapter adapter;
    private List<GiaoDichDuKien> dataList;
    private RecyclerView rv;
    private View layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_giao_dich_du_kien, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dao = new GiaoDichDuKienDAO(getContext());
        dao.open();

        rv = view.findViewById(R.id.rvDuKien);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        loadData();

        
        view.findViewById(R.id.fabThemDuKien).setOnClickListener(v -> showThemDialog());
    }

    private void loadData() {
        dataList = dao.getAll();
        adapter = new GiaoDichDuKienAdapter(getContext(), dataList);

        adapter.setOnItemActionListener(new GiaoDichDuKienAdapter.OnItemActionListener() {
            @Override
            public void onDelete(GiaoDichDuKien item, int position) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Xóa khoản")
                        .setMessage("Bạn muốn xóa khoản \"" + item.getTitle() + "\"?")
                        .setPositiveButton("Xóa", (d, w) -> {
                            dao.deletePlanned(item.getId());
                            dataList.remove(position);
                            adapter.notifyItemRemoved(position);
                            updateEmptyState();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onMarkComplete(GiaoDichDuKien item, int position) {
                if ("completed".equals(item.getStatus())) {
                    Toast.makeText(getContext(), "Khoản này đã hoàn thành rồi", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(getContext())
                        .setTitle("Đánh dấu hoàn thành")
                        .setMessage("Đánh dấu \"" + item.getTitle() + "\" là đã thực hiện?")
                        .setPositiveButton("Xác nhận", (d, w) -> {
                            dao.markCompleted(item.getId());
                            item.setStatus("completed");
                            adapter.notifyItemChanged(position);
                            Toast.makeText(getContext(), "✅ Đã đánh dấu hoàn thành!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        rv.setAdapter(adapter);
        updateEmptyState();
    }

    private void showThemDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_them_du_kien, null);

        EditText etTieu = dialogView.findViewById(R.id.etTieuDeDuKien);
        EditText etSoTien = dialogView.findViewById(R.id.etSoTienDuKien);
        EditText etNgayHan = dialogView.findViewById(R.id.etNgayHanDuKien);
        EditText etGhiChu = dialogView.findViewById(R.id.etGhiChuDuKien);
        RadioGroup rgLoai = dialogView.findViewById(R.id.rgLoaiDuKien);

        
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
        etNgayHan.setOnClickListener(v -> {
            DatePickerDialog dp = new DatePickerDialog(getContext(),
                    (picker, y, m, d) -> {
                        cal.set(y, m, d);
                        etNgayHan.setText(sdf.format(cal.getTime()));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
            dp.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = etTieu.getText().toString().trim();
            String soTienStr = etSoTien.getText().toString().trim();
            String ngayHan = etNgayHan.getText().toString().trim();

            if (title.isEmpty() || soTienStr.isEmpty() || ngayHan.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            double soTien;
            try {
                soTien = Double.parseDouble(soTienStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            String type = (rgLoai.getCheckedRadioButtonId() == R.id.rbThuDuKien) ? "income" : "expense";
            GiaoDichDuKien item = new GiaoDichDuKien(
                    0, title, soTien, "Khác", type, ngayHan,
                    "pending", etGhiChu.getText().toString().trim(), 0
            );
            long id = dao.addPlanned(item);
            if (id > 0) {
                item.setId(id);
                dataList.add(0, item);
                adapter.notifyItemInserted(0);
                rv.smoothScrollToPosition(0);
                updateEmptyState();
                Toast.makeText(getContext(), "✅ Đã thêm khoản mới!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void updateEmptyState() {
        if (dataList.isEmpty()) {
            rv.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rv.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dao != null) dao.close();
    }
}
