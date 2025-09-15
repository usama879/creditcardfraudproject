package com.frauddetect.fraudshield;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    TextInputEditText etAmount, etCardNumber, etCity, etLatitude, etLongitude, etDate, etTime;
    AutoCompleteTextView spinnerMerchantCategory;
    MaterialButton btnSaveTransaction;
    FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 100;


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

        setupMerchantDropdown();
        setupDatePicker();
        setupTimePicker();
        setupSaveButton();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkLocationPermissionAndFetch();
    }

    private void setupMerchantDropdown() {
        List<String> categories = Arrays.asList(
                "Food & Dining",
                "Kids & Pets",
                "Home",
                "Health & Fitness",
                "Misc (POS / Net)",
                "Shopping (POS / Net)"
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

    private void setupSaveButton() {
        btnSaveTransaction.setOnClickListener(v -> {
            if (validateInputs()) {
                Toast.makeText(this, "Transaction saved successfully!", Toast.LENGTH_SHORT).show();
                // TODO: Save to DB / send to ML model
            }
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
}