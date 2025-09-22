package com.frauddetect.fraudshield.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frauddetect.fraudshield.Adapters.TransactionAdapter;
import com.frauddetect.fraudshield.Models.SupabaseApi;
import com.frauddetect.fraudshield.Models.Transactions;
import com.frauddetect.fraudshield.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TransactionFragment extends Fragment {

    private TextView tvTotalTransactions, tvValidTransactions, tvFraudTransactions, tvResultCount;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        tvTotalTransactions = view.findViewById(R.id.tvTotalTransactions);
        tvValidTransactions = view.findViewById(R.id.tvValidTransactions);
        tvFraudTransactions = view.findViewById(R.id.tvFraudTransactions);
        progressBar = view.findViewById(R.id.progressBar);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        rvTransactions = view.findViewById(R.id.rvTransactions);
        tvResultCount = view.findViewById(R.id.tvResultCount);

        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new TransactionAdapter(new java.util.ArrayList<>());
        rvTransactions.setAdapter(adapter);

        String userId = getUserIdFromPrefs(requireContext());
        if (userId != null) {
            fetchTransactions(userId);
        }

        return view;

    }

    private String getUserIdFromPrefs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        return prefs.getString("id", null);
    }

    private void fetchTransactions(String userId) {
        progressBar.setVisibility(View.VISIBLE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://muthrrjxqtvcgmbrzklx.supabase.co/rest/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SupabaseApi api = retrofit.create(SupabaseApi.class);

        api.getAllTransactions("eq." + userId).enqueue(new Callback<List<Transactions>>() {
            @Override
            public void onResponse(@NonNull Call<List<Transactions>> call, @NonNull Response<List<Transactions>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Transactions> transactionList = response.body();
                    if (transactionList.isEmpty()) {
                        llEmptyState.setVisibility(View.VISIBLE);
                        rvTransactions.setVisibility(View.GONE);
                    } else {
                        llEmptyState.setVisibility(View.GONE);
                        rvTransactions.setVisibility(View.VISIBLE);

                        int total = transactionList.size();
                        int fraud = 0;
                        for (Transactions t : transactionList) {
                            if ("fraud".equalsIgnoreCase(t.getStatus())) {
                                fraud++;
                            }
                        }
                        int valid = total - fraud;

                        tvTotalTransactions.setText(String.valueOf(total));
                        tvValidTransactions.setText(String.valueOf(valid));
                        tvFraudTransactions.setText(String.valueOf(fraud));
                        tvResultCount.setText("showing " + total + " results");

                        adapter.updateData(transactionList);
                    }
                } else {
                    llEmptyState.setVisibility(View.VISIBLE);
                    rvTransactions.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Transactions>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                llEmptyState.setVisibility(View.VISIBLE);
                rvTransactions.setVisibility(View.GONE);
            }
        });
    }


}

