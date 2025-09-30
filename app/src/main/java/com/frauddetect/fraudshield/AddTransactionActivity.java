package com.frauddetect.fraudshield;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.frauddetect.fraudshield.Models.ApiClient;
import com.frauddetect.fraudshield.Models.SupabaseApi;
import com.frauddetect.fraudshield.Models.Transactions;
import com.frauddetect.fraudshield.Models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddTransactionActivity extends AppCompatActivity {

    TextInputEditText etAmount, etCardNumber, etCity, etLatitude, etLongitude, etDate, etTime;
    AutoCompleteTextView spinnerMerchantCategory;
    MaterialButton btnSaveTransaction;
    FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    TextView etCityPopulation, etAge, etUserId;

    int category_shopping_pos = 0;
    int category_misc_pos = 0;
    int category_misc_net = 0;
    int category_shopping_net = 0;
    int category_food_dining = 0;
    int category_kids_pets = 0;
    int category_home = 0;
    int category_personal_care = 0;
    MaterialToolbar addTransactionToolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        etAmount = findViewById(R.id.etAmount);
        spinnerMerchantCategory = findViewById(R.id.spinnerMerchantCategory);
        etCardNumber = findViewById(R.id.etCardNumber);
        etCity = findViewById(R.id.etCity);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction);
        etCityPopulation= findViewById(R.id.etCityPopulation);
        etAge = findViewById(R.id.etAge);
        etUserId = findViewById(R.id.etUserId);
        addTransactionToolbar = findViewById(R.id.addTransactionToolbar);

        setupMerchantDropdown();
        setupDatePicker();
        setupTimePicker();
        setupSaveButton();

        addTransactionToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AddTransactionActivity.this, DashboardActivity.class);
                startActivity(intent);

            }
        });


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkLocationPermissionAndFetch();
        loadUserAgeFromSupabase();
    }

    private void loadUserAgeFromSupabase() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", null);

        if (userId == null) {
            return;
        } else {
            etUserId.setText(userId);
        }

        Retrofit retrofit = ApiClient.getClient();
        SupabaseApi api = retrofit.create(SupabaseApi.class);

        Call<List<User>> call = api.getUserById("eq." + userId);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User user = response.body().get(0);
                    etAge.setText(user.getAge());

                } else {

                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

            }
        });
    }

    private void setupMerchantDropdown() {
        List<String> categories = Arrays.asList(
                "Food & Dining",
                "Kids & Pets",
                "Home",
                "Health & Fitness",
                "Misc (POS)",
                "Misc (Net)",
                "Shopping (POS)",
                "Shopping (Net)"

        );

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        spinnerMerchantCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select Date")
                            .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                etDate.setText(sdf.format(selection));
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });
    }

    private void setupTimePicker() {
        etTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            MaterialTimePicker timePicker =
                    new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                            .setMinute(calendar.get(Calendar.MINUTE))
                            .setTitleText("Select Time")
                            .build();

            timePicker.addOnPositiveButtonClickListener(view -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d",
                        timePicker.getHour(), timePicker.getMinute());
                etTime.setText(selectedTime);
            });

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etAmount.getText())) {
            etAmount.setError("Enter amount");
            return false;
        }
        if (TextUtils.isEmpty(spinnerMerchantCategory.getText())) {
            spinnerMerchantCategory.setError("Select merchant");
            return false;
        }
        if (TextUtils.isEmpty(etCardNumber.getText())) {
            etCardNumber.setError("Enter card number");
            return false;
        } else if (etCardNumber.getText().toString().trim().length() != 16) {
            etCardNumber.setError("Enter valid card number (16 digits)");
            return false;
        }
        if (TextUtils.isEmpty(etCity.getText())) {
            etCity.setError("Enter city");
            return false;
        }
        if (TextUtils.isEmpty(etLatitude.getText())) {
            etLatitude.setError("Enter latitude");
            return false;
        }
        if (TextUtils.isEmpty(etLongitude.getText())) {
            etLongitude.setError("Enter longitude");
            return false;
        }
        if (TextUtils.isEmpty(etDate.getText())) {
            etDate.setError("Pick date");
            return false;
        }
        if (TextUtils.isEmpty(etTime.getText())) {
            etTime.setError("Pick time");
            return false;
        }
        return true;
    }

    private void checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        fetchCurrentLocation();
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    etLatitude.setText(String.valueOf(lat));
                    etLongitude.setText(String.valueOf(lon));

                    try {
                        Geocoder geocoder = new Geocoder(AddTransactionActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            String cityName = addresses.get(0).getLocality();
                            etCity.setText(cityName != null ? cityName : "");

                            int cityPopulation = getPopulationForCity(etCity.getText().toString());

                            if (cityPopulation != -1) {
                                etCityPopulation.setText(String.valueOf(cityPopulation));
                            } else {

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int getPopulationForCity(String cityName) {
        int population = -1;

        try {
            InputStream is = getAssets().open("worldcities.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            String headerLine = reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                String cityInRow = tokens[1].trim();
                String populationStr = tokens[9].trim();
                if (cityInRow.equalsIgnoreCase(cityName)) {
                    population = Integer.parseInt(populationStr);
                    break;
                }
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return population;
    }

    private int[] getCategoryOneHot(String category) {
        int[] oneHot = new int[8];

        switch (category) {
            case "Shopping (POS)": oneHot[0] = 1; break;
            case "Misc (POS)":     oneHot[1] = 1; break;
            case "Misc (Net)":     oneHot[2] = 1; break;
            case "Shopping (Net)": oneHot[3] = 1; break;
            case "Food & Dining":  oneHot[4] = 1; break;
            case "Kids & Pets":    oneHot[5] = 1; break;
            case "Home":           oneHot[6] = 1; break;
            case "Personal Care":  oneHot[7] = 1; break;
            default: break;
        }

        return oneHot;
    }

    private void setupSaveButton() {
        btnSaveTransaction.setOnClickListener(v -> {
            if (!validateInputs()) return;

            android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(AddTransactionActivity.this);
            progressDialog.setMessage("Saving details...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            double amount = Double.parseDouble(etAmount.getText().toString());
            double cityPop = Double.parseDouble(etCityPopulation.getText().toString());

            double amtMin = 1.0, amtMax = 28948.9;
            double popMin = 23.0, popMax = 2906700.0;

            double normalizedAmt = (amount - amtMin) / (amtMax - amtMin);
            double normalizedPop = (cityPop - popMin) / (popMax - popMin);

            String normalizedAmtStr = String.format(Locale.getDefault(), "%.6f", normalizedAmt);
            String normalizedPopStr = String.format(Locale.getDefault(), "%.6f", normalizedPop);


            int[] oneHot = getCategoryOneHot(spinnerMerchantCategory.getText().toString());

            String unixTime = String.valueOf(System.currentTimeMillis() / 1000L);
            String tid = java.util.UUID.randomUUID().toString();
            String age = etAge.getText().toString();
            String maskedCard = etCardNumber.getText().toString();
            String lat = etLatitude.getText().toString();
            String lng = etLongitude.getText().toString();
            String amountRaw = etAmount.getText().toString();
            String city = etCity.getText().toString();
            String populationRaw = etCityPopulation.getText().toString();
            String date = etDate.getText().toString();
            String time = etTime.getText().toString();
            String userId = etUserId.getText().toString();

            Transactions transaction = new Transactions(
                    tid,
                    normalizedAmtStr,
                    age,
                    unixTime,
                    normalizedPopStr,
                    maskedCard,
                    lat,
                    lng,
                    String.valueOf(oneHot[0]),
                    String.valueOf(oneHot[1]),
                    String.valueOf(oneHot[2]),
                    String.valueOf(oneHot[3]),
                    String.valueOf(oneHot[4]),
                    String.valueOf(oneHot[5]),
                    String.valueOf(oneHot[6]),
                    String.valueOf(oneHot[7]),
                    amountRaw,
                    city,
                    populationRaw,
                    date,
                    time,
                    userId,
                    "Unverified"
            );

            SupabaseApi api = ApiClient.getClient().create(SupabaseApi.class);
            Call<Void> call = api.createTransaction(transaction);
            call.enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                    if (response.isSuccessful()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(AddTransactionActivity.this, "Transaction details saved", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AddTransactionActivity.this, DashboardActivity.class);
                                startActivity(intent);
                            }
                        }, 2000);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(AddTransactionActivity.this, "Failed to save transaction details", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    progressDialog.dismiss();
                    t.printStackTrace();
                    Toast.makeText(AddTransactionActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(AddTransactionActivity.this, DashboardActivity.class);
        startActivity(intent);

    }
}