package com.pinetechs.orvix.ims.android.core.network;

import android.content.Context;

import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static final Map<String, Retrofit> CLIENTS = new ConcurrentHashMap<>();

    private ApiClient() {
    }

    public static Retrofit getClient(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String baseUrl = sessionManager.getApiBaseUrl();

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalStateException("Client API Base URL is not configured. Open SetupActivity first.");
        }

        return getClient(context, baseUrl, true);
    }

    public static Retrofit getBootstrapClient(Context context) {
        return getClient(context, Constants.BOOTSTRAP_BASE_URL, false);
    }

    private static Retrofit getClient(Context context, String rawBaseUrl, boolean includeAuthInterceptor) {
        String baseUrl = normalizeBaseUrl(rawBaseUrl);
        String key = baseUrl + "|auth=" + includeAuthInterceptor;

        Retrofit existing = CLIENTS.get(key);
        if (existing != null) {
            return existing;
        }

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        if (includeAuthInterceptor) {
            clientBuilder.addInterceptor(new AuthInterceptor(context));
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CLIENTS.put(key, retrofit);
        return retrofit;
    }

    public static void clearCache() {
        CLIENTS.clear();
    }

    private static String normalizeBaseUrl(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Base URL is required");
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Base URL is required");
        }

        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }

        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }

        return normalized;
    }
}
