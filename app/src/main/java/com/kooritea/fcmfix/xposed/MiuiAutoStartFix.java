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
public class MiuiAutoStartFix extends XposedModule {
    Set<String> allowList;

    public MiuiAutoStartFix(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        super(loadPackageParam);
        this.allowList = null;
        startHook();
    }

    public MiuiAutoStartFix(XC_LoadPackage.LoadPackageParam loadPackageParam, Set<String> set) {
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
        Method method;
        Method[] declaredMethods = XposedHelpers.findClass("com.android.server.am.BroadcastQueueInjector", this.loadPackageParam.classLoader).getDeclaredMethods();
        int length = declaredMethods.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                method = null;
                break;
            }
            method = declaredMethods[i];
            if (method.getName().equals("checkApplicationAutoStart")) {
                break;
            }
            i++;
        }
        if (method != null) {
            XposedBridge.hookMethod(method, new XC_MethodHook() { // from class: com.kooritea.fcmfix.xposed.MiuiAutoStartFix.1
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                    String str;
                    Intent intent = (Intent) XposedHelpers.getObjectField(methodHookParam.args[2], "intent");
                    if ("com.google.android.c2dm.intent.RECEIVE".equals(intent.getAction())) {
                        if (intent.getComponent() != null) {
                            str = intent.getComponent().getPackageName();
                        } else {
                            str = intent.getPackage();
                        }
                        if (MiuiAutoStartFix.this.targetIsAllow(str)) {
                            MiuiAutoStartFix miuiAutoStartFix = MiuiAutoStartFix.this;
                            miuiAutoStartFix.printLog( "Allow Auto Start: " + str);
                            methodHookParam.setResult(true);
                        }
                    }
                }
            });
        } else {
            printLog( "Not found checkApplicationAutoStart in com.android.server.am.BroadcastQueueInjector");
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
        AndroidAppHelper.currentApplication().getApplicationContext().registerReceiver(new BroadcastReceiver() { // from class: com.kooritea.fcmfix.xposed.MiuiAutoStartFix.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("com.kooritea.fcmfix.update.config".equals(intent.getAction())) {
                    MiuiAutoStartFix.this.onUpdateConfig();
                }
            }
        }, intentFilter);
    }
}