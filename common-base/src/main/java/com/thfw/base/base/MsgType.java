package com.thfw.base.base;

/**
 * Author:pengs
 * Date: 2022/3/4 16:22
 * Describe:Todo
 */
public class MsgType {


    public static final int TASK = 1;
    public static final int TESTING = 2;
    public static final int TOOL_PACKAGE = 3;
    public static final int TOPIC_DIALOG = 4;
    public static final int MUSIC = 5;
    public static final int VIDEO = 6;
    public static final int H5 = 7;
    public static final int COMMON_PROBLEM = 8;
    public static final int ABOUT_US = 9;
    public static final int MOOD = 10; // 心情签到
    public static final int VOICE_COMMAND = 11; // 语音指令
    public static final int SYSTEM = 12;
    public static final int BOOK = 13;
    public static final int IDEO_BOOK = 14;
    public static final int SIGN_ONE_YEAR = 15;// 注册满一年


    /**
     * 1 => "task",
     * <p>
     * 2 => "testing",
     * <p>
     * 3 => "tool_package",
     * <p>
     * 4 => "topic_dialog",
     * <p>
     * 5 => "music",
     * <p>
     * 6 => "video",
     * <p>
     * 7 => "H5",
     * <p>
     * 8 => "common_problem",
     * <p>
     * 9 => "about_us",
     * <p>
     * 手机端
     * <p>
     * 10=>"mood"
     * <p>
     * 机器人端
     * <p>
     * 11=>"voice_command"
     * <p>
     * 12=>system
     */

    public static boolean isMobileMsg(int msgType) {
        return VOICE_COMMAND != msgType;
    }

    public static boolean isRobotMsg(int msgType) {
        return MOOD != msgType;
    }

}
