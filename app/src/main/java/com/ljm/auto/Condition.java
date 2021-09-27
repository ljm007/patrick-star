package com.ljm.auto;

import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;

public class Condition<T> {

    public static Check<String> classCheck = (nodeInfo, token) -> nodeInfo != null && TextUtils.equals(nodeInfo.getClassName(), token);
    public static Check<String> idCheck = (nodeInfo, token) -> nodeInfo != null && TextUtils.equals(nodeInfo.getViewIdResourceName(), token);
    public static Check<String> textCheck = (nodeInfo, token) -> nodeInfo != null && TextUtils.equals(nodeInfo.getText(), token);
    public static Check<String> descCheck = (nodeInfo, token) -> nodeInfo != null && TextUtils.equals(nodeInfo.getContentDescription(), token);
    public static Check<String> visibleCheck = (nodeInfo, token) -> nodeInfo != null && nodeInfo.isVisibleToUser();
    public static Check<String> viewGroupCheck = (nodeInfo, token) -> nodeInfo != null && nodeInfo.getChildCount() > 0;
    public static Check<String> canClickCheck = (nodeInfo, token) -> nodeInfo != null && nodeInfo.isClickable();

    private T mToken;
    private Check<T> mCheck;

    public Condition(T mToken, Check<T> mCheck) {
        this.mToken = mToken;
        this.mCheck = mCheck;
    }

    public boolean check(AccessibilityNodeInfo nodeInfo) {
        return mCheck.check(nodeInfo, mToken);
    }

    public static interface Check<T> {
        boolean check(AccessibilityNodeInfo nodeInfo, T token);
    }

}

