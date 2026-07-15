package com.pinetechs.orvix.ims.android.core.network;

import android.content.Context;
import android.content.pm.ApplicationInfo;

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
        return getClient(context, true);
    }

    public static Retrofit getPublicClient(Context context) {
        return getClient(context, false);
    }

    private static Retrofit getClient(Context context, boolean includeAuthInterceptor) {
        SessionManager sessionManager = new SessionManager(context);
        String baseUrl = sessionManager.getApiBaseUrl();

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalStateException("Client API Base URL is not configured. Open SetupActivity first.");
        }

        // بناء المسار الموحد للخدمات: Base URL + api/app/v1/
        String serviceUrl = normalizeBaseUrl(baseUrl);
        if (serviceUrl.endsWith("/api/")) {
            serviceUrl += "app/v1/";
        } else if (!serviceUrl.endsWith("/api/app/v1/")) {
            serviceUrl += "api/app/v1/";
        }

        return getClient(context, serviceUrl, includeAuthInterceptor);
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
        loggingInterceptor.redactHeader("Authorization");
        loggingInterceptor.setLevel(isDebuggable(context)
                ? HttpLoggingInterceptor.Level.BASIC
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        if (includeAuthInterceptor) {
            clientBuilder.addInterceptor(new AuthInterceptor(context));
        }

        clientBuilder.addInterceptor(loggingInterceptor);

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

    private static boolean isDebuggable(Context context) {
        return context != null
                && (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}
