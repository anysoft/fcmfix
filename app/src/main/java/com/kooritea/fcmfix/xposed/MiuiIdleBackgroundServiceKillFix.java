package com.kooritea.fcmfix.xposed;

import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.kooritea.fcmfix.BuildConfig;
import com.kooritea.fcmfix.util.ContentProviderHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.lang.reflect.Method;
import java.util.Set;

/* loaded from: classes.dex */
public class MiuiIdleBackgroundServiceKillFix extends XposedModule {
    Set<String> allowList;

    public MiuiIdleBackgroundServiceKillFix(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        super(loadPackageParam);
        this.allowList = null;
        startHook();
    }

    public MiuiIdleBackgroundServiceKillFix(XC_LoadPackage.LoadPackageParam loadPackageParam, Set<String> set) {
        super(loadPackageParam);
        this.allowList = null;
        this.allowList = set;
        startHook();
    }

    @Override // com.kooritea.fcmfix.xposed.XposedModule
    protected void onCanReadConfig() throws Exception {
        onUpdateConfig();
        initUpdateConfigReceiver();
    }

    protected void startHook() {
        Method[] declaredMethods;
        Method method = null;
        for (Method method2 : XposedHelpers.findClass("com.android.server.am.ActivityManagerService", this.loadPackageParam.classLoader).getDeclaredMethods()) {
            if (method2.getName().equals("getAppStartModeLocked") && method2.getParameterTypes().length == 9) {
                method = method2;
            }
        }
        if (method != null) {
            XposedBridge.hookMethod(method, new XC_MethodHook() { // from class: com.kooritea.fcmfix.xposed.MiuiIdleBackgroundServiceKillFix.1
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                    String str = (String) methodHookParam.args[7];
                    if (str != null && !str.isEmpty() && MiuiIdleBackgroundServiceKillFix.this.targetIsAllow(str)) {
                        MiuiIdleBackgroundServiceKillFix miuiIdleBackgroundServiceKillFix = MiuiIdleBackgroundServiceKillFix.this;
                        miuiIdleBackgroundServiceKillFix.printLog("getAppStartModeLocked hooked: \n package_name is:" + str + "\n result is:" + methodHookParam.getResult() + " -> 0");
                        methodHookParam.setResult(0);
                    }
                }
            });
        } else {
            printLog("Not found targetMethod_getAppStartModeLocked in com.android.server.am.ActivityManagerService");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean targetIsAllow(String str) {
        if (str.equals(BuildConfig.APPLICATION_ID)) {
            return true;
        }
        if (this.allowList == null) {
            checkUserDeviceUnlock(AndroidAppHelper.currentApplication().getApplicationContext());
        }
        Set<String> set = this.allowList;
        if (set == null) {
            return false;
        }
        for (String str2 : set) {
            if (str2.equals(str)) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onUpdateConfig() {
        this.allowList = new ContentProviderHelper(AndroidAppHelper.currentApplication().getApplicationContext(), "content://com.kooritea.fcmfix.provider/config").getStringSet("allowList");
    }

    private void initUpdateConfigReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.kooritea.fcmfix.update.config");
        AndroidAppHelper.currentApplication().getApplicationContext().registerReceiver(new BroadcastReceiver() { // from class: com.kooritea.fcmfix.xposed.MiuiIdleBackgroundServiceKillFix.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("com.kooritea.fcmfix.update.config".equals(intent.getAction())) {
                    MiuiIdleBackgroundServiceKillFix.this.onUpdateConfig();
                }
            }
        }, intentFilter);
    }
}