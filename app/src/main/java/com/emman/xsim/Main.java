package com.emman.xsim;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.emman.xsim.helpers.Utils;
import com.emman.xsim.helpers.Resources;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.io.File;
import java.lang.reflect.Constructor;

public class Main implements IXposedHookZygoteInit, Resources {

    private static boolean canSetDefaultDataSubId = false;
    private XC_MethodHook addSubInfoRecordHook;
    private XC_MethodHook setDefaultDataSubIdHook;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    final Class<?> SubscriptionController = findClass(CLASS_SUBSCRIPTION_CONTROLLER, null);

	addSubInfoRecordHook = new XC_MethodHook() {
	    @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
		canSetDefaultDataSubId = false;
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
		canSetDefaultDataSubId = true;
            }
	};

	setDefaultDataSubIdHook = new XC_MethodHook() {
	    @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
		if(!canSetDefaultDataSubId){
			param.setResult(null);
		}
            }
	};

	findAndHookMethod(SubscriptionController, "addSubInfoRecord", String.class, int.class, addSubInfoRecordHook);

	findAndHookMethod(SubscriptionController, "setDefaultDataSubId", int.class, setDefaultDataSubIdHook);

    }

    public static Context getContext() {
	Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
	return (Context) callMethod(activityThread, "getSystemContext");
    }
}
