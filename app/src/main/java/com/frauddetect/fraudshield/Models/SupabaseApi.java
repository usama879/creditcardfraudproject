package com.frauddetect.fraudshield.Models;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {

    String API_KEY = "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11dGhycmp4cXR2Y2dtYnJ6a2x4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcyNzExNjcsImV4cCI6MjA3Mjg0NzE2N30.bcpLEom-1JSwql_QGaicGdLIHM7rIXA_mZwK_YmMC-w";
    String AUTHORIZATION = "Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11dGhycmp4cXR2Y2dtYnJ6a2x4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcyNzExNjcsImV4cCI6MjA3Mjg0NzE2N30.bcpLEom-1JSwql_QGaicGdLIHM7rIXA_mZwK_YmMC-w";

    @Headers({API_KEY, AUTHORIZATION})
    @GET("users")
    Call<List<User>> checkUserByEmail(@Query("email") String emailFilter);

    @Headers({API_KEY, AUTHORIZATION, "Content-Type: application/json"})
    @POST("users")
    Call<Void> createUser(@Body User user);

    @Headers({API_KEY, AUTHORIZATION})
    @GET("users")
    Call<List<User>> getUserById(@Query("id") String idFilter);

}

