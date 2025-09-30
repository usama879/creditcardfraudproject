package com.frauddetect.fraudshield;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.frauddetect.fraudshield.Models.ApiClient;
import com.frauddetect.fraudshield.Models.SupabaseApi;
import com.frauddetect.fraudshield.Models.Transactions;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsAnalyticsActivity extends AppCompatActivity {

    private PieChart pieChart;
    private LineChart lineChart;
    private BarChart barChart;
    private TextView tvTotalTransactions, tvTotalAmount, tvAvgTransaction;
    private ProgressBar progressBar;
    private static final String TAG = "ReportsAnalytics";
    MaterialToolbar reportAnalyticsToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_analytics);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        reportAnalyticsToolbar = findViewById(R.id.reportAnalyticsToolbar);

        reportAnalyticsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StatisticsAnalyticsActivity.this, DashboardActivity.class);
                startActivity(intent);

            }
        });

        initViews();

        String userId = getCurrentUserId();
        if (userId != null) {
            showLoading(true);
            fetchTransactionsAndSetupCharts(userId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        pieChart = findViewById(R.id.pieChart);
        lineChart = findViewById(R.id.lineChart);
        barChart = findViewById(R.id.barChart);
        tvTotalTransactions = findViewById(R.id.tvTotalTransactions);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvAvgTransaction = findViewById(R.id.tvAvgTransaction);
        progressBar = findViewById(R.id.reportProgressBar);

    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? ProgressBar.VISIBLE : ProgressBar.GONE);
    }

    private String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        return prefs.getString("id", null);
    }

    private void fetchTransactionsAndSetupCharts(String userId) {
        SupabaseApi api = ApiClient.getClient().create(SupabaseApi.class);
        Call<List<Transactions>> call = api.getAllTransactions("eq." + userId);

        call.enqueue(new Callback<List<Transactions>>() {
            @Override
            public void onResponse(Call<List<Transactions>> call, Response<List<Transactions>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Transactions> transactions = response.body();
                    updateOverviewStats(transactions);
                    setupPieChart(transactions);
                    setupMonthlyLineChart(transactions);
                    setupTransactionStatusBarChart(transactions);
                } else {
                    Toast.makeText(StatisticsAnalyticsActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Transactions>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(StatisticsAnalyticsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API Failure", t);
            }
        });
    }

    private void updateOverviewStats(List<Transactions> transactions) {
        int totalCount = transactions.size();
        double totalAmount = 0.0;

        for (Transactions t : transactions) {
            totalAmount += parseDoubleSafe(t.getAmount());
        }

        double avgAmount = totalCount > 0 ? totalAmount / totalCount : 0.0;

        tvTotalTransactions.setText(String.valueOf(totalCount));
        tvTotalAmount.setText(formatCurrency(totalAmount));
        tvAvgTransaction.setText(formatCurrency(avgAmount));
    }

    private String formatCurrency(double amount) {
        if (amount >= 1_000_000_000) {
            return "$" + String.format("%.2fB", amount / 1_000_000_000);
        } else if (amount >= 1_000_000) {
            return "$" + String.format("%.2fM", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return "$" + String.format("%.2fK", amount / 1_000);
        } else {
            return "$" + String.format("%.2f", amount);
        }
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void setupPieChart(List<Transactions> transactions) {
        HashMap<String, Float> categorySums = new HashMap<>();
        String[] categories = {
                "Shopping (POS)", "Shopping (NET)", "Food & Dining", "Misc (POS)",
                "Misc (NET)", "Home", "Kids & Pets", "Personal Care"
        };

        for (String cat : categories) categorySums.put(cat, 0f);

        for (Transactions t : transactions) {
            float amt = parseFloatSafe(t.getAmount());
            if ("1".equals(t.getCategory_shopping_pos())) categorySums.put(categories[0], categorySums.get(categories[0]) + amt);
            if ("1".equals(t.getCategory_shopping_net())) categorySums.put(categories[1], categorySums.get(categories[1]) + amt);
            if ("1".equals(t.getCategory_food_dining())) categorySums.put(categories[2], categorySums.get(categories[2]) + amt);
            if ("1".equals(t.getCategory_misc_pos())) categorySums.put(categories[3], categorySums.get(categories[3]) + amt);
            if ("1".equals(t.getCategory_misc_net())) categorySums.put(categories[4], categorySums.get(categories[4]) + amt);
            if ("1".equals(t.getCategory_home())) categorySums.put(categories[5], categorySums.get(categories[5]) + amt);
            if ("1".equals(t.getCategory_kids_pets())) categorySums.put(categories[6], categorySums.get(categories[6]) + amt);
            if ("1".equals(t.getCategory_personal_care())) categorySums.put(categories[7], categorySums.get(categories[7]) + amt);
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (String cat : categories) {
            float total = categorySums.get(cat);
            if (total > 0) {
                entries.add(new PieEntry(total, cat));
            }
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(1f, "No Data"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getPieChartColors());
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatCurrency(value);
            }
        });

        pieChart.setData(data);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setDrawEntryLabels(true);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(10f);

        pieChart.invalidate();
    }

    private ArrayList<Integer> getPieChartColors() {
        return new ArrayList<>(Arrays.asList(
                ContextCompat.getColor(this, R.color.deep_blue),
                ContextCompat.getColor(this, R.color.lighter_blue),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#F44336"),
                Color.parseColor("#00BCD4"),
                Color.parseColor("#795548")
        ));
    }

    private void setupMonthlyLineChart(List<Transactions> transactions) {
        SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfMonthYear = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        HashMap<String, Float> monthSums = new HashMap<>();
        for (Transactions t : transactions) {
            float amt = parseFloatSafe(t.getAmount());
            try {
                Date date = sdfInput.parse(t.getDate());
                String monthYear = sdfMonthYear.format(date);
                monthSums.put(monthYear, monthSums.getOrDefault(monthYear, 0f) + amt);
            } catch (ParseException e) {
                Log.e(TAG, "Date parsing error", e);
            }
        }

        ArrayList<String> labels = new ArrayList<>(monthSums.keySet());
        labels.sort((a, b) -> {
            try {
                Date dateA = sdfMonthYear.parse(a);
                Date dateB = sdfMonthYear.parse(b);
                return dateA.compareTo(dateB);
            } catch (ParseException e) {
                return 0;
            }
        });

        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            entries.add(new Entry(i, monthSums.get(labels.get(i))));
        }

        if (entries.isEmpty()) {
            entries.add(new Entry(0, 0));
            labels.add("No Data");
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "Monthly Spending");
        lineDataSet.setColor(ContextCompat.getColor(this, R.color.deep_blue));
        lineDataSet.setCircleColor(ContextCompat.getColor(this, R.color.lighter_blue));
        lineDataSet.setLineWidth(3f);
        lineDataSet.setCircleRadius(5f);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueTextColor(ContextCompat.getColor(this, R.color.text_primary));
        lineDataSet.setFillColor(ContextCompat.getColor(this, R.color.primary_container));
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillAlpha(50);

        LineData lineData = new LineData(lineDataSet);
        lineData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatCurrency(value);
            }
        });

        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                return (index >= 0 && index < labels.size()) ? labels.get(index) : "";
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
        leftAxis.setGranularity(1f);
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatCurrency(value);
            }
        });

        lineChart.getDescription().setEnabled(false);
        lineChart.setGridBackgroundColor(Color.TRANSPARENT);
        lineChart.invalidate();
    }

    private void setupTransactionStatusBarChart(List<Transactions> transactions) {
        int fraudCount = 0;
        int notFraudCount = 0;
        int unverifiedCount = 0;

        for (Transactions t : transactions) {
            String status = t.getStatus() != null ? t.getStatus().trim().toLowerCase() : "unverified";
            if (status.equals("fraud")) fraudCount++;
            else if (status.equals("not_fraud")) notFraudCount++;
            else unverifiedCount++;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, fraudCount));
        entries.add(new BarEntry(1, notFraudCount));
        entries.add(new BarEntry(2, unverifiedCount));

        BarDataSet dataSet = new BarDataSet(entries, "Transactions by Status");
        dataSet.setColors(new int[]{
                Color.parseColor("#F44336"),
                Color.parseColor("#4CAF50"),
                ContextCompat.getColor(this, R.color.lighter_blue)
        });
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(R.color.deep_blue);
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChart.setData(data);

        String[] labels = {"Fraud", "Not Fraud", "Unverified"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                return (index >= 0 && index < labels.length) ? labels[index] : "";
            }
        });
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        YAxis leftAxis = barChart.getAxisLeft();
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);
        leftAxis.setGranularity(1f);
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        leftAxis.setAxisMinimum(0f);

        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.invalidate();
    }

    private float parseFloatSafe(String val) {
        try {
            return Float.parseFloat(val);
        } catch (Exception e) {
            return 0f;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(StatisticsAnalyticsActivity.this, DashboardActivity.class);
        startActivity(intent);

    }
}