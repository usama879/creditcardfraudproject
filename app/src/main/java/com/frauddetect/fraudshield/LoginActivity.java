package com.frauddetect.fraudshield;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.frauddetect.fraudshield.Models.ApiClient;
import com.frauddetect.fraudshield.Models.SupabaseApi;
import com.frauddetect.fraudshield.Models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    TextView tvSignUp, tvForgotPassword;
    EditText etPassword, etEmail;
    ImageView ivTogglePassword;
    Button btnLogin;
    boolean[] isPasswordVisible = {false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Activity started");
        setContentView(R.layout.activity_login);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        tvSignUp = findViewById(R.id.textRegister);
        tvForgotPassword = findViewById(R.id.textForgotPassword);
        etPassword = findViewById(R.id.editTextPassword);
        etEmail = findViewById(R.id.editTextEmail);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        btnLogin = findViewById(R.id.buttonLogin);

        ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible[0]) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
                Log.d(TAG, "onClick: Password hidden");
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_on);
                Log.d(TAG, "onClick: Password visible");
            }
            etPassword.setSelection(etPassword.length());
            isPasswordVisible[0] = !isPasswordVisible[0];
        });

        tvSignUp.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Sign up clicked, navigating to RegisterActivity");
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            Log.d(TAG, "onClick: Login button clicked with email = " + email);

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick: Validation failed - empty fields");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email");
                Log.d(TAG, "onClick: Validation failed - invalid email");
                return;
            }

            loginUser(email, password);
        });
    }

    private void loginUser(String email, String password) {
        Log.d(TAG, "loginUser: Starting login for email = " + email);
        SupabaseApi api = ApiClient.getClient().create(SupabaseApi.class);

        Call<List<User>> call = api.checkUserByEmail("eq." + email);

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                Log.d(TAG, "loginUser: API response code = " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    Log.d(TAG, "loginUser: Users returned = " + users.size());

                    if (users.isEmpty()) {
                        Toast.makeText(LoginActivity.this, "Account not found. Please register.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "loginUser: No account found for email = " + email);
                    } else {
                        User user = users.get(0);
                        if (user.getPassword().equals(password)) {
                            Log.d(TAG, "loginUser: Login successful for user ID = " + user.getId());
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            saveUserIdToPrefs(user.getId());
                        } else {
                            Toast.makeText(LoginActivity.this, "Incorrect credentials", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "loginUser: Incorrect password for email = " + email);
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Email not valid", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "loginUser: API response unsuccessful or empty body");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "loginUser: API call failed", t);
            }
        });
    }

    private void saveUserIdToPrefs(String userId) {
        getSharedPreferences("user_pref", MODE_PRIVATE)
                .edit()
                .putString("id", userId)
                .apply();
        Log.d(TAG, "saveUserIdToPrefs: User ID saved = " + userId);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed: Exiting app to home");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
