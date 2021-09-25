package com.ljm.auto;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Switch mSwitch;
    private ServiceConnection mServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isAccessibilitySettingsOn(getPackageName())) {
            Toast.makeText(this, R.string.toast_please_setting, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    private void init() {
        findViewById(R.id.tv_setting).setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        mSwitch = findViewById(R.id.switch1);
        mSwitch.setEnabled(false);
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mSwitch.setText(R.string.tv_close_service);
                MyService.getInstance().startForeground();
            } else {
                mSwitch.setText(R.string.tv_open_service);
                MyService.getInstance().stopForeground();
            }
        });
        Intent intent = new Intent(this, MyService.class);
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mSwitch.setEnabled(true);
                mSwitch.setChecked(MyService.getInstance().isForeground());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    private boolean isAccessibilitySettingsOn(String service) {
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        String settingValue = Settings.Secure.getString(
                getApplicationContext().getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (settingValue != null) {
            mStringColonSplitter.setString(settingValue);
            while (mStringColonSplitter.hasNext()) {
                String accessibilityService = mStringColonSplitter.next();
                if (accessibilityService.contains(service)) {
                    return true;
                }
            }
        }
        return false;
    }
}