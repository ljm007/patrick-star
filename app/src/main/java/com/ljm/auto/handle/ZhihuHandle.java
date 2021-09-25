package com.ljm.auto.handle;

import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.ljm.auto.Util;

import java.util.ArrayList;
import java.util.List;

/***
 *
 * @author ljm
 * @date 2021/9/25
 *
 * 自动关闭知乎广告
 */
public class ZhihuHandle extends Handle {

    private static final String PKG = "com.zhihu.android";
    private static final String CLASS_RECYCLEVIEW = "androidx.recyclerview.widget.RecyclerView";
    private static final String CLASS_VIEW = "android.view.View";
    private static final String CLASS_FRAMELAYOUT = "android.widget.FrameLayout";
    private static final String CLASS_IMAGEVIEW = "android.widget.ImageView";
    private static final String CLASS_IMAGE = "android.widget.Image";
    private static final String ID_VIEWPAGER = "com.zhihu.android:id/view_pager_container";
    private static final String ID_WEBVIEW_CONTAINER = "com.zhihu.android:id/mix_container";
    private static final String ID_SKIP = "com.zhihu.android:id/btn_skip";

    @Override
    public void handle(AccessibilityEvent event) {
        int type = event.getEventType();
        Log.d(TAG, "handle() called with: event = [" + event + "]");
        switch (type) {
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                skipGuangGao();
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                skipKaiPingGuangGao();
                break;
            default:
                break;
        }
    }

    private void skipKaiPingGuangGao() {
        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        if (rootInfo == null || !PKG.contentEquals(rootInfo.getPackageName())) return;
        AccessibilityNodeInfo temp = Util.findById(rootInfo, ID_SKIP);
        Log.d(TAG, "skipKaiPingGuangGao: " + temp);
        if (temp != null) {
            temp.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    private void skipGuangGao() {
        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        if (rootInfo == null || !PKG.contentEquals(rootInfo.getPackageName())) return;
        //找到recycleview的父级
        AccessibilityNodeInfo temp = Util.findById(rootInfo, ID_VIEWPAGER);
        if (temp != null) {
            //找recycleview
            Log.d(TAG, "skipGuangGao: find recycleview");
            List<AccessibilityNodeInfo> recycleViews = new ArrayList<>();
            Util.findByClassAllVisable(temp, CLASS_RECYCLEVIEW, recycleViews);
            for (AccessibilityNodeInfo recyviewInfo : recycleViews) {
                if (recyviewInfo != null) {
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
                }
            }
            temp.recycle();
        } else {
            boolean haveGuangGao = false;
            List<AccessibilityNodeInfo> images = new ArrayList<>();
            Util.findByClassAllVisable(rootInfo, CLASS_IMAGE, images);
            Log.d(TAG, "skipGuangGao: images " + images.size());
            for (AccessibilityNodeInfo image : images) {
                if (image != null && image.isClickable()) {
                    haveGuangGao = true;
                    image.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    image.recycle();
                }
            }
            if (!haveGuangGao) {
                images.clear();
                Util.findByClassAllVisable(rootInfo, CLASS_VIEW, images);
                if (images != null && !images.isEmpty()) {
                    for (AccessibilityNodeInfo view : images) {
                        if (view != null
                                && TextUtils.equals("×", view.getText())
                                && view.isClickable()) {
                            view.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            view.recycle();
                        }
                    }
                }
            }
        }
        rootInfo.recycle();
    }

    private List<AccessibilityNodeInfo> findGuangGao(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> ret = new ArrayList<>();
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo temp = nodeInfo.getChild(i);
            if (temp == null) break;
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
}
