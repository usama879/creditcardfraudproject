package com.frauddetect.fraudshield.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.frauddetect.fraudshield.Models.Transactions;
import com.frauddetect.fraudshield.R;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transactions> transactionList;

    public TransactionAdapter(List<Transactions> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transactions transaction = transactionList.get(position);

        String tid = transaction.getTid();
        if (tid != null && tid.length() > 12) {
            tid = tid.substring(tid.length() - 12);
        }
        holder.tvTransactionId.setText("TID: " + tid);

        holder.tvAmount.setText("$" + transaction.getAmount());
        holder.tvCardNumber.setText(maskCardNumber(transaction.getCc_num()));
        holder.tvMerchantName.setText(transaction.getCategory_shopping_pos() != null && transaction.getCategory_shopping_pos().equals("1") ? "Shopping (POS)" :
                transaction.getCategory_shopping_net() != null && transaction.getCategory_shopping_net().equals("1") ? "Shopping (NET)" :
                        transaction.getCategory_food_dining() != null && transaction.getCategory_food_dining().equals("1") ? "Food & Dining" :
                                transaction.getCategory_misc_pos() != null && transaction.getCategory_misc_pos().equals("1") ? "Misc (POS)" :
                                        transaction.getCategory_misc_net() != null && transaction.getCategory_misc_net().equals("1") ? "Misc (NET)" :
                                                transaction.getCategory_home() != null && transaction.getCategory_home().equals("1") ? "Home" :
                                                        transaction.getCategory_kids_pets() != null && transaction.getCategory_kids_pets().equals("1") ? "Kids & Pets" :
                                                                transaction.getCategory_personal_care() != null && transaction.getCategory_personal_care().equals("1") ? "Personal Care" : "Unknown");

        holder.tvCity.setText(transaction.getCity());
        holder.tvLatitude.setText(transaction.getLat());
        holder.tvLongitude.setText(transaction.getLng());
        holder.tvTransactionDateTime.setText(transaction.getDate() + " " + transaction.getTime());
        holder.tvUnixTime.setText(transaction.getUnix_time());
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionId, tvAmount, tvCardNumber, tvMerchantName, tvCity, tvLatitude, tvLongitude, tvTransactionDateTime, tvUnixTime;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionId = itemView.findViewById(R.id.tvTransactionId);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCardNumber = itemView.findViewById(R.id.tvCardNumber);
            tvMerchantName = itemView.findViewById(R.id.tvMerchantName);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvLatitude = itemView.findViewById(R.id.tvLatitude);
            tvLongitude = itemView.findViewById(R.id.tvLongitude);
            tvTransactionDateTime = itemView.findViewById(R.id.tvTransactionDateTime);
            tvUnixTime = itemView.findViewById(R.id.tvUnixTime);
        }
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber != null && cardNumber.length() > 4) {
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return cardNumber != null ? cardNumber : "N/A";
    }

    public void updateData(List<Transactions> newData) {
        this.transactionList = newData;
        notifyDataSetChanged();
    }

}
