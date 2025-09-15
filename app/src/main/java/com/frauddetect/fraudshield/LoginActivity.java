package com.frauddetect.fraudshield;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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

    TextView tvSignUp, tvForgotPassword;
    EditText etPassword, etEmail;
    ImageView ivTogglePassword;
    Button btnLogin;
    boolean[] isPasswordVisible = {false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_on);
            }
            etPassword.setSelection(etPassword.length());
            isPasswordVisible[0] = !isPasswordVisible[0];
        });


        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email");
                return;
            }

            loginUser(email, password);
        });
    }
    private void loginUser(String email, String password) {
        SupabaseApi api = ApiClient.getClient().create(SupabaseApi.class);

        Call<List<User>> call = api.checkUserByEmail("eq." + email);

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();

                    if (users.isEmpty()) {
                        Toast.makeText(LoginActivity.this, "Account not found. Please register.", Toast.LENGTH_SHORT).show();
                    } else {
                        User user = users.get(0);
                        if (user.getPassword().equals(password)) {
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            saveUserIdToPrefs(user.getId());
                        } else {
                            Toast.makeText(LoginActivity.this, "Incorrect credentials", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Email not valid", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void saveUserIdToPrefs(String userId) {
        getSharedPreferences("user_pref", MODE_PRIVATE)
                .edit()
                .putString("id", userId)
                .apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}