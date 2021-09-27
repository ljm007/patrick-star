package com.ljm.auto.handle;

import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.ljm.auto.Condition;
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
    private static final String ID_SKIP = "com.zhihu.android:id/btn_skip";

    private static final String ID_ANSWER_SCREEN0 = "com.zhihu.android:id/view_content";//回答页面才有的id
    private static final String ID_ANSWER_SCREEN1 = "com.zhihu.android:id/mix_container";

    private Condition<String> View_CLASS_CONDITION = new Condition<>(CLASS_VIEW, Condition.classCheck);
    private Condition<String> Recycleview_CLASS_CONDITION = new Condition<>(CLASS_RECYCLEVIEW, Condition.classCheck);
    private Condition<String> Framelayout_CLASS_CONDITION = new Condition<>(CLASS_FRAMELAYOUT, Condition.classCheck);
    private Condition<String> ImageView_CLASS_CONDITION = new Condition<>(CLASS_IMAGEVIEW, Condition.classCheck);
    private Condition<String> Image_CLASS_CONDITION = new Condition<>(CLASS_IMAGE, Condition.classCheck);
    private Condition<String> ViewPage_ID_CONDITION = new Condition<>(ID_VIEWPAGER, Condition.idCheck);
    private Condition<String> Skip_ID_CONDITION = new Condition<>(ID_SKIP, Condition.idCheck);
    private Condition<String> Visible_CONDITION = new Condition<>("", Condition.visibleCheck);
    private Condition<String> ViewGroup_CONDITION = new Condition<>("", Condition.viewGroupCheck);
    private Condition<String> Click_CONDITION = new Condition<>("", Condition.canClickCheck);
    private Condition<String> X_CONDITION = new Condition<>("×", Condition.textCheck);


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
        if (temp != null) {
            temp.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    private void skipGuangGao() {
        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        if (rootInfo == null || !PKG.contentEquals(rootInfo.getPackageName())) return;
        //找viewPager
        AccessibilityNodeInfo viewPagerInfo = Util.findById(rootInfo, ID_VIEWPAGER);
        if (viewPagerInfo != null) {
            //找recycleview
            List<AccessibilityNodeInfo> recycleViews = new ArrayList<>();
//            Util.findAllVisable(recycleViews, viewPagerInfo, Recycleview_CLASS_CONDITION);
            Util.findByClassAllVisable(viewPagerInfo, CLASS_RECYCLEVIEW, recycleViews);
            Log.d(TAG, "skipGuangGao: recycleViews " + recycleViews.size());
            for (AccessibilityNodeInfo recyviewInfo : recycleViews) {
                if (recyviewInfo != null) {
                    //查找广告
                    List<AccessibilityNodeInfo> guanggaos = findGuangGao(recyviewInfo);
                    //关闭广告
                    guanggaos.forEach(nodeInfo -> Util.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_CLICK));
                }
            }
            viewPagerInfo.recycle();
        } else if (Util.containIds(rootInfo, ID_ANSWER_SCREEN0, ID_ANSWER_SCREEN1)) {
            List<AccessibilityNodeInfo> images = new ArrayList<>();
            Util.findAllVisable(images, rootInfo, Image_CLASS_CONDITION, Click_CONDITION);
            if (images.isEmpty()) {
                Util.findAllVisable(images, rootInfo, View_CLASS_CONDITION, Click_CONDITION, X_CONDITION);
            }
            images.forEach(nodeInfo -> Util.performAction(nodeInfo, AccessibilityNodeInfo.ACTION_CLICK));
        }
        rootInfo.recycle();
    }

    private List<AccessibilityNodeInfo> findGuangGao(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> ret = new ArrayList<>();
        for (AccessibilityNodeInfo temp : Util.findAllByList(Util.toSubViewList(nodeInfo), Framelayout_CLASS_CONDITION)) {
            try {
                //广告都是帧布局，最后的view是imageview并且没有id的就是广告关闭按钮
                int n = temp.getChild(0).getChildCount();
                AccessibilityNodeInfo temp1 = temp.getChild(0).getChild(n - 1);
                if (ImageView_CLASS_CONDITION.check(temp1) &&
                        TextUtils.isEmpty(temp1.getViewIdResourceName())) {
                    ret.add(temp1);
                }
            } catch (Exception exception) {
                Log.e(TAG, "findGuangGao: ", exception);
            }

        }
        return ret;
    }
}
