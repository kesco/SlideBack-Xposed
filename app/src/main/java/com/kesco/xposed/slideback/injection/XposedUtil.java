package com.kesco.xposed.slideback.injection;

import de.robv.android.xposed.XposedBridge;

public class XposedUtil {
    private XposedUtil() {
        throw new RuntimeException("The class has no instance.");
    }

    public static void log(String msg, Object... arg) {
        XposedBridge.log(String.format("SlideBack: " + msg, arg));
    }
}
