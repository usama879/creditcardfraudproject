package com.frauddetect.fraudshield.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.frauddetect.fraudshield.Adapters.RecentAlertsAdapter;
import com.frauddetect.fraudshield.AddTransactionActivity;
import com.frauddetect.fraudshield.DashboardActivity;
import com.frauddetect.fraudshield.FraudDetectionActivity;
import com.frauddetect.fraudshield.LoginActivity;
import com.frauddetect.fraudshield.Models.ApiClient;
import com.frauddetect.fraudshield.Models.SupabaseApi;
import com.frauddetect.fraudshield.Models.Transactions;
import com.frauddetect.fraudshield.Models.User;
import com.frauddetect.fraudshield.R;
import com.frauddetect.fraudshield.StatisticsAnalyticsActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeFragment extends Fragment {

    TextView tvGreeting, tvUsername, tv_alert_count, tv_no_alerts;
    CardView card_add_transaction, card_view_transactions, card_fraud_detection, card_reports_analytics,
            logout_user, card_alerts_summary, viewAlertsBtn, alert_count_badge;
    RecyclerView rv_recent_alerts;
    RecentAlertsAdapter recentAlertsAdapter;
    ProgressBar recent_alert_progress;

    private LinearLayout empty_alerts_layout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvUsername = view.findViewById(R.id.tv_username);
        card_add_transaction = view.findViewById(R.id.card_add_transaction);
        card_view_transactions = view.findViewById(R.id.card_view_transactions);
        card_fraud_detection = view.findViewById(R.id.card_fraud_detection);
        card_reports_analytics = view.findViewById(R.id.card_reports_analytics);
        logout_user = view.findViewById(R.id.logout_user);
        card_alerts_summary = view.findViewById(R.id.card_alerts_summary);
        tv_alert_count = view.findViewById(R.id.tv_alert_count);
        rv_recent_alerts = view.findViewById(R.id.rv_recent_alerts);
        viewAlertsBtn = view.findViewById(R.id.viewAlertsBtn);
        recent_alert_progress = view.findViewById(R.id.recent_alert_progress);


        empty_alerts_layout = view.findViewById(R.id.empty_alerts_layout);
        alert_count_badge = view.findViewById(R.id.alert_count_badge);
        viewAlertsBtn = view.findViewById(R.id.viewAlertsBtn);
        recent_alert_progress = view.findViewById(R.id.recent_alert_progress);
        rv_recent_alerts = view.findViewById(R.id.rv_recent_alerts);
        tv_alert_count = view.findViewById(R.id.tv_alert_count);

        rv_recent_alerts.setLayoutManager(new LinearLayoutManager(getContext()));
        recentAlertsAdapter = new RecentAlertsAdapter(getContext(), new ArrayList<>());
        rv_recent_alerts.setAdapter(recentAlertsAdapter);

        loadAlerts();

        logout_user.setOnClickListener(v -> {
            try {
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE);
                sharedPreferences.edit().clear().apply();

                Toast.makeText(getContext(), "Logout", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            } catch (Exception e) {
                Log.e("HomeFragment", "Logout error: ", e);
            }
        });

        viewAlertsBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(getContext(), DashboardActivity.class);
                intent.putExtra("openFragment", "alerts");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("HomeFragment", "View Alerts error: ", e);
            }
        });

        card_reports_analytics.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(getContext(), StatisticsAnalyticsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("HomeFragment", "Reports Analytics error: ", e);
            }
        });

        card_fraud_detection.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(getContext(), FraudDetectionActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("HomeFragment", "Fraud Detection error: ", e);
            }
        });

        card_view_transactions.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(getContext(), DashboardActivity.class);
                intent.putExtra("openFragment", "transactions");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("HomeFragment", "View Transactions error: ", e);
            }
        });

        card_add_transaction.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(getContext(), AddTransactionActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("HomeFragment", "Add Transaction error: ", e);
            }
        });

        setGreetingMessage();
        loadUsernameFromSupabase();

        return view;
    }

    private void loadAlerts() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", null);
        if (userId == null) return;

        recent_alert_progress.setVisibility(View.VISIBLE);
        empty_alerts_layout.setVisibility(View.GONE);
        rv_recent_alerts.setVisibility(View.GONE);
        viewAlertsBtn.setVisibility(View.GONE);
        alert_count_badge.setVisibility(View.GONE);

        SupabaseApi api = ApiClient.getClient().create(SupabaseApi.class);

        Call<List<Transactions>> fraudCall = api.getTransactionsByUserAndStatus("eq." + userId, "eq.fraud");
        Call<List<Transactions>> unverifiedCall = api.getTransactionsByUserAndStatus("eq." + userId, "eq.Unverified");

        fraudCall.enqueue(new Callback<List<Transactions>>() {
            @Override
            public void onResponse(@NonNull Call<List<Transactions>> call, @NonNull Response<List<Transactions>> fraudResponse) {
                if (!isAdded() || getContext() == null) return;

                List<Transactions> fraudList = fraudResponse.isSuccessful() && fraudResponse.body() != null
                        ? fraudResponse.body() : new ArrayList<>();

                unverifiedCall.enqueue(new Callback<List<Transactions>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Transactions>> call, @NonNull Response<List<Transactions>> unverifiedResponse) {
                        if (!isAdded() || getContext() == null) return;

                        List<Transactions> unverifiedList = unverifiedResponse.isSuccessful() && unverifiedResponse.body() != null
                                ? unverifiedResponse.body() : new ArrayList<>();

                        List<Transactions> alertList = new ArrayList<>();
                        alertList.addAll(fraudList);
                        alertList.addAll(unverifiedList);

                        recent_alert_progress.setVisibility(View.GONE);

                        if (alertList.isEmpty()) {
                            empty_alerts_layout.setVisibility(View.VISIBLE);
                            rv_recent_alerts.setVisibility(View.GONE);
                            viewAlertsBtn.setVisibility(View.GONE);
                            alert_count_badge.setVisibility(View.GONE);
                        } else {
                            empty_alerts_layout.setVisibility(View.GONE);
                            rv_recent_alerts.setVisibility(View.VISIBLE);
                            viewAlertsBtn.setVisibility(View.VISIBLE);
                            alert_count_badge.setVisibility(View.VISIBLE);

                            tv_alert_count.setText(String.valueOf(alertList.size()));
                            Collections.sort(alertList, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));
                            if (alertList.size() > 2) {
                                alertList = alertList.subList(0, 2);
                            }
                            recentAlertsAdapter = new RecentAlertsAdapter(getContext(), alertList);
                            rv_recent_alerts.setAdapter(recentAlertsAdapter);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Transactions>> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        recent_alert_progress.setVisibility(View.GONE);
                        empty_alerts_layout.setVisibility(View.VISIBLE);
                        Log.e("HomeFragment", "Error loading unverified transactions: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<Transactions>> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                recent_alert_progress.setVisibility(View.GONE);
                empty_alerts_layout.setVisibility(View.VISIBLE);
                Log.e("HomeFragment", "Error loading fraud transactions: " + t.getMessage());
            }
        });
    }

    private void setGreetingMessage() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning, ";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Good Afternoon, ";
        } else {
            greeting = "Good Evening, ";
        }

        tvGreeting.setText(greeting);
    }

    private void loadUsernameFromSupabase() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", null);
        if (userId == null) return;

        Retrofit retrofit = ApiClient.getClient();
        SupabaseApi api = retrofit.create(SupabaseApi.class);

        Call<List<User>> call = api.getUserById("eq." + userId);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User user = response.body().get(0);
                    tvUsername.setText(user.getName());
                    sharedPreferences.edit().putString("name", user.getName()).apply();
                } else {
                    Log.e("HomeFragment", "No user found for id: " + userId);
                    String cachedName = sharedPreferences.getString("name", "User");
                    tvUsername.setText(cachedName);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Log.e("HomeFragment", "Error: " + t.getMessage(), t);
                String cachedName = sharedPreferences.getString("name", "User");
                tvUsername.setText(cachedName);
            }
        });
    }
}
