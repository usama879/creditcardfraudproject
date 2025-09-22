package com.frauddetect.fraudshield.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.frauddetect.fraudshield.Models.SupabaseApi;
import com.frauddetect.fraudshield.Models.User;
import com.frauddetect.fraudshield.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {

    private TextInputEditText etProfileName, etProfileEmail, etProfilePhone, etProfileAge, etProfilePassword;
    private MaterialButton btnToggleEdit, btnCancel, btnSaveChanges;
    private LinearLayout llActionButtons;

    private SupabaseApi supabaseApi;
    private ProgressDialog progressDialog;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        etProfileName = view.findViewById(R.id.etProfileName);
        etProfileEmail = view.findViewById(R.id.etProfileEmail);
        etProfilePhone = view.findViewById(R.id.etProfilePhone);
        etProfileAge = view.findViewById(R.id.etProfileAge);
        etProfilePassword = view.findViewById(R.id.etProfilePassword);

        btnToggleEdit = view.findViewById(R.id.btnToggleEdit);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        llActionButtons = view.findViewById(R.id.llActionButtons);

        initSupabase();
        fetchUserDetails();

        btnSaveChanges.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Save clicked (update logic pending)", Toast.LENGTH_SHORT).show();
            enableEditing(false);
        });

        etProfileEmail.setFocusable(false);
        etProfilePassword.setFocusable(false);

        btnToggleEdit.setOnClickListener(v -> enableEditing(true));
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableEditing(false);
                btnToggleEdit.setVisibility(View.VISIBLE);

            }
        });


        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserDetails();
                btnToggleEdit.setVisibility(View.VISIBLE);

            }
        });

        return view;
    }

    private void initSupabase() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://muthrrjxqtvcgmbrzklx.supabase.co/rest/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().build())
                .build();

        supabaseApi = retrofit.create(SupabaseApi.class);
    }

    private void fetchUserDetails() {

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", null);


        Call<List<User>> call = supabaseApi.getUserById("eq." + userId);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User user = response.body().get(0);
                    etProfileName.setText(user.getName());
                    etProfileEmail.setText(user.getEmail());
                    etProfilePhone.setText(user.getPhone());
                    etProfileAge.setText(String.valueOf(user.getAge()));
                    etProfilePassword.setText(user.getPassword());
                } else {
                    Toast.makeText(getContext(), "Failed to fetch user details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserDetails() {
        showProgress("Saving changes...");

        Map<String, String> updatedData = new HashMap<>();
        updatedData.put("name", etProfileName.getText().toString().trim());
        updatedData.put("email", etProfileEmail.getText().toString().trim());
        updatedData.put("phone", etProfilePhone.getText().toString().trim());
        updatedData.put("age", etProfileAge.getText().toString().trim());
        updatedData.put("password", etProfilePassword.getText().toString().trim());

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", null);

        Call<Void> call = supabaseApi.updateUserById("eq." + userId, updatedData);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                            Toast.makeText(getContext(), "Changes saved successfully", Toast.LENGTH_SHORT).show();
                            enableEditing(false);
                        }
                    }, 1600);
                } else {
                    hideProgress();
                    Toast.makeText(getContext(), "Failed to save changes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                hideProgress();
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void enableEditing(boolean enable) {
        btnToggleEdit.setVisibility(View.GONE);
        etProfileName.setEnabled(enable);
        etProfileEmail.setEnabled(enable);
        etProfilePhone.setEnabled(enable);
        etProfileAge.setEnabled(enable);
        etProfilePassword.setEnabled(enable);

        llActionButtons.setVisibility(enable ? View.VISIBLE : View.GONE);
    }


}
