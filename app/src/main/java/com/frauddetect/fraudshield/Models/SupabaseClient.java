package com.frauddetect.fraudshield.Models;

import retrofit2.Retrofit;

public class SupabaseClient {

    private static SupabaseClient instance;
    private SupabaseApi api;

    private SupabaseClient() {
        Retrofit retrofit = ApiClient.getClient();
        api = retrofit.create(SupabaseApi.class);
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }

    public SupabaseApi getApi() {
        return api;
    }
}
