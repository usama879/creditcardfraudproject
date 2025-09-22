package com.frauddetect.fraudshield.Models;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.frauddetect.fraudshield.R;

public class LoadingDialog extends Dialog {

    private TextView tvDialogTitle;
    private TextView tvDialogMessage;
    private View[] stepIndicators;
    private TextView[] stepTexts;
    private Handler handler;
    private int currentStep = 0;

    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        setCancelable(false);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews();
        startProgressAnimation();
    }

    private void initViews() {
        tvDialogTitle = findViewById(R.id.tvDialogTitle);
        tvDialogMessage = findViewById(R.id.tvDialogMessage);

        View llProgressSteps = findViewById(R.id.llProgressSteps);
        if (llProgressSteps != null && llProgressSteps instanceof View) {
            stepIndicators = new View[]{
                    llProgressSteps.findViewById(R.id.stepIndicator1),
                    llProgressSteps.findViewById(R.id.stepIndicator2)
            };
            stepTexts = new TextView[]{
                    (TextView) ((ViewGroup) llProgressSteps).getChildAt(0).findViewById(R.id.stepText1),
                    (TextView) ((ViewGroup) llProgressSteps).getChildAt(1).findViewById(R.id.stepText2)
            };
        }
    }

    private void startProgressAnimation() {
        handler = new Handler(Looper.getMainLooper());
        currentStep = 0;

        handler.postDelayed(this::nextStep, 1000);
    }

    private void nextStep() {
        currentStep++;
        updateStep(currentStep);

        if (currentStep == 1) {
            updateMessage("Validating transaction data...");
            handler.postDelayed(this::nextStep, 1600);
        } else if (currentStep == 2) {
            updateMessage("Running ML fraud detection model...");
            handler.postDelayed(this::nextStep, 1600);
        }
    }

    private void updateStep(int step) {
        if (stepIndicators != null && stepIndicators.length > 0) {
            for (int i = 0; i < stepIndicators.length; i++) {
                if (i < step) {
                    stepIndicators[i].setBackgroundResource(R.drawable.step_indicator_active);
                    if (stepTexts != null && stepTexts.length > i)
                        stepTexts[i].setTextColor(getContext().getColor(R.color.deep_blue));
                } else {
                    stepIndicators[i].setBackgroundResource(R.drawable.step_indicator_inactive);
                    if (stepTexts != null && stepTexts.length > i)
                        stepTexts[i].setTextColor(getContext().getColor(R.color.text_secondary));
                }
            }
        }
    }

    private void updateMessage(String message) {
        if (tvDialogMessage != null) {
            tvDialogMessage.setText(message);
        }
    }

    public void updateTitle(String title) {
        if (tvDialogTitle != null) {
            tvDialogTitle.setText(title);
        }
    }

    @Override
    public void dismiss() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.dismiss();
    }
}
