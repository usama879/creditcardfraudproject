package com.frauddetect.fraudshield;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.frauddetect.fraudshield.Models.ApiClient;
import com.frauddetect.fraudshield.Models.SupabaseApi;
import com.frauddetect.fraudshield.Models.User;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword, editTextPhone;
    private Button btnRegister;
    private ImageView ivTogglePassword, ivToggleConfirmPassword;
    private TextView tvLogin;

    private TextView tvUppercaseRule, tvLowercaseRule, tvNumberRule, tvSpecialCharRule;
    private ImageView imageUppercaseRule, imageLowercaseRule, imageNumberRule, imageSpecialCharRule;

    private LinearLayout passwordRules;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initViews();
        setupListeners();

    }

    private void initViews() {
        etName = findViewById(R.id.editTextName);
        etEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        etPassword = findViewById(R.id.editTextPassword);
        etConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        btnRegister = findViewById(R.id.buttonRegister);
        ivTogglePassword = findViewById(R.id.iconToggle);
        ivToggleConfirmPassword = findViewById(R.id.iconConfirmToggle);
        tvLogin = findViewById(R.id.textLogin);

        passwordRules = findViewById(R.id.passwordRules);
        tvUppercaseRule = findViewById(R.id.tvUppercaseRule);
        tvLowercaseRule = findViewById(R.id.tvLowercaseRule);
        tvNumberRule = findViewById(R.id.tvNumberRule);
        tvSpecialCharRule = findViewById(R.id.tvSpecialCharRule);

        imageUppercaseRule = findViewById(R.id.imageUppercaseRule);
        imageLowercaseRule = findViewById(R.id.imageLowercaseRule);
        imageNumberRule = findViewById(R.id.imageNumberRule);
        imageSpecialCharRule = findViewById(R.id.imageSpecialCharRule);

    }

    private void setupListeners() {
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });

        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        ivToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility());

        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                registerUser();
            }
        });

        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                passwordRules.setVisibility(TextView.VISIBLE);
            } else {

            }
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();

                if (password.matches(".*[A-Z].*")) {
                    tvUppercaseRule.setTextColor(getResources().getColor(R.color.lighter_blue));
                    imageUppercaseRule.setColorFilter(getResources().getColor(R.color.deep_blue), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    tvUppercaseRule.setTextColor(getResources().getColor(R.color.subtitle_gray));
                    imageUppercaseRule.setColorFilter(getResources().getColor(R.color.subtitle_gray), android.graphics.PorterDuff.Mode.SRC_IN);
                }

                if (password.matches(".*[a-z].*")) {
                    tvLowercaseRule.setTextColor(getResources().getColor(R.color.lighter_blue));
                    imageLowercaseRule.setColorFilter(getResources().getColor(R.color.deep_blue), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    tvLowercaseRule.setTextColor(getResources().getColor(R.color.subtitle_gray));
                    imageLowercaseRule.setColorFilter(getResources().getColor(R.color.subtitle_gray), android.graphics.PorterDuff.Mode.SRC_IN);
                }

                if (password.matches(".*\\d.*")) {
                    tvNumberRule.setTextColor(getResources().getColor(R.color.lighter_blue));
                    imageNumberRule.setColorFilter(getResources().getColor(R.color.deep_blue), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    tvNumberRule.setTextColor(getResources().getColor(R.color.subtitle_gray));
                    imageNumberRule.setColorFilter(getResources().getColor(R.color.subtitle_gray), android.graphics.PorterDuff.Mode.SRC_IN);
                }

                if (password.matches(".*[!@#$%^&*+=?-].*")) {
                    tvSpecialCharRule.setTextColor(getResources().getColor(R.color.lighter_blue));
                    imageSpecialCharRule.setColorFilter(getResources().getColor(R.color.deep_blue), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    tvSpecialCharRule.setTextColor(getResources().getColor(R.color.subtitle_gray));
                    imageSpecialCharRule.setColorFilter(getResources().getColor(R.color.subtitle_gray), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });


    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_eye_on);
        }
        etPassword.setSelection(etPassword.length());
        isPasswordVisible = !isPasswordVisible;
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye_off);
        } else {
            etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye_on);
        }
        etConfirmPassword.setSelection(etConfirmPassword.length());
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
    }

    private void saveUserIdToPrefs(String userId) {
        getSharedPreferences("user_pref", MODE_PRIVATE)
                .edit()
                .putString("id", userId)
                .apply();
    }

    private boolean validateInputs() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email");
            return false;
        }

        if (!password.matches(".*[A-Z].*") ||
                !password.matches(".*[a-z].*") ||
                !password.matches(".*\\d.*") ||
                !password.matches(".*[!@#$%^&*+=?-].*")) {
            etPassword.setError("Password must include uppercase, lowercase, number, and symbol");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }

        if (phone.length() < 5) {
            editTextPhone.setError("Enter valid phone number");
            return false;
        }

        return true;
    }

    private void registerUser() {
        String id = UUID.randomUUID().toString();
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setMessage("Please wait, we are creating account...");
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
        progressDialog.show();

        SupabaseApi api = ApiClient.getClient().create(SupabaseApi.class);

        api.checkUserByEmail("eq." + email).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Account already exists with this email.", Toast.LENGTH_LONG).show();
                } else {
                    User user = new User(id, name, email, phone, password);
                    api.createUser(user).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_LONG).show();
                                        saveUserIdToPrefs(id);
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                }, 1600);
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "Registration Failed: " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);

    }
}