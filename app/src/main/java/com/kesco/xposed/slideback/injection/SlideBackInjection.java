package com.kesco.xposed.slideback.injection;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.kesco.adk.moko.slideback.SlideEdge;
import com.kesco.adk.moko.slideback.SlideLayout;
import com.kesco.adk.moko.slideback.SlideListener;
import com.kesco.adk.moko.slideback.SlideState;
import com.kesco.adk.moko.slideback.SlidebackPackage;
import com.kesco.xposed.slideback.domain.AppInfo;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SlideBackInjection implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {
    private static final String FILTER_PREFIX_ANDROID = "android";
    private static final String FILTER_PREFIX_COM_ANDROID = "com.android";

    private static String modPath;

    private XSharedPreferences pref = null;

    public SlideBackInjection() {
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        modPath = startupParam.modulePath;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (filterSystemApp(lpparam.packageName) || !filterSlideApp(lpparam.packageName)) {
            return;
        }

        AppInfo app = loadAppInfo(lpparam.packageName);
        Set<String> activities = app.getAvaliableSlideActivities();
        XC_MethodHook onCreateHookCallBack = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                attachSlideLayout((Activity) param.thisObject);
            }
        };

        for (String act : activities) {
            XposedHelpers.findAndHookMethod(act, lpparam.classLoader, "onCreate", Bundle.class, onCreateHookCallBack);
        }

        XposedHelpers.findAndHookMethod("android.app.Activity", lpparam.classLoader, "onPostCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity act = (Activity) param.thisObject;
                SlidebackPackage.convertActivityToTranslucent(act);
            }
        });
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (filterSystemApp(resparam.packageName) || !filterSlideApp(resparam.packageName)) {
            return;
        }
        XResources res = resparam.res;
        XModuleResources modRes = XModuleResources.createInstance(modPath, res);
        int resId;

        resId = com.kesco.adk.moko.slideback.R.color.start_shadow_color;
        res.setReplacement(resId, modRes.fwd(resId));
        resId = com.kesco.adk.moko.slideback.R.color.center_shadow_color;
        res.setReplacement(resId, modRes.fwd(resId));
        resId = com.kesco.adk.moko.slideback.R.color.end_shadow_color;
        res.setReplacement(resId, modRes.fwd(resId));
        resId = com.kesco.adk.moko.slideback.R.dimen.shadow_width;
        res.setReplacement(resId, modRes.fwd(resId));
        resId = com.kesco.adk.moko.slideback.R.id.slide_view;
        res.setReplacement(resId, modRes.fwd(resId));
    }

    private void attachSlideLayout(final Activity act) {
        Window win = act.getWindow();
        ViewGroup decorView = (ViewGroup) win.getDecorView();
        Drawable bg = decorView.getBackground();
        win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        decorView.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View screenView = decorView.getChildAt(0);
        decorView.removeViewAt(0);
        SlideLayout slideLayout = new SlideLayout(act, screenView);
        slideLayout.addView(screenView);
        decorView.addView(slideLayout, 0);
        screenView.setBackgroundDrawable(bg);
        slideLayout.setSlideEdge(SlideEdge.LEFT);
        slideLayout.setListener(new SlideListener() {
            @Override
            public void onSlideStart() {
            }

            @Override
            public void onSlide(float percent, @NotNull SlideState state) {
            }

            @Override
            public void onSlideFinish() {
                XposedBridge.log(act.getClass().getSimpleName() + " : Finish");
                act.finish();
                act.overridePendingTransition(0, 0);
            }
        });
        SlidebackPackage.convertActivityFromTranslucent(act);
    }

    private Set<String> loadSlideAppStrList() {
        return getPref().getStringSet("slide_app_list", new HashSet<String>());
    }

    private XSharedPreferences getPref() {
        if (pref == null) {
            pref = new XSharedPreferences("com.kesco.xposed.slideback", "app_settings");
            pref.makeWorldReadable();
        } else {
            pref.reload();
        }
        return pref;
    }

    private boolean filterSystemApp(String app) {
        boolean ret = false;
        String subStr;
        if (app.length() >= FILTER_PREFIX_ANDROID.length()) {
            subStr = app.substring(0, FILTER_PREFIX_ANDROID.length());
            ret = subStr.equals(FILTER_PREFIX_ANDROID);
        } else if (app.length() >= FILTER_PREFIX_COM_ANDROID.length()) {
            subStr = app.substring(0, FILTER_PREFIX_COM_ANDROID.length());
            ret = subStr.equals(FILTER_PREFIX_COM_ANDROID);
        }
        return ret;
    }

    private boolean filterSlideApp(String app) {
        boolean ret = false;
        for (String item : loadSlideAppStrList()) {
            if (ret = app.equals(item)) break;
        }
        return ret;
    }

    private AppInfo loadAppInfo(String packageName) {
        String prefName = packageName.replace(".", "_");
        SharedPreferences pref = new XSharedPreferences("com.kesco.xposed.slideback", prefName);
        String name = pref.getString("name", "");
        String pack = pref.getString("pack", "");
        boolean doSlide = pref.getBoolean("do_slide", false);
        AppInfo app = new AppInfo(name, pack, null, doSlide);
        app.setAvaliableSlideActivities(pref.getStringSet("avaliable_slide_activities", new HashSet<String>()));

        return app;
    }
}
