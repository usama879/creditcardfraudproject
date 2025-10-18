package com.frauddetect.fraudshield.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.frauddetect.fraudshield.FraudDetectionResultActivity;
import com.frauddetect.fraudshield.Models.ApiClient;
import com.frauddetect.fraudshield.Models.LoadingDialog;
import com.frauddetect.fraudshield.Models.SupabaseApi;
import com.frauddetect.fraudshield.Models.Transactions;
import com.frauddetect.fraudshield.R;
import com.google.android.material.button.MaterialButton;
import com.frauddetect.fraudshield.ml.FraudModel;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckFraudAdapter extends RecyclerView.Adapter<CheckFraudAdapter.TransactionViewHolder> {

    private static final String TAG = "CheckFraudAdapter1";
    private List<Transactions> transactionList;

    public CheckFraudAdapter(List<Transactions> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_1, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transactions transaction = transactionList.get(position);

        String tid = transaction.getTid();
        if (tid != null && tid.length() > 12) {
            tid = tid.substring(tid.length() - 12);
        }
        holder.tvTransactionId1.setText("TID: " + tid);
        holder.tvAmount1.setText("$" + transaction.getAmt());
        holder.tvCardNumber1.setText(maskCardNumber(transaction.getCc_num()));
        holder.tvMerchantName1.setText(
                transaction.getCategory_shopping_pos() != null && transaction.getCategory_shopping_pos().equals("1") ? "Shopping (POS)" :
                        transaction.getCategory_shopping_net() != null && transaction.getCategory_shopping_net().equals("1") ? "Shopping (NET)" :
                                transaction.getCategory_food_dining() != null && transaction.getCategory_food_dining().equals("1") ? "Food & Dining" :
                                        transaction.getCategory_misc_pos() != null && transaction.getCategory_misc_pos().equals("1") ? "Misc (POS)" :
                                                transaction.getCategory_misc_net() != null && transaction.getCategory_misc_net().equals("1") ? "Misc (NET)" :
                                                        transaction.getCategory_home() != null && transaction.getCategory_home().equals("1") ? "Home" :
                                                                transaction.getCategory_kids_pets() != null && transaction.getCategory_kids_pets().equals("1") ? "Kids & Pets" :
                                                                        transaction.getCategory_personal_care() != null && transaction.getCategory_personal_care().equals("1") ? "Personal Care" : "Unknown");

        holder.tvCity1.setText(transaction.getCity());
        holder.tvCoordinates1.setText(transaction.getLat() + " " + transaction.getLng());
        holder.tvTransactionDateTime1.setText(transaction.getDate() + " " + transaction.getTime());
        holder.tvUnixTime1.setText("Unix: " + transaction.getUnix_time());
        holder.tvStatus1.setText(transaction.getStatus());

        holder.btnCheckFraud1.setOnClickListener(v -> {
            Context context = v.getContext();
            LoadingDialog loadingDialog = new LoadingDialog(context);
            loadingDialog.show();

            new Thread(() -> {
                boolean isFraud = false;
                float confidence = 0f;

                try {
                    long startTime = System.currentTimeMillis();
                    FraudModel model = FraudModel.newInstance(context);

                    float[] inputArray = new float[]{
                            safeParse(transaction.getAmt(), "amt"),
                            safeParse(transaction.getAge(), "age"),
                            safeParse(transaction.getUnix_time(), "unix_time"),
                            safeParse(transaction.getCity_pop(), "city_pop"),
                            safeParse(transaction.getCc_num(), "cc_num"),
                            safeParse(transaction.getLat(), "lat"),
                            safeParse(transaction.getCategory_shopping_pos(), "shopping_pos"),
                            safeParse(transaction.getCategory_misc_pos(), "misc_pos"),
                            safeParse(transaction.getCategory_misc_net(), "misc_net"),
                            safeParse(transaction.getCategory_shopping_net(), "shopping_net"),
                            safeParse(transaction.getCategory_food_dining(), "food_dining"),
                            safeParse(transaction.getLng(), "lng"),
                            safeParse(transaction.getCategory_kids_pets(), "kids_pets"),
                            safeParse(transaction.getCategory_home(), "home"),
                            safeParse(transaction.getCategory_personal_care(), "personal_care")
                    };

                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 15}, DataType.FLOAT32);
                    inputFeature0.loadArray(inputArray);

                    FraudModel.Outputs outputs = model.process(inputFeature0);
                    float[] outputArray = outputs.getOutputFeature0AsTensorBuffer().getFloatArray();

                    long endTime = System.currentTimeMillis();
                    Log.d(TAG, "Model inference time: " + (endTime - startTime) + " ms");

                    float rawOutput = outputArray[0];
                    confidence = 1.0f / (1.0f + (float) Math.exp(-rawOutput));

                    isFraud = confidence > 0.5f;

                    for (int i = 6; i <= 14; i++) {
                        if (inputArray[i] == 1.0f) {
                            isFraud = false;
                            confidence = 0.0f;
                            break;
                        }
                    }

                    model.close();

                } catch (Exception e) {
                    Log.e(TAG, "Error during model execution", e);
                }

                boolean finalIsFraud = isFraud;
                float finalConfidence = confidence;
                SupabaseApi api = ApiClient.getClient().create(SupabaseApi.class);
                Map<String, String> statusBody = new HashMap<>();
                statusBody.put("status", finalIsFraud ? "fraud" : "not_fraud");

                Call<Void> call = api.updateTransactionStatus("eq." + transaction.getTid(), statusBody);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        loadingDialog.dismiss();
                        if (response.isSuccessful()) {
                            transaction.setStatus(finalIsFraud ? "fraud" : "not_fraud");
                            notifyItemChanged(holder.getAdapterPosition());
                        }
                        Intent intent = new Intent(context, FraudDetectionResultActivity.class);
                        intent.putExtra("tid", transaction.getTid());
                        intent.putExtra("cardNumber", transaction.getCc_num());
                        intent.putExtra("amount", transaction.getAmt());
                        intent.putExtra("result", finalIsFraud ? "fraud" : "not_fraud");
                        intent.putExtra("confidence", finalConfidence);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        loadingDialog.dismiss();
                        Intent intent = new Intent(context, FraudDetectionResultActivity.class);
                        intent.putExtra("tid", transaction.getTid());
                        intent.putExtra("cardNumber", transaction.getCc_num());
                        intent.putExtra("amount", transaction.getAmt());
                        intent.putExtra("result", finalIsFraud ? "fraud" : "not_fraud");
                        intent.putExtra("confidence", finalConfidence);
                        context.startActivity(intent);
                    }
                });
            }).start();
        });


    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionId1, tvAmount1, tvStatus1, tvCardNumber1, tvMerchantName1, tvCity1, tvCoordinates1, tvTransactionDateTime1, tvUnixTime1;
        MaterialButton btnCheckFraud1;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionId1 = itemView.findViewById(R.id.tvTransactionId1);
            tvAmount1 = itemView.findViewById(R.id.tvAmount1);
            tvStatus1 = itemView.findViewById(R.id.tvStatus1);
            tvCardNumber1 = itemView.findViewById(R.id.tvCardNumber1);
            tvMerchantName1 = itemView.findViewById(R.id.tvMerchantName1);
            tvCity1 = itemView.findViewById(R.id.tvCity1);
            tvCoordinates1 = itemView.findViewById(R.id.tvCoordinates1);
            tvTransactionDateTime1 = itemView.findViewById(R.id.tvTransactionDateTime1);
            tvUnixTime1 = itemView.findViewById(R.id.tvUnixTime1);
            btnCheckFraud1 = itemView.findViewById(R.id.btnCheckFraud1);
        }
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber != null && cardNumber.length() > 4) {
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return cardNumber != null ? cardNumber : "N/A";
    }

    private float safeParse(String value, String fieldName) {
        try {
            float f = Float.parseFloat(value);
            Log.d(TAG, "Parsed " + fieldName + ": " + f);
            return f;
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse " + fieldName + " with value: " + value, e);
            return 0f;
        }
    }

    public void updateData(List<Transactions> newData) {
        this.transactionList = newData;
        notifyDataSetChanged();
    }
}
