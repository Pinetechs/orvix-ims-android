package com.pinetechs.orvix.ims.android.core.presentation;

import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.pinetechs.orvix.ims.android.core.util.LocaleHelper;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // منع الشاشة من الانطفاء طالما التطبيق مفتوح
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply the saved language before the activity is created
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
