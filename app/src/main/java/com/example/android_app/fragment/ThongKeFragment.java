package com.example.android_app.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.R;
import com.example.android_app.adapter.ThongKeDanhMucAdapter;
import com.example.android_app.database.GiaoDichDAO;
import com.example.android_app.model.GiaoDich;
import com.example.android_app.view.BarChartView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ThongKeFragment extends Fragment {

    private TextView tvNgayBatDau, tvNgayKetThuc, tvThongKeThuNhap, tvThongKeChiTieu;
    private EditText etTimKiemThongKe;
    private ImageView btnLocThongKe;
    private BarChartView bieuDoCot;
    private RecyclerView rvTopDanhMuc;
    private GiaoDichDAO transactionDAO;
    
    private Calendar startCal = Calendar.getInstance();
    private Calendar endCal = Calendar.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thong_ke, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionDAO = new GiaoDichDAO(getContext());
        transactionDAO.open();

        khoiTaoGiaoDien(view);
        caiDatChonNgay();

        // Mặc định lọc trong 30 ngày gần đây
        startCal.add(Calendar.DAY_OF_MONTH, -30);
        tvNgayBatDau.setText(sdf.format(startCal.getTime()));
        tvNgayKetThuc.setText(sdf.format(endCal.getTime()));

        apDungBoLoc();
    }

    private void khoiTaoGiaoDien(View view) {
        tvThongKeThuNhap = view.findViewById(R.id.tvThongKeThuNhap);
        tvThongKeChiTieu = view.findViewById(R.id.tvThongKeChiTieu);
        tvNgayBatDau = view.findViewById(R.id.tvNgayBatDau);
        tvNgayKetThuc = view.findViewById(R.id.tvNgayKetThuc);
        etTimKiemThongKe = view.findViewById(R.id.etTimKiemThongKe);
        btnLocThongKe = view.findViewById(R.id.btnLocThongKe);
        bieuDoCot = view.findViewById(R.id.bieuDoCot);
        rvTopDanhMuc = view.findViewById(R.id.rvTopDanhMuc);

        rvTopDanhMuc.setLayoutManager(new LinearLayoutManager(getContext()));
        btnLocThongKe.setOnClickListener(v -> apDungBoLoc());
    }

    private void caiDatChonNgay() {
        tvNgayBatDau.setOnClickListener(v -> showDatePicker(true));
        tvNgayKetThuc.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStart) {
        Calendar current = isStart ? startCal : endCal;
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            if (isStart) {
                startCal.set(year, month, dayOfMonth);
                tvNgayBatDau.setText(sdf.format(startCal.getTime()));
            } else {
                endCal.set(year, month, dayOfMonth);
                tvNgayKetThuc.setText(sdf.format(endCal.getTime()));
            }
            apDungBoLoc();
        }, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void apDungBoLoc() {
        String keyword = etTimKiemThongKe.getText().toString();
        List<GiaoDich> allTransactions = transactionDAO.getTransactionsByFilter(keyword, null, null);
        
        List<GiaoDich> filteredList = new ArrayList<>();
        double totalIncome = 0;
        double totalExpense = 0;
        Map<String, Double> categoryMap = new HashMap<>();

        // Chuẩn hóa thời gian lọc (Bắt đầu lúc 00:00:00 và kết thúc lúc 23:59:59)
        Calendar startLimit = (Calendar) startCal.clone();
        startLimit.set(Calendar.HOUR_OF_DAY, 0);
        startLimit.set(Calendar.MINUTE, 0);
        startLimit.set(Calendar.SECOND, 0);
        startLimit.set(Calendar.MILLISECOND, 0);

        Calendar endLimit = (Calendar) endCal.clone();
        endLimit.set(Calendar.HOUR_OF_DAY, 23);
        endLimit.set(Calendar.MINUTE, 59);
        endLimit.set(Calendar.SECOND, 59);
        endLimit.set(Calendar.MILLISECOND, 999);

        long startTime = startLimit.getTimeInMillis();
        long endTime = endLimit.getTimeInMillis();

        for (GiaoDich t : allTransactions) {
            try {
                Date tDate = sdf.parse(t.getNgay());
                if (tDate != null) {
                    long transTime = tDate.getTime();
                    
                    if (transTime >= startTime && transTime <= endTime) {
                        filteredList.add(t);
                        if ("income".equalsIgnoreCase(t.getLoai())) {
                            totalIncome += t.getSoTien();
                        } else {
                            totalExpense += t.getSoTien();
                            String cat = t.getCategory() != null ? t.getCategory() : "Khác";
                            categoryMap.put(cat, categoryMap.getOrDefault(cat, 0.0) + t.getSoTien());
                        }
                    }
                }
            } catch (ParseException e) {
                Log.e("StatError", "Lỗi định dạng ngày: " + t.getNgay() + ". Mong đợi dd/MM/yyyy");
            }
        }

        capNhatGiaoDienTongQuan(totalIncome, totalExpense);
        updateChartData(filteredList);
        updateCategoryList(categoryMap);
    }

    private void capNhatGiaoDienTongQuan(double income, double expense) {
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvThongKeThuNhap.setText(fmt.format(income) + " ₫");
        tvThongKeChiTieu.setText(fmt.format(expense) + " ₫");
    }

    private void updateChartData(List<GiaoDich> list) {
        Map<String, Float> dailyData = new TreeMap<>();
        
        Calendar cal = (Calendar) endCal.clone();
        cal.add(Calendar.DAY_OF_MONTH, -6);
        for (int i = 0; i < 7; i++) {
            dailyData.put(sdf.format(cal.getTime()), 0f);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (GiaoDich t : list) {
            if (!"income".equalsIgnoreCase(t.getLoai()) && dailyData.containsKey(t.getNgay())) {
                dailyData.put(t.getNgay(), dailyData.get(t.getNgay()) + (float) t.getSoTien());
            }
        }

        float[] values = new float[7];
        String[] labels = new String[7];
        int idx = 0;
        for (Map.Entry<String, Float> entry : dailyData.entrySet()) {
            values[idx] = entry.getValue();
            labels[idx] = entry.getKey().substring(0, 5);
            idx++;
        }

        bieuDoCot.setData(values, labels);
    }

    private void updateCategoryList(Map<String, Double> categoryMap) {
        List<Map.Entry<String, Double>> sortedCategories = new ArrayList<>(categoryMap.entrySet());
        Collections.sort(sortedCategories, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        
        ThongKeDanhMucAdapter adapter = new ThongKeDanhMucAdapter(sortedCategories);
        rvTopDanhMuc.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (transactionDAO != null) transactionDAO.close();
    }
}
