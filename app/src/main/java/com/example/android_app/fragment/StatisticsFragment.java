package com.example.android_app.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_app.R;
import com.example.android_app.database.TransactionDAO;
import com.example.android_app.model.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    private TextView tvStartDate, tvEndDate, tvStatIncome, tvStatExpense;
    private EditText etSearchStat;
    private ImageView btnFilterStat;
    private TransactionDAO transactionDAO;
    
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionDAO = new TransactionDAO(getContext());
        transactionDAO.open();

        tvStatIncome = view.findViewById(R.id.tvStatIncome);
        tvStatExpense = view.findViewById(R.id.tvStatExpense);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        etSearchStat = view.findViewById(R.id.etSearchStat);
        btnFilterStat = view.findViewById(R.id.btnFilterStat);

        tvStartDate.setOnClickListener(v -> showDatePicker(true));
        tvEndDate.setOnClickListener(v -> showDatePicker(false));

        btnFilterStat.setOnClickListener(v -> applyFilter());

        // Load ban đầu (Tất cả)
        applyFilter();
    }

    private void showDatePicker(boolean isStart) {
        Calendar current = isStart ? startDate : endDate;
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            if (isStart) {
                startDate.set(year, month, dayOfMonth);
                tvStartDate.setText(sdf.format(startDate.getTime()));
            } else {
                endDate.set(year, month, dayOfMonth);
                tvEndDate.setText(sdf.format(endDate.getTime()));
            }
        }, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void applyFilter() {
        String keyword = etSearchStat.getText().toString();
        String start = tvStartDate.getText().toString().equals("Từ ngày") ? null : tvStartDate.getText().toString();
        String end = tvEndDate.getText().toString().equals("Đến ngày") ? null : tvEndDate.getText().toString();

        List<Transaction> list = transactionDAO.getTransactionsByFilter(keyword, start, end);
        
        double income = 0;
        double expense = 0;
        
        // Vì SQL filter chưa bắt được Date nên ta có thể kết hợp lọc bằng tay (tùy chọn)
        for (Transaction t : list) {
            if ("income".equals(t.getType())) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }
        }

        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvStatIncome.setText(fmt.format(income) + " ₫");
        tvStatExpense.setText(fmt.format(expense) + " ₫");

        Toast.makeText(getContext(), "Đã lọc " + list.size() + " giao dịch", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (transactionDAO != null) transactionDAO.close();
    }
}
