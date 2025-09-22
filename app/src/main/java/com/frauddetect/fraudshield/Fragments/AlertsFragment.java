package com.frauddetect.fraudshield.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.frauddetect.fraudshield.Adapters.AlertsAdapter;
import com.frauddetect.fraudshield.Adapters.RecentAlertsAdapter;
import com.frauddetect.fraudshield.Models.ApiClient;
import com.frauddetect.fraudshield.Models.SupabaseApi;
import com.frauddetect.fraudshield.Models.Transactions;
import com.frauddetect.fraudshield.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AlertsFragment extends Fragment {

    RecyclerView alerts_recyclerview;
    AlertsAdapter recentAlertsAdapter;
    ProgressBar alert_progress;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts, container, false);

        alerts_recyclerview = view.findViewById(R.id.alerts_recyclerview);
        alert_progress = view.findViewById(R.id.alert_progress);

        alerts_recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        recentAlertsAdapter = new AlertsAdapter(getContext(), new ArrayList<>());
        alerts_recyclerview.setAdapter(recentAlertsAdapter);

        loadAlerts();

        return view;
    }

    private void loadAlerts() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", null);
        if (userId == null) return;

        SupabaseApi api = ApiClient.getClient().create(SupabaseApi.class);

        Call<List<Transactions>> fraudCall = api.getTransactionsByUserAndStatus("eq." + userId, "eq.fraud");
        Call<List<Transactions>> unverifiedCall = api.getTransactionsByUserAndStatus("eq." + userId, "eq.Unverified");

        fraudCall.enqueue(new Callback<List<Transactions>>() {
            @Override
            public void onResponse(@NonNull Call<List<Transactions>> call, @NonNull Response<List<Transactions>> fraudResponse) {
                List<Transactions> fraudList = fraudResponse.isSuccessful() && fraudResponse.body() != null
                        ? fraudResponse.body() : new ArrayList<>();

                unverifiedCall.enqueue(new Callback<List<Transactions>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Transactions>> call, @NonNull Response<List<Transactions>> unverifiedResponse) {
                        List<Transactions> unverifiedList = unverifiedResponse.isSuccessful() && unverifiedResponse.body() != null
                                ? unverifiedResponse.body() : new ArrayList<>();

                        List<Transactions> alertList = new ArrayList<>();
                        alertList.addAll(fraudList);
                        alertList.addAll(unverifiedList);

                        Collections.sort(alertList, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));

                        recentAlertsAdapter = new AlertsAdapter(getContext(), alertList);
                        alerts_recyclerview.setAdapter(recentAlertsAdapter);

                        alert_progress.setVisibility(View.GONE);
                        alerts_recyclerview.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Transactions>> call, @NonNull Throwable t) {
                        Log.e("HomeFragment", "Error loading unverified transactions: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<Transactions>> call, @NonNull Throwable t) {
                Log.e("HomeFragment", "Error loading fraud transactions: " + t.getMessage());
            }
        });
    }

}