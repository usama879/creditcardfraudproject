package com.frauddetect.fraudshield;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frauddetect.fraudshield.Adapters.CheckFraudAdapter;
import com.frauddetect.fraudshield.Models.SupabaseApi;
import com.frauddetect.fraudshield.Models.Transactions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FraudDetectionActivity extends AppCompatActivity {

    RecyclerView rvFraudDetection1;
    ProgressBar progressBar1;
    private CheckFraudAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fraud_detection);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        rvFraudDetection1 = findViewById(R.id.rvFraudDetection1);
        progressBar1 = findViewById(R.id.progressBar1);

        rvFraudDetection1.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CheckFraudAdapter(new ArrayList<>());
        rvFraudDetection1.setAdapter(adapter);

        String userId = getUserIdFromPrefs(this);
        if (userId != null) {
            fetchUnverifiedTransactions(userId);
        }
    }

    private String getUserIdFromPrefs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        return prefs.getString("id", null);
    }

    private void fetchUnverifiedTransactions(String userId) {
        progressBar1.setVisibility(View.VISIBLE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://muthrrjxqtvcgmbrzklx.supabase.co/rest/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SupabaseApi api = retrofit.create(SupabaseApi.class);

        api.getTransactionsByUserAndStatus("eq." + userId, "eq.Unverified")
                .enqueue(new Callback<List<Transactions>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Transactions>> call,
                                           @NonNull Response<List<Transactions>> response) {
                        progressBar1.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.updateData(response.body());
                        } else {
                            adapter.updateData(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Transactions>> call,
                                          @NonNull Throwable t) {
                        progressBar1.setVisibility(View.GONE);
                        adapter.updateData(new ArrayList<>());
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(FraudDetectionActivity.this, DashboardActivity.class));
    }
}
