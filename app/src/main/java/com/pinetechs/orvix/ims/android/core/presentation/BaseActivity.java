package com.pinetechs.orvix.ims.android.core.presentation;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.pinetechs.orvix.ims.android.core.util.LocaleHelper;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply the saved language before the activity is created
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
