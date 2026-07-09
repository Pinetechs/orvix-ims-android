package com.pinetechs.orvix.ims.android.auth.presentation;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.textfield.TextInputEditText;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.bootstrap.presentation.SetupActivity;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.LocaleHelper;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.task.presentation.TaskListActivity;

import android.content.Context;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    private LoginViewModel viewModel;
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private ProgressBar progressBar;
    private TextView clientNameTextView;
    private TextView languageSwitchText;
    private ImageView logoView;
    private ImageView clientLogoView;
    private View clientNameCard;

    private int secretClickCount = 0;
    private static final int SECRET_CLICK_THRESHOLD = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        clientNameTextView = findViewById(R.id.clientNameTextView);
        progressBar = findViewById(R.id.progressBar);
        languageSwitchText = findViewById(R.id.languageSwitchText);
        logoView = findViewById(R.id.logoView);
        clientLogoView = findViewById(R.id.clientLogoView);
        clientNameCard = findViewById(R.id.clientNameCard);

        SessionManager sessionManager = new SessionManager(this);

        // شعار Orvix يظل ثابتاً في مكانه
        logoView.setImageResource(R.drawable.ic_logo);

        String logoUrl = sessionManager.getLogoUrl();
        String clientNameStr = sessionManager.getClientName();

        // 1. الإعداد الافتراضي: إظهار اسم الزبون
        clientNameCard.setVisibility(View.VISIBLE);
        clientNameTextView.setText(clientNameStr != null && !clientNameStr.trim().isEmpty() ? clientNameStr : "Client Connected");
        clientLogoView.setVisibility(View.INVISIBLE); // نجعله INVISIBLE بدلاً من GONE لكي يعمل Glide

        if (logoUrl != null && !logoUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(logoUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            // في حال الفشل: نضمن بقاء الاسم وظهور مساحة الشعار مخفية
                            runOnUiThread(() -> {
                                clientNameCard.setVisibility(View.VISIBLE);
                                clientLogoView.setVisibility(View.GONE);
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            // في حال النجاح: نخفي الاسم ونظهر الشعار فوراً
                            runOnUiThread(() -> {
                                clientNameCard.setVisibility(View.GONE);
                                clientLogoView.setVisibility(View.VISIBLE);
                            });
                            return false;
                        }
                    })
                    .into(clientLogoView);
        }

        updateLanguageButtonText(sessionManager.getLanguage());
        languageSwitchText.setOnClickListener(v -> toggleLanguage(sessionManager));

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText() != null ? usernameEditText.getText().toString().trim() : "";
            String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";

            if (username.isEmpty() && password.isEmpty()) {
                handleSecretClick();
            } else {
                secretClickCount = 0; // Reset counter if they are actually trying to login
                viewModel.login(username, password);
            }
        });

        observeLoginState();
    }

    private void handleSecretClick() {
        secretClickCount++;
        if (secretClickCount >= SECRET_CLICK_THRESHOLD) {
            secretClickCount = 0;
            changeClient();
        } else if (secretClickCount > 5) {
            // Optional: Show a subtle hint after 5 clicks so the developer knows it's working
            Toast.makeText(this, (SECRET_CLICK_THRESHOLD - secretClickCount) + " more clicks to reset", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleLanguage(SessionManager sessionManager) {
        String currentLang = sessionManager.getLanguage();
        String newLang = currentLang.equals("ar") ? "en" : "ar";
        sessionManager.setLanguage(newLang);

        // Restart activity to apply language change
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void updateLanguageButtonText(String lang) {
        if (lang.equals("ar")) {
            languageSwitchText.setText("English");
        } else {
            languageSwitchText.setText("العربية");
        }
    }

    private void observeLoginState() {
        viewModel.getLoginState().observe(this, state -> {
            if (state == null) {
                return;
            }

            if (state.getStatus() == Resource.Status.LOADING) {
                setLoading(true);
            } else if (state.getStatus() == Resource.Status.SUCCESS) {
                setLoading(false);
                openTaskList();
            } else if (state.getStatus() == Resource.Status.ERROR) {
                setLoading(false);
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
    }

    private void changeClient() {
        new SessionManager(this).clearClientConfigAndSession();
        Intent intent = new Intent(this, SetupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openTaskList() {
        Intent intent = new Intent(this, TaskListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
