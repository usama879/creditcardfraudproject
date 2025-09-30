package com.frauddetect.fraudshield.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.frauddetect.fraudshield.DashboardActivity;
import com.frauddetect.fraudshield.Models.SupabaseApi;
import com.frauddetect.fraudshield.Models.SupabaseClient;
import com.frauddetect.fraudshield.Models.Transactions;
import com.frauddetect.fraudshield.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;


public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    private Context context;
    private final List<Transactions> alerts;
    private OnActionClickListener actionClickListener;

    public interface OnActionClickListener {
        void onActionClick(Transactions transaction, int position);
    }

    public AlertsAdapter(Context context, List<Transactions> alerts) {
        this.context = context;
        this.alerts = alerts;
    }

    public void setOnActionClickListener(OnActionClickListener listener) {
        this.actionClickListener = listener;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alerts, parent, false);
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

        holder.tvDate.setText(formatDateShort(alert.getDate()));
        holder.tvTime.setText(getRelativeTime(alert.getDate(), alert.getTime()));
        holder.tvCardNumber.setText(maskCardNumber(alert.getCc_num()));

        String status = alert.getStatus();
        updateStatusAppearance1(holder, status);

        holder.btnTakeAction.setOnClickListener(v -> {
            if (actionClickListener != null) {
                actionClickListener.onActionClick(alert, position);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (actionClickListener != null) {
                actionClickListener.onActionClick(alert, position);
            }
        });

        if (status.equals("fraud")) {
            holder.btnTakeAction.setVisibility(View.VISIBLE);
        } else if (status.equals("not_fraud")) {
            holder.btnTakeAction.setVisibility(View.GONE);
        } else {
            holder.btnTakeAction.setVisibility(View.VISIBLE);
        }

        holder.btnTakeAction.setOnClickListener(v -> {
            if ("fraud".equalsIgnoreCase(status)) {
                ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Blocking " + alert.getCc_num() + " card for further transactions");
                progressDialog.setCancelable(false);
                progressDialog.show();

                SupabaseApi api = SupabaseClient.getInstance().getApi();
                Log.d("AlertsAdapter", "Attempting to delete tid: " + alert.getTid());

                api.deleteTransaction("eq." + alert.getTid()).enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                progressDialog.dismiss();
                                if (response.isSuccessful()) {
                                    Toast.makeText(context, "Card Blocked Temporary", Toast.LENGTH_SHORT).show();
                                    alerts.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    notifyItemRangeChanged(holder.getAdapterPosition(), alerts.size());

                                } else {
                                    Toast.makeText(context, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, 1300);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Intent intent = new Intent(context, DashboardActivity.class);
                intent.putExtra("openFragment", "alerts");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return alerts != null ? alerts.size() : 0;
    }

    private void updateStatusAppearance1(AlertViewHolder holder, String status) {
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

    private void updateAlertColors(AlertsAdapter.AlertViewHolder holder, int iconBg, int accentColor,
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
            return String.format(Locale.getDefault(), "$%.0f", amount);
        }
    }

    private String formatDateShort(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private String getRelativeTime(String dateStr, String timeStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date transactionDate = format.parse(dateStr + " " + (timeStr != null ? timeStr : "00:00") + ":00");
            Date now = new Date();

            long diffInMillis = now.getTime() - transactionDate.getTime();
            long diffInMinutes = diffInMillis / (60 * 1000);
            long diffInHours = diffInMinutes / 60;
            long diffInDays = diffInHours / 24;

            if (diffInMinutes < 1) {
                return "Just now";
            } else if (diffInMinutes < 60) {
                return diffInMinutes + "m ago";
            } else if (diffInHours < 24) {
                return diffInHours + "h ago";
            } else if (diffInDays == 1) {
                return "Yesterday";
            } else if (diffInDays < 7) {
                return diffInDays + "d ago";
            } else {
                return "1w+ ago";
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

    public void updateData(List<Transactions> newAlerts) {
        this.alerts.clear();
        this.alerts.addAll(newAlerts);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < alerts.size()) {
            alerts.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cvStatusBadge, cvAlertIcon;
        TextView tvTransactionId, tvAmount, tvDate, tvCardNumber, tvTime, tvStatus;
        MaterialButton btnTakeAction;
        ImageView ivAlertStatus;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            cvStatusBadge = itemView.findViewById(R.id.cvStatusBadge);
            cvAlertIcon = itemView.findViewById(R.id.cvAlertIcon);
            tvTransactionId = itemView.findViewById(R.id.tvAlertTransactionId);
            tvAmount = itemView.findViewById(R.id.tvAlertAmount);
            tvDate = itemView.findViewById(R.id.tvAlertDate);
            tvCardNumber = itemView.findViewById(R.id.tvAlertCardNumber);
            tvTime = itemView.findViewById(R.id.tvAlertTime);
            tvStatus = itemView.findViewById(R.id.tvAlertStatus);
            btnTakeAction = itemView.findViewById(R.id.btnTakeAction);
            ivAlertStatus = itemView.findViewById(R.id.ivAlertStatus);

        }
    }
}