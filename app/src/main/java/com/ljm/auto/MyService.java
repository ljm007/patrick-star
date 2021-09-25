package com.ljm.auto;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.ljm.auto.handle.Handle;
import com.ljm.auto.handle.ZhihuHandle;

import java.util.ArrayList;
import java.util.List;

public class MyService extends AccessibilityService {

    public static final int ONGOING_NOTIFICATION_ID = 1111;
    private static MyService mInstance;
    private final String TAG = "MyService";
    private final List<Handle> handles = new ArrayList<>();
    private Notification mNotification;
    private boolean isForeground;

    public MyService() {
        mInstance = this;
        handles.add(new ZhihuHandle());
    }

    public static MyService getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        handles.forEach(handle -> handle.bind(this));
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "my_channel_01";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "服务",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(getString(R.string.channel_description));
        manager.createNotificationChannel(channel);
        mNotification = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent)
                .setContentTitle(getString(R.string.notify_title))
                .setContentText(getString(R.string.notify_message))
                .setTicker(getString(R.string.notify_message))
                .build();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        handles.forEach(Handle::unbind);
        stopForeground(true);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        handles.forEach(handle -> handle.handle(accessibilityEvent));
    }

    @Override
    public void onInterrupt() {
    }

    public boolean isForeground() {
        return isForeground;
    }

    public void startForeground() {
        isForeground = true;
        startForeground(ONGOING_NOTIFICATION_ID, mNotification);
    }

    public void stopForeground() {
        isForeground = false;
        stopForeground(true);
    }
}