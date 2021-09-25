package com.ljm.auto.handle;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/***
 *
 * @author ljm
 * @date 2021/9/25
 *
 */
public abstract class Handle {
    protected final String TAG = getClass().getSimpleName();
    protected AccessibilityService mService;

    public void bind(AccessibilityService service) {
        mService = service;
    }

    public void unbind() {
        mService = null;
    }

    public AccessibilityNodeInfo getRootInActiveWindow() {
        return mService.getRootInActiveWindow();
    }

    public abstract void handle(AccessibilityEvent event);
}
