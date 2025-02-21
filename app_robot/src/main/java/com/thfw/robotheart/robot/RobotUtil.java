package com.thfw.robotheart.robot;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

import androidx.fragment.app.FragmentActivity;

import com.opensource.svgaplayer.SVGAImageView;
import com.thfw.base.ContextApp;
import com.thfw.base.utils.LogUtil;
import com.thfw.base.utils.ToastUtil;
import com.thfw.base.utils.Util;
import com.thfw.robotheart.constants.AnimFileName;
import com.thfw.robotheart.util.DialogRobotFactory;
import com.thfw.ui.voice.tts.TtsHelper;
import com.thfw.ui.voice.tts.TtsModel;

import java.lang.reflect.Method;

/**
 * Author:pengs
 * Date: 2022/5/21 8:39
 * Describe:是否安装在机器人设备上
 * 串口通信相关【严格校验，关于串口通信需要是否安装在 桌面1.0机器人上】
 */
public class RobotUtil {

    private static final String TAG = RobotUtil.class.getSimpleName();
    private static final String SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";

    public static int installRobot = -1;
    /**
     * 长按关机监听
     */
    private static BroadcastReceiver myBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 你自己先把 reasons ==homekey 和 长按homekey 排除，剩下的做下面的处理
            String reason = intent.getStringExtra("reason");
            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {

                LogUtil.i(TAG, "Intent.ACTION_CLOSE_SYSTEM_DIALOGS : " + intent.getStringExtra("reason"));

                if (intent.getExtras() != null && intent.getExtras().getBoolean("myReason")) {

                    myBroadCastReceiver.abortBroadcast();

                } else if (reason != null) {

                    if (reason.equalsIgnoreCase("globalactions")) {
                        // 监听电源长按键的方法：
                        Intent myIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                        myIntent.putExtra("myReason", true);
                        context.sendOrderedBroadcast(myIntent, null);
                        LogUtil.i(TAG, "电源 键被长按====================");
                    } else if (reason.equalsIgnoreCase("homekey")) {
                        //监听Home键的方法
                        //在这里做一些你自己想要的操作,比如重新打开自己的锁屏程序界面，这样子就不会消失了
                        LogUtil.i(TAG, "Home 键被触发");
                    } else if (reason.equalsIgnoreCase("recentapps")) {
                        //监听Home键长按的方法
                        LogUtil.i(TAG, "Home 键被长按");
                    }

                }
            } else if (intent.getAction().equals(SHUTDOWN)) {
                LogUtil.i(TAG, "关机 广播====================");
            }
        }
    };

    /**
     * @return 是否系统应用
     */
    public static boolean isSystemApp() {
        return Util.isSystemApp(ContextApp.get().getPackageName()) || isBuildMsg();
    }

    /**
     * 第一个桌面机器人
     *
     * @return
     */
    public static boolean isBuildMsg() {
        return "TH-ROBOT".equals(Build.PRODUCT) || "TH-RK3399".equals(Build.DEVICE)
                || "RK3399-TH".equals(Build.MODEL) || "ROCKCHIP-TH".equals(Build.BRAND);
    }

    /**
     * 设置系统时区
     */
    public static void setTimeZone(Context context, String timeZone) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setTimeZone(timeZone);
    }

    /**
     * 设置系统时区
     */
    public static void setTimeZoneChina(Context context) {
        setTimeZone(context, "Asia/Shanghai");
    }

    /**
     * 是否桌面1.0机器人
     *
     * @return
     */
    public static boolean isInstallRobot() {
        if (installRobot == -1) {
            installRobot = isBuildMsg() ? 1 : 0;
        }
        return installRobot == 1;
    }

    public static void shutdown(FragmentActivity fragmentActivity) {
        TtsHelper.getInstance().start(new TtsModel("拜拜,下次见哦"), null);
        DialogRobotFactory.createFullSvgaDialog(fragmentActivity, AnimFileName.EMOJI_GUANJI, new DialogRobotFactory.OnSVGACallBack() {
            @Override
            public void callBack(SVGAImageView svgaImageView) {
                shutdown();
            }
        });
    }

    /**
     * 关机
     *
     * @return
     */
    public static boolean shutdown() {
        if (isInstallRobot()) {
            LogUtil.i(TAG, "shutdown() -> 关机");
            return CommandExecution.execCommand("reboot -p", true).result == 1;
        } else {
            return false;
        }
    }


    public static void shutdownByActivity(Context context) {
        String action = "com.android.internal.intent.action.REQUEST_SHUTDOWN";
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            action = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
        }

        Intent shutdown = new Intent(action);
        shutdown.putExtra("android.intent.extra.KEY_CONFIRM", false);
        shutdown.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(shutdown);
            ToastUtil.show("broadcast->shutdown SSSS");
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.show("broadcast->shutdown eEEE");
        }
    }


    public static void shutdownByInvoke() {
        try {
            PowerManager pManager = (PowerManager) ContextApp.get().getSystemService(Context.POWER_SERVICE);
            if (pManager != null) {
                Method method = pManager.getClass().getMethod("shutdown", boolean.class, String.class, boolean.class);
                method.invoke(pManager, false, null, false);
                ToastUtil.show("broadcast->shutdown001");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.show("broadcast->shutdown002");
        }

    }

    /**
     * 长按关机按钮监听
     */
    public static void longPressOffBtn() {
        if (!isInstallRobot()) {
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        ContextApp.get().registerReceiver(myBroadCastReceiver, intentFilter);
        LogUtil.i(TAG, "longPressOffBtn====================");
    }

}
