package com.frauddetect.fraudshield.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.frauddetect.fraudshield.AddTransactionActivity;
import com.frauddetect.fraudshield.Models.ApiClient;
import com.frauddetect.fraudshield.Models.SupabaseApi;
import com.frauddetect.fraudshield.Models.User;
import com.frauddetect.fraudshield.R;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    TextView tvGreeting, tvUsername;
    CardView card_add_transaction;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvUsername = view.findViewById(R.id.tv_username);
        card_add_transaction = view.findViewById(R.id.card_add_transaction);

        card_add_transaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddTransactionActivity.class);
                startActivity(intent);
            }
        });

        setGreetingMessage();
        loadUsernameFromSupabase();

        return view;
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

        if (userId == null) {
            Toast.makeText(requireContext(), "User not found in preferences", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        SupabaseApi api = retrofit.create(SupabaseApi.class);

        Call<List<User>> call = api.getUserById("eq." + userId);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
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
                Log.e("HomeFragment", "Error: " + t.getMessage(), t);
                String cachedName = sharedPreferences.getString("name", "User");
                tvUsername.setText(cachedName);
            }
        });
    }
}
