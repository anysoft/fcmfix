package com.kooritea.fcmfix.xposed;

import android.content.Context;

import com.kooritea.fcmfix.util.XposedUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PowerkeeperFix extends XposedModule {
    public PowerkeeperFix(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        super(loadPackageParam);
        this.startHook();
    }

    protected void startHook(){
        try {
            // hook and modify the SystemProperties of miui.os.Build
            hookSystemPropertiesToGlobal();
            printLog("[fcmfix] start to hook powerkeeper!");
            hookMilletPolicy();
            hookGmsObserver();
            hookGmsCoreUtils();
            hookExtremePowerController();
            hookNetdExecutor();
            printLog("[fcmfix] hook powerkeeper finished!");
        }catch (Exception e){
            printLog("[fcmfix] hook powerkeeper error!");
        }
    }
    protected void hookSystemPropertiesToGlobal() {
        try{
            XposedUtils.findAndHookMethodAnyParam("android.os.SystemProperties",loadPackageParam.classLoader,"get",new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String name = (String)param.args[0];
                    if("ro.product.mod_device".equals(name)){
                        String device = (String)XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.os.SystemProperties", loadPackageParam.classLoader),"get", "ro.product.name");
                        String modDevice = (String)param.getResult();
                        if(!modDevice.endsWith("_global") && !"".equals(device) && device != null){
                            printLog("[powerkeeper]" + device + "_global");
                            param.setResult(device + "_global");
                        }
                    }
                }
            });
            XposedHelpers.setStaticBooleanField(XposedHelpers.findClass("miui.os.Build",loadPackageParam.classLoader), "IS_INTERNATIONAL_BUILD", true);
            XposedHelpers.setStaticBooleanField(XposedHelpers.findClass("miui.os.Build",loadPackageParam.classLoader), "IS_GLOBAL_BUILD", true);
        }catch (XposedHelpers.ClassNotFoundError | NoSuchMethodError  e){
            printLog("No Such Method com.android.server.am.BroadcastQueueInjector.checkApplicationAutoStart", false);
        }
    }
    /***
     * miui 12.5 com.miui.powerkeeper
     */
    protected void hookGmsObserver() {
        printLog("[fcmfix] start to hook hookGmsObserver!");
        /**
         * com.miui.powerkeeper.utils.GmsObserver
         */
        // miui powerkeeper 反向优化
        Class gmsObserverClass = XposedHelpers.findClass("com.miui.powerkeeper.utils.GmsObserver", this.loadPackageParam.classLoader);

        // hook 禁止更新 updateGmsState
        XposedHelpers.findAndHookMethod(gmsObserverClass, "updateGmsState", new Object[]{Boolean.TYPE, new XC_MethodReplacement() {
            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});
        // 禁止更新 updateGmsNetWork
        XposedHelpers.findAndHookMethod(gmsObserverClass, "updateGmsNetWork", new Object[]{Boolean.TYPE, new XC_MethodReplacement() {
            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});
        // 禁止关闭 gms Feedback
        XposedHelpers.findAndHookMethod(gmsObserverClass, "stopGetFeedback", new Object[]{new XC_MethodReplacement() {
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});
        // 禁止 禁用gms
        XposedHelpers.findAndHookMethod(gmsObserverClass, "disableGms", new Object[]{new XC_MethodReplacement() {
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});
//        XposedHelpers.findAndHookMethod(gmsObserverClass, "isGmsAppInstalled", new Object[]{new XC_MethodReplacement() {
//            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                return true;
//            }
//        }});
//        XposedHelpers.findAndHookMethod(gmsObserverClass, "isGmsCoreAppEnabled", new Object[]{new XC_MethodReplacement() {
//            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                return true;
//            }
//        }});
    }

    protected void hookGmsCoreUtils() {
        printLog("[fcmfix] start to hook hookGmsCoreUtils!");
        /**
         * com.miui.powerkeeper.utils.GmsCoreUtils;
         */
        Class gmsCoreUtilsClass = XposedHelpers.findClass("com.miui.powerkeeper.utils.GmsCoreUtils", this.loadPackageParam.classLoader);

        XposedHelpers.findAndHookMethod(gmsCoreUtilsClass, "killGmsCoreProcess", new Object[]{Context.class, new XC_MethodReplacement() {
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});

//        XposedHelpers.findAndHookMethod(gmsCoreUtilsClass, "isGmsCoreApp", new Object[]{String.class, new XC_MethodReplacement() {
//            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                return false;
//            }
//        }});
//
//        XposedHelpers.findAndHookMethod(gmsCoreUtilsClass, "isInstalledGoogleApps", new Object[]{Context.class, new XC_MethodReplacement() {
//            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                return true;
//            }
//        }});
    }

    protected void hookMilletPolicy() {
        printLog("[fcmfix] start to hook hookMilletPolicy!");
        /**
         * com.miui.powerkeeper.millet.MilletPolicy
         */

        final String gms = "com.google.android.gms";
        final String extService = "com.google.android.ext.services";
        final String teams = "com.microsoft.teams";   // microsoft teams
        final  String telegram = "org.telegram.messenger";     // telegram
        final String telegramX = "org.thunderdog.challegram";     // telegram x
        final String qq = "com.tencent.mobileqq";     // qq
        final String wechat = "com.tencent.mm";     // wechat
        //powerkeeper 内置的名单
        //系统黑名单
        List<String> mSystemBlackList = new ArrayList(
                Arrays.asList(
                        "com.miui.gallery",
                        "com.miui.player",
                        "com.android.contacts",
                        "com.android.browser",
                        "com.miui.cloudservice",
                        "com.android.soundrecorder",
                        "com.miui.micloudsync",
                        "com.android.quicksearchbox",
                        "com.miui.hybrid",
                        "com.android.thememanager",
                        "com.xiaomi.misettings",
                        "com.miui.fm",
                        "com.miui.systemAdSolution"
                ));
        //白名单
        List<String> mDataWhiteList = new ArrayList(
                Arrays.asList(
                        teams,
                        telegram,
                        telegramX,
                        "com.google.android.gms",
                        "com.google.android.ext.services",
                        "com.xiaomi.mibrain.speech",
                        "com.miui.virtualsim",
                        "com.xiaomi.xmsf",
                        "com.xiaomi.account",
                        "com.tencent.mobileqq",
                        "com.google.android.tts",
                        "com.xiaomi.aiasst.service",
                        "com.sinovoice.voicebook",
                        "com.tencent.mm",
                        "com.tencent.mobileqq",
                        "com.flyersoft.moonreaderp",
                        "com.wyfc.itingtxt2",
                        "com.gedoor.monkeybook",
                        "com.iflytek.vflynote",
                        "com.flyersoft.seekbooks",
                        "com.flyersoft.moonreader",
                        "com.ss.android.lark.kami",
                        "com.google.android.wearable.app.cn",
                        "com.xiaomi.wearable"
                ));
        List<String> whiteApps = new ArrayList(
                Arrays.asList(
                        teams,
                        telegram,
                        telegramX,
                        "com.google.android.gms",
                        "com.google.android.ext.services",
                        "com.miui.hybrid",
                        "com.miui.player",
                        "com.miui.systemAdSolution",
                        "com.miui.weather2"
                ));
        List<String> musicApp = new ArrayList(
                Arrays.asList(
                        "com.ximalaya.ting.android",
                        "fm.qingting.qtradio",
                        "com.kugou.android",
                        "com.netease.cloudmusic",
                        "com.tencent.qqmusic",
                        "fm.xiami.main"));

        Class milletPolicyClass = XposedHelpers.findClass("com.miui.powerkeeper.millet.MilletPolicy", this.loadPackageParam.classLoader);
        if (null != milletPolicyClass){

            /**
             * 强行修改 static 全局静态变量
             */
            XposedHelpers.setStaticObjectField(milletPolicyClass, "mDataWhiteList", mDataWhiteList);
            XposedHelpers.setStaticObjectField(milletPolicyClass, "mSystemBlackList", mSystemBlackList);
            XposedHelpers.setStaticObjectField(milletPolicyClass, "whiteApps", whiteApps);

            // hook milletPolicy 构造函数
            XposedUtils.findAndHookConstructorAnyParam(milletPolicyClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object milletPolicy = param.thisObject;
                    // static 静态列表修改
                    List<String> mSystemBlackList = (List<String>) XposedHelpers.getStaticObjectField(milletPolicy.getClass(),"mSystemBlackList");
                    List<String> mDataWhiteList = (List<String>) XposedHelpers.getStaticObjectField(milletPolicy.getClass(),"mDataWhiteList");
                    List<String> whiteApps = (List<String>) XposedHelpers.getStaticObjectField(milletPolicy.getClass(),"whiteApps");
                    //遵循国际版标准
                    mSystemBlackList.remove(gms);
//                    whiteApps.remove(gms);
//                    whiteApps.remove(extService);
                    whiteApps.add(teams);
                    whiteApps.add(telegram);
                    whiteApps.add(telegramX);

                    mDataWhiteList.remove(qq);
//                    mDataWhiteList.remove(wechat);
                    mDataWhiteList.add(teams);
                    mDataWhiteList.add(telegram);
                    mDataWhiteList.add(telegramX);

                    // 实例变量
                    List<String> pkgWhiteList = (List<String>) XposedHelpers.getObjectField(milletPolicy, "pkgWhiteList");
                    List<String> pkgBlackList = (List<String>) XposedHelpers.getObjectField(milletPolicy, "pkgBlackList");
                    List<String> pkgGrayList = (List<String>) XposedHelpers.getObjectField(milletPolicy, "pkgGrayList");
                    Set<String> mUseDataWhiteList = (Set<String>) XposedHelpers.getObjectField(milletPolicy, "mUseDataWhiteList");
                    Set<String> mUseSystemBlackList = (Set<String>) XposedHelpers.getObjectField(milletPolicy, "mUseSystemBlackList");

                    //add into pkgWhiteList
                    pkgWhiteList.add(gms);
                    pkgWhiteList.add(extService);
                    pkgWhiteList.add(teams);
                    pkgWhiteList.add(telegram);
                    pkgWhiteList.add(telegramX);

                    //add into mUseDataWhiteList
                    mUseDataWhiteList.add(gms);
                    mUseDataWhiteList.add(extService);
                    mUseDataWhiteList.remove(qq);
//                    mUseDataWhiteList.remove(wechat);

                    mUseDataWhiteList.add(teams);
                    mUseDataWhiteList.add(telegram);
                    mUseDataWhiteList.add(telegramX);

                    //remove from pkgBlackList
                    pkgBlackList.remove(gms);
                    pkgBlackList.remove(extService);

                    //remove from mUseSystemBlackList
                    mUseSystemBlackList.remove(gms);
                    mUseSystemBlackList.remove(extService);
                }
            });
        }
    }

    protected void hookExtremePowerController() {
        printLog("[fcmfix] start to hook hookExtremePowerController!");
        /**
         * com.miui.powerkeeper.statemachine.ExtremePowerController
         * 禁用 gms
         * 锁屏时候禁止通知 disableNotificationOnLockScreen
         */
        Class extremePowerController = XposedHelpers.findClass("com.miui.powerkeeper.statemachine.ExtremePowerController", this.loadPackageParam.classLoader);

        //extremePowerController
        XposedHelpers.findAndHookMethod(extremePowerController, "disableGmsCoreIfNecessary", new Object[]{new XC_MethodReplacement() {
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});
        XposedHelpers.findAndHookMethod(extremePowerController, "disableNotificationOnLockScreen", new Object[]{new XC_MethodReplacement() {
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});
    }

    protected void hookNetdExecutor() {
        printLog("[fcmfix] start to hook hookNetdExecutor!");
        /**
         *   com.miui.powerkeeper.utils.NetdExecutor
         *   iptables 限制 gms 网络和 dns 解析
         */
        Class netdExecutor = XposedHelpers.findClass("com.miui.powerkeeper.utils.NetdExecutor", this.loadPackageParam.classLoader);
        //netdExecutor
        XposedHelpers.findAndHookMethod(netdExecutor, "initGmsChain", new Object[]{String.class, Integer.TYPE, String.class, new XC_MethodReplacement() {
            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});
        XposedHelpers.findAndHookMethod(netdExecutor, "setGmsChainState", new Object[]{String.class, Boolean.TYPE, new XC_MethodReplacement() {
            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});
        XposedHelpers.findAndHookMethod(netdExecutor, "setGmsDnsBlockerState", new Object[]{Integer.TYPE, Boolean.TYPE, new XC_MethodReplacement() {
            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});
    }
}
