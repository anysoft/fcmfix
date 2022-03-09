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
public class MiuiProcessManagerServiceFix extends XposedModule {
    Set<String> allowList;

    public MiuiProcessManagerServiceFix(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        super(loadPackageParam);
        this.allowList = null;
        startHook();
    }

    public MiuiProcessManagerServiceFix(XC_LoadPackage.LoadPackageParam loadPackageParam, Set<String> set) {
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
        for (Method method2 : XposedHelpers.findClass("com.android.server.am.ProcessManagerService", this.loadPackageParam.classLoader).getDeclaredMethods()) {
            if (method2.getName().equals("killOnce") && method2.getParameterTypes().length == 4) {
                method = method2;
            }
        }
        if (method != null) {
            XposedBridge.hookMethod(method, new XC_MethodHook() { // from class: com.kooritea.fcmfix.xposed.MiuiProcessManagerServiceFix.1
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                    int intValue = ((Integer) methodHookParam.args[2]).intValue();
                    String str = (String) methodHookParam.args[0].getClass().getField("processName").get(methodHookParam.args[0]);
                    String str2 = (String) methodHookParam.args[1];
                    if (str.equals(BuildConfig.APPLICATION_ID)) {
                        methodHookParam.args[2] = 100;
                    } else if (!MiuiProcessManagerServiceFix.this.targetIsAllow(str)) {
                    } else {
                        if (intValue == 103 || intValue == 104) {
                            methodHookParam.args[2] = 102;
                            MiuiProcessManagerServiceFix miuiProcessManagerServiceFix = MiuiProcessManagerServiceFix.this;
                            miuiProcessManagerServiceFix.printLog( "killOnce hooked: \n package_name is:" + str + "\n reason is:" + str2 + " \n killLevel is:" + intValue + " -> 102");
                        }
                    }
                }
            });
        } else {
            printLog( "Not found targetMethod_killOnce in com.android.server.am.ProcessManagerService");
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
        AndroidAppHelper.currentApplication().getApplicationContext().registerReceiver(new BroadcastReceiver() { // from class: com.kooritea.fcmfix.xposed.MiuiProcessManagerServiceFix.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("com.kooritea.fcmfix.update.config".equals(intent.getAction())) {
                    MiuiProcessManagerServiceFix.this.onUpdateConfig();
                }
            }
        }, intentFilter);
    }
}