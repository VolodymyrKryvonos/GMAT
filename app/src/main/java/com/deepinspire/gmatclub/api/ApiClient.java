package com.deepinspire.gmatclub.api;

import com.deepinspire.gmatclub.utils.WebviewCookieHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;

    public Retrofit getClient() {
        if (retrofit != null) return retrofit;
        try {
            //OkHttpClient.Builder client = new OkHttpClient.Builder();
            OkHttpClient.Builder okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(60 * 5, TimeUnit.SECONDS)
                    .readTimeout(60 * 5, TimeUnit.SECONDS)
                    .writeTimeout(60 * 5, TimeUnit.SECONDS);
            okHttpClient.cookieJar(new WebviewCookieHandler());

            Retrofit.Builder builder = new Retrofit.Builder().baseUrl(Api.HOME_URL + "/");

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = builder
                    .client(okHttpClient.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            return retrofit;
        } catch (final Exception exception) {
            return null;
        }
    }

}