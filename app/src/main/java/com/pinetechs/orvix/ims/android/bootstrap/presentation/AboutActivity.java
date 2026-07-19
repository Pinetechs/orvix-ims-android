package com.pinetechs.orvix.ims.android.bootstrap.presentation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.presentation.BaseActivity;
import com.pinetechs.orvix.ims.android.core.util.DeviceUtils;
import com.pinetechs.orvix.ims.android.core.util.VersionUtils;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        TextView versionTv = findViewById(R.id.versionTextView);
        TextView deviceTv = findViewById(R.id.deviceInfoTextView);
        TextView deviceIdTv = findViewById(R.id.deviceIdTextView);

        String version = VersionUtils.getCurrentVersionName(this) + " (" + VersionUtils.getCurrentVersionCode(this) + ")";
        String manufacturer = android.os.Build.MANUFACTURER;
        String model = android.os.Build.MODEL;
        String deviceId = DeviceUtils.getDeviceId(this);

        versionTv.setText(version);
        deviceTv.setText(manufacturer + " " + model);
        deviceIdTv.setText(deviceId);

        findViewById(R.id.websiteTextView).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.pinetechs.com"));
            startActivity(intent);
        });
    }
}
