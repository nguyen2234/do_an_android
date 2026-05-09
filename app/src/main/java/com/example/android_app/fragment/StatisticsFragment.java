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
import com.example.android_app.adapter.StatCategoryAdapter;
import com.example.android_app.database.TransactionDAO;
import com.example.android_app.model.Transaction;
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

public class StatisticsFragment extends Fragment {

    private TextView tvStartDate, tvEndDate, tvStatIncome, tvStatExpense;
    private EditText etSearchStat;
    private ImageView btnFilterStat;
    private BarChartView barChart;
    private RecyclerView rvTopCategories;
    private TransactionDAO transactionDAO;
    
    private Calendar startCal = Calendar.getInstance();
    private Calendar endCal = Calendar.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionDAO = new TransactionDAO(getContext());
        transactionDAO.open();

        initViews(view);
        setupDatePickers();

        // Mặc định lọc trong 30 ngày gần đây
        startCal.add(Calendar.DAY_OF_MONTH, -30);
        tvStartDate.setText(sdf.format(startCal.getTime()));
        tvEndDate.setText(sdf.format(endCal.getTime()));

        applyFilter();
    }

    private void initViews(View view) {
        tvStatIncome = view.findViewById(R.id.tvStatIncome);
        tvStatExpense = view.findViewById(R.id.tvStatExpense);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        etSearchStat = view.findViewById(R.id.etSearchStat);
        btnFilterStat = view.findViewById(R.id.btnFilterStat);
        barChart = view.findViewById(R.id.barChart);
        rvTopCategories = view.findViewById(R.id.rvTopCategories);

        rvTopCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        btnFilterStat.setOnClickListener(v -> applyFilter());
    }

    private void setupDatePickers() {
        tvStartDate.setOnClickListener(v -> showDatePicker(true));
        tvEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStart) {
        Calendar current = isStart ? startCal : endCal;
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            if (isStart) {
                startCal.set(year, month, dayOfMonth);
                tvStartDate.setText(sdf.format(startCal.getTime()));
            } else {
                endCal.set(year, month, dayOfMonth);
                tvEndDate.setText(sdf.format(endCal.getTime()));
            }
            applyFilter();
        }, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void applyFilter() {
        String keyword = etSearchStat.getText().toString();
        List<Transaction> allTransactions = transactionDAO.getTransactionsByFilter(keyword, null, null);
        
        List<Transaction> filteredList = new ArrayList<>();
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

        for (Transaction t : allTransactions) {
            try {
                Date tDate = sdf.parse(t.getDate());
                if (tDate != null) {
                    long transTime = tDate.getTime();
                    
                    if (transTime >= startTime && transTime <= endTime) {
                        filteredList.add(t);
                        if ("income".equalsIgnoreCase(t.getType())) {
                            totalIncome += t.getAmount();
                        } else {
                            totalExpense += t.getAmount();
                            String cat = t.getCategory() != null ? t.getCategory() : "Khác";
                            categoryMap.put(cat, categoryMap.getOrDefault(cat, 0.0) + t.getAmount());
                        }
                    }
                }
            } catch (ParseException e) {
                Log.e("StatError", "Lỗi định dạng ngày: " + t.getDate() + ". Mong đợi dd/MM/yyyy");
            }
        }

        updateSummaryUI(totalIncome, totalExpense);
        updateChartData(filteredList);
        updateCategoryList(categoryMap);
    }

    private void updateSummaryUI(double income, double expense) {
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvStatIncome.setText(fmt.format(income) + " ₫");
        tvStatExpense.setText(fmt.format(expense) + " ₫");
    }

    private void updateChartData(List<Transaction> list) {
        Map<String, Float> dailyData = new TreeMap<>();
        
        Calendar cal = (Calendar) endCal.clone();
        cal.add(Calendar.DAY_OF_MONTH, -6);
        for (int i = 0; i < 7; i++) {
            dailyData.put(sdf.format(cal.getTime()), 0f);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (Transaction t : list) {
            if (!"income".equalsIgnoreCase(t.getType()) && dailyData.containsKey(t.getDate())) {
                dailyData.put(t.getDate(), dailyData.get(t.getDate()) + (float) t.getAmount());
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

        barChart.setData(values, labels);
    }

    private void updateCategoryList(Map<String, Double> categoryMap) {
        List<Map.Entry<String, Double>> sortedCategories = new ArrayList<>(categoryMap.entrySet());
        Collections.sort(sortedCategories, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        
        StatCategoryAdapter adapter = new StatCategoryAdapter(sortedCategories);
        rvTopCategories.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (transactionDAO != null) transactionDAO.close();
    }
}
