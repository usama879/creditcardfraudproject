package com.frauddetect.fraudshield.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.frauddetect.fraudshield.Models.Transactions;
import com.frauddetect.fraudshield.R;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentAlertsAdapter extends RecyclerView.Adapter<RecentAlertsAdapter.AlertViewHolder> {

    private Context context;
    private final List<Transactions> alerts;

    public RecentAlertsAdapter(Context context, List<Transactions> alerts) {
        this.context = context;
        this.alerts = alerts;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recent_alert_item, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Transactions alert = alerts.get(position);

        String tid = alert.getTid();
        if (tid != null && tid.length() > 12) {
            tid = tid.substring(tid.length() - 12);
        }
        holder.tvTransactionId.setText("TXN" + tid);

        double amountValue = parseAmount(alert.getAmount());
        holder.tvAmount.setText(formatCurrency(amountValue));

        holder.tvTime.setText(getRelativeTime(alert.getDate(), alert.getTime()));

        String status = alert.getStatus();
        updateStatusAppearance(holder, status);
    }

    @Override
    public int getItemCount() {
        return alerts != null ? alerts.size() : 0;
    }

    private void updateStatusAppearance(AlertViewHolder holder, String status) {
        if (status == null) {
            status = "pending";
        }

        switch (status.toLowerCase()) {
            case "fraud":
                updateAlertColors(holder,
                        Color.parseColor("#FFEBEE"),
                        Color.parseColor("#F44336"),
                        R.drawable.ic_warning,
                        "FRAUD");
                break;
            case "not_fraud":
                updateAlertColors(holder,
                        Color.parseColor("#E8F5E8"),
                        Color.parseColor("#4CAF50"),
                        R.drawable.ic_check_circle,
                        "VALID");
                break;
            case "unverified":
            case "pending":
            default:
                updateAlertColors(holder,
                        ContextCompat.getColor(context, R.color.primary_container),
                        ContextCompat.getColor(context, R.color.lighter_blue),
                        R.drawable.ic_schedule_modern,
                        "UNVERIFIED");
                break;
        }
    }

    private void updateAlertColors(AlertViewHolder holder, int iconBg, int accentColor,
                                   int iconRes, String statusText) {
        holder.cvAlertIcon.setCardBackgroundColor(iconBg);
        holder.ivAlertStatus.setImageResource(iconRes);
        holder.ivAlertStatus.setColorFilter(accentColor);

        holder.cvStatusBadge.setCardBackgroundColor(accentColor);
        holder.tvStatus.setText(statusText);
        holder.tvStatus.setTextColor(Color.WHITE);
    }

    private double parseAmount(String amount) {
        try {
            return Double.parseDouble(amount);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String formatCurrency(double amount) {
        if (amount >= 1000000) {
            return String.format(Locale.getDefault(), "$%.1fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format(Locale.getDefault(), "$%.1fK", amount / 1000);
        } else {
            return String.format(Locale.getDefault(), "$%.2f", amount);
        }
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private String getRelativeTime(String dateStr, String timeStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date transactionDate = format.parse(dateStr + " " + timeStr + ":00");
            Date now = new Date();

            long diffInMillis = now.getTime() - transactionDate.getTime();
            long diffInMinutes = diffInMillis / (60 * 1000);
            long diffInHours = diffInMinutes / 60;
            long diffInDays = diffInHours / 24;

            if (diffInMinutes < 1) {
                return "Just now";
            } else if (diffInMinutes < 60) {
                return diffInMinutes + " mins ago";
            } else if (diffInHours < 24) {
                return diffInHours + " hrs ago";
            } else {
                return diffInDays + " days ago";
            }
        } catch (ParseException e) {
            return "Recently";
        }
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber != null && cardNumber.length() > 4) {
            return "**** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return cardNumber != null ? cardNumber : "N/A";
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cvAlertIcon, cvStatusBadge;
        ImageView ivAlertStatus;
        TextView tvTransactionId, tvAmount, tvTime, tvStatus;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            cvAlertIcon = itemView.findViewById(R.id.cvAlertIcon);
            cvStatusBadge = itemView.findViewById(R.id.cvStatusBadge);
            ivAlertStatus = itemView.findViewById(R.id.ivAlertStatus);
            tvTransactionId = itemView.findViewById(R.id.tvAlertTransactionId);
            tvAmount = itemView.findViewById(R.id.tvAlertAmount);
            tvTime = itemView.findViewById(R.id.tvAlertTime);
            tvStatus = itemView.findViewById(R.id.tvAlertStatus);
        }
    }
}