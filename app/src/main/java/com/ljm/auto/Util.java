package com.ljm.auto;

import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/***
 *
 * @author ljm
 * @date 2021/9/24
 *
 */
public class Util {
    private static final String TAG = "Util";

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

    public static AccessibilityNodeInfo findFirst(AccessibilityNodeInfo nodeInfo, Condition... conditions) {
        if (nodeInfo == null) return null;
        boolean check = Arrays.stream(conditions).allMatch(condition -> condition.check(nodeInfo));
        if (check) {
            return nodeInfo;
        } else {
            int n = nodeInfo.getChildCount();
            if (n > 0) {
                for (int i = 0; i < n; i++) {
                    AccessibilityNodeInfo temp = nodeInfo.getChild(i);
                    temp = findFirst(temp, conditions);
                    if (temp != null) {
                        return temp;
                    }
                }
            }
            return null;
        }
    }

    public static void findAll(List<AccessibilityNodeInfo> results, AccessibilityNodeInfo nodeInfo, Condition... conditions) {
        if (nodeInfo == null) return;
        boolean check = Arrays.stream(conditions).allMatch(condition -> condition.check(nodeInfo));
        if (check) {
            results.add(nodeInfo);
        } else {
            int n = nodeInfo.getChildCount();
            if (n > 0) {
                for (int i = 0; i < n; i++) {
                    AccessibilityNodeInfo temp = nodeInfo.getChild(i);
                    findAll(results, temp, conditions);
                }
            }
        }
    }

    public static AccessibilityNodeInfo findFirstVisable(AccessibilityNodeInfo nodeInfo, Condition... conditions) {
        if (nodeInfo == null || !nodeInfo.isVisibleToUser()) return null;
        boolean check = Arrays.stream(conditions).allMatch(condition -> condition.check(nodeInfo));
        if (check) {
            return nodeInfo;
        } else {
            int n = nodeInfo.getChildCount();
            if (n > 0) {
                for (int i = 0; i < n; i++) {
                    AccessibilityNodeInfo temp = nodeInfo.getChild(i);
                    temp = findFirstVisable(temp, conditions);
                    if (temp != null) {
                        return temp;
                    }
                }
            }
            return null;
        }
    }

    public static void findAllVisable(List<AccessibilityNodeInfo> results, AccessibilityNodeInfo nodeInfo, Condition... conditions) {
        if (nodeInfo == null || !nodeInfo.isVisibleToUser()) return;
        boolean check = Arrays.stream(conditions).allMatch(condition -> condition.check(nodeInfo));
        if (check) {
            results.add(nodeInfo);
        } else {
            int n = nodeInfo.getChildCount();
            if (n > 0) {
                for (int i = 0; i < n; i++) {
                    AccessibilityNodeInfo temp = nodeInfo.getChild(i);
                    findAllVisable(results, temp, conditions);
                }
            }
        }
    }

    public static List<AccessibilityNodeInfo> findAllByList(List<AccessibilityNodeInfo> intputs, Condition... conditions) {
        return intputs.stream()
                .filter(nodeInfo -> Arrays.stream(conditions).allMatch(condition -> condition.check(nodeInfo)))
                .collect(Collectors.toList());
    }

    public static List<AccessibilityNodeInfo> toSubViewList(AccessibilityNodeInfo nodeInfo) {
        return Stream.iterate(0, i -> i + 1)
                .limit(nodeInfo.getChildCount())
                .map(nodeInfo::getChild)
                .collect(Collectors.toList());
    }

    public static void performAction(AccessibilityNodeInfo nodeInfo, int action) {
        try {
            Log.d(TAG, "performAction() called with: nodeInfo = [" + nodeInfo + "], action = [" + action + "]");
            nodeInfo.performAction(action);
            nodeInfo.recycle();
        } catch (Exception e) {
            Log.e(TAG, "执行辅助动作出错 performAction: ", e);
        }
    }

    public static boolean containIds(AccessibilityNodeInfo nodeInfo, String... ids) {
        return Arrays.stream(ids).noneMatch(s -> nodeInfo.findAccessibilityNodeInfosByViewId(s).isEmpty());
    }
}
