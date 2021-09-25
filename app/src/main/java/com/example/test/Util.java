package com.example.test;

import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/***
 *
 * @author ljm
 * @date 2021/9/24
 *
 */
public class Util {

    public static AccessibilityNodeInfo findById(AccessibilityNodeInfo nodeInfo, String id) {
        List<AccessibilityNodeInfo> result = nodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public static AccessibilityNodeInfo findByText(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> result = nodeInfo.findAccessibilityNodeInfosByText(text);
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public static AccessibilityNodeInfo findByClass(AccessibilityNodeInfo nodeInfo, String name) {
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

    public static void findByClassAll(AccessibilityNodeInfo nodeInfo, String name, List<AccessibilityNodeInfo> results) {
        int n = nodeInfo.getChildCount();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                AccessibilityNodeInfo temp = nodeInfo.getChild(i);
                if (temp != null) {
                    if (TextUtils.equals(name, temp.getClassName())) {
                        results.add(temp);
                    }
                    findByClassAll(temp, name, results);
                }
            }
        } else {
            if (TextUtils.equals(name, nodeInfo.getClassName())) {
                results.add(nodeInfo);
            }
        }
    }

    public static void findByClassAllVisable(AccessibilityNodeInfo nodeInfo, String name, List<AccessibilityNodeInfo> results) {
        if (!nodeInfo.isVisibleToUser()) {
            return;
        }
        int n = nodeInfo.getChildCount();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                AccessibilityNodeInfo temp = nodeInfo.getChild(i);
                if (temp != null && temp.isVisibleToUser()) {
                    if (TextUtils.equals(name, temp.getClassName())) {
                        results.add(temp);
                    }
                    findByClassAllVisable(temp, name, results);
                }
            }
        } else {
            if (TextUtils.equals(name, nodeInfo.getClassName())) {
                results.add(nodeInfo);
            }
        }
    }
}
