package com.frauddetect.fraudshield;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class FraudDetectionResultActivity extends AppCompatActivity {

    private TextView tvTransactionStatus, tvStatusMessage, tvTid, tvCardNumber,
            tvAmount, tvAnalysisTime, tvConfidenceScore;
    private ImageView ivStatusIcon;
    private MaterialCardView cvStatusIcon;
    private MaterialButton btnAnalyzeAnother;
    MaterialToolbar fraudAnalysisToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fraud_detection_result);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        fraudAnalysisToolbar = findViewById(R.id.fraudAnalysisToolbar);

        fraudAnalysisToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FraudDetectionResultActivity.this, FraudDetectionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        });


        initViews();
        displayResults();
        setupButtons();


    }

    private void initViews() {
        tvTransactionStatus = findViewById(R.id.tvTransactionStatus);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);
        tvTid = findViewById(R.id.tvTid);
        tvCardNumber = findViewById(R.id.tvCardNumber);
        tvAmount = findViewById(R.id.tvAmount);
        tvAnalysisTime = findViewById(R.id.tvAnalysisTime);
        tvConfidenceScore = findViewById(R.id.tvConfidenceScore);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        cvStatusIcon = findViewById(R.id.cvStatusIcon);
        btnAnalyzeAnother = findViewById(R.id.btnAnalyzeAnother);

    }

    private void displayResults() {
        String tid = getIntent().getStringExtra("tid");
        String cardNumber = getIntent().getStringExtra("cardNumber");
        String amount = getIntent().getStringExtra("amount");
        String result = getIntent().getStringExtra("result");
        float prediction = getIntent().getFloatExtra("prediction", 0.5f);
        float finalConfidence = getIntent().getFloatExtra("confidence", 0.0f);


        tvTid.setText(formatTid(tid));
        tvCardNumber.setText(maskCardNumber(cardNumber));
        tvAmount.setText("$" + amount);

        SimpleDateFormat timeFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        tvAnalysisTime.setText(timeFormat.format(new Date()));

        int confidence = Math.round(prediction * 100);
        tvConfidenceScore.setText(String.valueOf(finalConfidence));

        if ("fraud".equalsIgnoreCase(result)) {
            setupFraudUI();
        } else {
            setupLegitUI();
        }
    }

    private void setupFraudUI() {
        tvTransactionStatus.setText("Fraud Detected!");
        tvTransactionStatus.setTextColor(Color.parseColor("#D32F2F"));

        tvStatusMessage.setText("This transaction shows suspicious patterns and has been flagged as potentially fraudulent. Please verify with the cardholder.");

        cvStatusIcon.setCardBackgroundColor(Color.parseColor("#FFEBEE"));

        ivStatusIcon.setImageResource(R.drawable.ic_warning);
        ivStatusIcon.setColorFilter(Color.parseColor("#D32F2F"));
    }

    private void setupLegitUI() {
        tvTransactionStatus.setText("Transaction Verified");
        tvTransactionStatus.setTextColor(Color.parseColor("#388E3C"));

        tvStatusMessage.setText("This transaction appears legitimate based on our AI analysis. All patterns match normal spending behavior.");

        cvStatusIcon.setCardBackgroundColor(Color.parseColor("#E8F5E8"));

        ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
        ivStatusIcon.setColorFilter(Color.parseColor("#388E3C"));
    }

    private void setupButtons() {
        btnAnalyzeAnother.setOnClickListener(v -> {
            Intent intent = new Intent(this, FraudDetectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });


    }

    private String formatTid(String tid) {
        if (tid != null && tid.length() > 12) {
            return tid.substring(tid.length() - 12);
        }
        return tid != null ? tid : "N/A";
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber != null && cardNumber.length() > 4) {
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return cardNumber != null ? cardNumber : "N/A";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, FraudDetectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }
}