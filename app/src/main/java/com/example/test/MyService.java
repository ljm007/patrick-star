package com.example.test;

import android.accessibilityservice.AccessibilityService;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MyService extends AccessibilityService {
    private static final String TAG = "MyService";
    public static final String PKG = "com.zhihu.android";
    public static final String CLASS_RECYCLEVIEW = "androidx.recyclerview.widget.RecyclerView";
    public static final String CLASS_FRAMELAYOUT = "android.widget.FrameLayout";
    public static final String CLASS_IMAGEVIEW = "android.widget.ImageView";
    public static final String ID_VIEWPAGER = "com.zhihu.android:id/view_pager_container";

    public MyService() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Log.e(TAG, "uncaughtException: ", e));
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        int type = accessibilityEvent.getEventType();
        switch (type) {
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                handleEvent();
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                handleEvent();
                break;
            default:
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void handleEvent() {
        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        if (rootInfo == null || !PKG.contentEquals(rootInfo.getPackageName())) return;
        //找到recycleview的父级
        AccessibilityNodeInfo temp = findById(rootInfo, ID_VIEWPAGER);
        if (temp != null) {
            //找recycleview
            List<AccessibilityNodeInfo> recycleViews = new ArrayList<>();
            findByClassAll(temp, CLASS_RECYCLEVIEW, recycleViews);
            for (AccessibilityNodeInfo recyviewInfo : recycleViews) {
                if (recyviewInfo != null && recyviewInfo.isVisibleToUser()) {
                    Log.d(TAG, "找到当前recycleview");
                    //查找广告
                    List<AccessibilityNodeInfo> guanggaos = findGuangGao(recyviewInfo);
                    if (!guanggaos.isEmpty()) {
                        Log.d(TAG, "有广告");
                        for (AccessibilityNodeInfo guanggao : guanggaos) {
                            try {
                                Log.d(TAG, "关闭广告");
                                guanggao.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                guanggao.recycle();
                            } catch (Exception exception) {
                                Log.e(TAG, "关闭广告出现异常: ", exception);
                            }
                        }
                    }
                    recyviewInfo.recycle();
                }
            }
            temp.recycle();
        }
        rootInfo.recycle();
    }

    private List<AccessibilityNodeInfo> findGuangGao(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> ret = new ArrayList<>();
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo temp = nodeInfo.getChild(i);
            if (TextUtils.equals(CLASS_FRAMELAYOUT, temp.getClassName())) {
                //广告都是帧布局，有多种类型
                try {
                    int n = temp.getChild(0).getChildCount();
                    AccessibilityNodeInfo temp1 = temp.getChild(0).getChild(n - 1);
                    if (temp1 != null &&
                            TextUtils.equals(CLASS_IMAGEVIEW, temp1.getClassName()) &&
                            TextUtils.isEmpty(temp1.getViewIdResourceName())) {
                        ret.add(temp1);
                    }
                } catch (Exception exception) {
                    Log.e(TAG, "findGuangGao: ", exception);
                }
            }
        }
        return ret;
    }

    private AccessibilityNodeInfo findByClass(AccessibilityNodeInfo nodeInfo, String name) {
        int n = nodeInfo.getChildCount();
        if (n > 0) {
            AccessibilityNodeInfo ret = null;
            for (int i = 0; i < n; i++) {
                AccessibilityNodeInfo temp = nodeInfo.getChild(i);
                if (TextUtils.equals(name, temp.getClassName())) {
                    ret = temp;
                    break;
                } else {
                    temp = findByClass(temp, name);
                    if (temp != null) {
                        ret = temp;
                        break;
                    }
                }
            }
            return ret;
        } else {
            if (TextUtils.equals(name, nodeInfo.getClassName())) {
                return nodeInfo;
            } else {
                return null;
            }
        }
    }

    private void findByClassAll(AccessibilityNodeInfo nodeInfo, String name, List<AccessibilityNodeInfo> results) {
        int n = nodeInfo.getChildCount();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                AccessibilityNodeInfo temp = nodeInfo.getChild(i);
                if (temp != null) {
                    if (TextUtils.equals(name, temp.getClassName())) {
                        results.add(temp);
                    } else {
                        findByClassAll(temp, name, results);
                    }
                }
            }
        } else {
            if (TextUtils.equals(name, nodeInfo.getClassName())) {
                results.add(nodeInfo);
            }
        }
    }

    private AccessibilityNodeInfo findById(AccessibilityNodeInfo nodeInfo, String id) {
        List<AccessibilityNodeInfo> result = nodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    private AccessibilityNodeInfo findByText(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> result = nodeInfo.findAccessibilityNodeInfosByText(text);
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }
}