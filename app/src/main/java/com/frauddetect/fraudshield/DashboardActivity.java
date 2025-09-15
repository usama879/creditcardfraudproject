package com.frauddetect.fraudshield;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.frauddetect.fraudshield.Fragments.AlertsFragment;
import com.frauddetect.fraudshield.Fragments.HomeFragment;
import com.frauddetect.fraudshield.Fragments.ProfileFragment;
import com.frauddetect.fraudshield.Fragments.TransactionFragment;

public class DashboardActivity extends AppCompatActivity {

    LinearLayout navHome, navTransactions, navAlerts, navProfile;
    ImageView iconHome, iconTransactions, iconAlerts, iconProfile;
    TextView textHome, textTransactions, textAlerts, textProfile;

    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);

        navHome = findViewById(R.id.navHome);
        navTransactions = findViewById(R.id.navTransactions);
        navAlerts = findViewById(R.id.navAlerts);
        navProfile = findViewById(R.id.navProfile);

        iconHome = findViewById(R.id.iconHome);
        iconTransactions = findViewById(R.id.iconTransactions);
        iconAlerts = findViewById(R.id.iconAlerts);
        iconProfile = findViewById(R.id.iconProfile);

        textHome = findViewById(R.id.textHome);
        textTransactions = findViewById(R.id.textTransactions);
        textAlerts = findViewById(R.id.textAlerts);
        textProfile = findViewById(R.id.textProfile);

        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                    if (fine != null && coarse != null && fine && coarse) {
                        Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        checkAndRequestLocationPermissions();

        loadFragment(new HomeFragment());
        setActive(navHome);

        navHome.setOnClickListener(v -> { loadFragment(new HomeFragment()); setActive(navHome); });
        navTransactions.setOnClickListener(v -> { loadFragment(new TransactionFragment()); setActive(navTransactions); });
        navAlerts.setOnClickListener(v -> { loadFragment(new AlertsFragment()); setActive(navAlerts); });
        navProfile.setOnClickListener(v -> { loadFragment(new ProfileFragment()); setActive(navProfile); });
    }

    private void checkAndRequestLocationPermissions() {
        boolean fineLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!fineLocationGranted || !coarseLocationGranted) {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentContainer, fragment)
                .commit();
    }

    private void setActive(LinearLayout selected) {
        resetNav();

        if (selected == navHome) {
            iconHome.setImageResource(R.drawable.ic_home_selected);
            textHome.setTextColor(ContextCompat.getColor(this, R.color.deep_blue));
            textHome.setTypeface(ResourcesCompat.getFont(this, R.font.inter_medium));
        } else if (selected == navTransactions) {
            iconTransactions.setImageResource(R.drawable.ic_transactions_selected_alt);
            textTransactions.setTextColor(ContextCompat.getColor(this, R.color.deep_blue));
            textTransactions.setTypeface(ResourcesCompat.getFont(this, R.font.inter_medium));
        } else if (selected == navAlerts) {
            iconAlerts.setImageResource(R.drawable.ic_alerts_selected_alt);
            textAlerts.setTextColor(ContextCompat.getColor(this, R.color.deep_blue));
            textAlerts.setTypeface(ResourcesCompat.getFont(this, R.font.inter_medium));
        } else if (selected == navProfile) {
            iconProfile.setImageResource(R.drawable.ic_profile_selected);
            textProfile.setTextColor(ContextCompat.getColor(this, R.color.deep_blue));
            textProfile.setTypeface(ResourcesCompat.getFont(this, R.font.inter_medium));
        }
    }

    private void resetNav() {
        iconHome.setImageResource(R.drawable.ic_home_unselected);
        textHome.setTextColor(ContextCompat.getColor(this, R.color.nav_unselected));
        textHome.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));

        iconTransactions.setImageResource(R.drawable.ic_transactions_unselected_alt);
        textTransactions.setTextColor(ContextCompat.getColor(this, R.color.nav_unselected));
        textTransactions.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));

        iconAlerts.setImageResource(R.drawable.ic_alerts_unselected_alt);
        textAlerts.setTextColor(ContextCompat.getColor(this, R.color.nav_unselected));
        textAlerts.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));

        iconProfile.setImageResource(R.drawable.ic_profile_unselected);
        textProfile.setTextColor(ContextCompat.getColor(this, R.color.nav_unselected));
        textProfile.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
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
