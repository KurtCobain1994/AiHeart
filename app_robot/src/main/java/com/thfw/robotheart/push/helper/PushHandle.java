package com.thfw.robotheart.push.helper;

import android.content.Context;
import android.content.Intent;

import com.thfw.base.base.MsgType;
import com.thfw.base.models.AudioEtcModel;
import com.thfw.base.models.PushModel;
import com.thfw.base.models.TalkModel;
import com.thfw.robotheart.R;
import com.thfw.robotheart.activitys.WebActivity;
import com.thfw.robotheart.activitys.audio.AudioPlayerActivity;
import com.thfw.robotheart.activitys.exercise.ExerciseDetailsActivity;
import com.thfw.robotheart.activitys.me.HelpBackActivity;
import com.thfw.robotheart.activitys.talk.AiTalkActivity;
import com.thfw.robotheart.activitys.task.SystemDetailActivity;
import com.thfw.robotheart.activitys.task.TaskActivity;
import com.thfw.robotheart.activitys.test.TestDetailActivity;
import com.thfw.robotheart.activitys.text.BookDetailActivity;
import com.thfw.robotheart.activitys.text.BookIdeoDetailActivity;
import com.thfw.robotheart.activitys.video.VideoPlayerActivity;
import com.thfw.robotheart.util.MsgCountManager;

/**
 * Author:pengs
 * Date: 2022/3/8 18:04
 * Describe:Todo
 */
public class PushHandle {

    public static final void handleMessage(Context mContext, PushModel pushModel) {
        int msgType = pushModel.getMsgType();
        int contentId = pushModel.getContentId();
        String longTextId = null;
        if (pushModel.getReadStatus() == 0) {
            if (pushModel.getId() > 0) {
                MsgCountManager.getInstance().readMsg(msgType, pushModel.getId());
            } else {
                longTextId = String.valueOf(pushModel.getMsgId());
                MsgCountManager.getInstance().readMsg(msgType, pushModel.getMsgId());
            }
        }
        switch (msgType) {
            case MsgType.TASK:
                mContext.startActivity(new Intent(mContext, TaskActivity.class));
                break;
            case MsgType.TESTING:
                TestDetailActivity.startActivity(mContext, contentId);
                break;
            case MsgType.TOOL_PACKAGE:
                ExerciseDetailsActivity.startActivity(mContext, contentId);
                break;
            case MsgType.TOPIC_DIALOG:
                AiTalkActivity.startActivity(mContext, new TalkModel(TalkModel.TYPE_SPEECH_CRAFT)
                        .setId(contentId));
                break;
            case MsgType.MUSIC:
                AudioEtcModel audioEtcModel = new AudioEtcModel();
                audioEtcModel.setId(contentId);
                AudioPlayerActivity.startActivity(mContext, audioEtcModel);
                break;
            case MsgType.VIDEO:
                VideoPlayerActivity.startActivity(mContext, contentId, false);
                break;
            case MsgType.H5:
                WebActivity.startActivity(mContext, pushModel.getTurnPage(), pushModel.getTitle());
                break;
            case MsgType.SYSTEM:

                if (pushModel.getId() > 0) {
                    SystemDetailActivity.startActivity(mContext, pushModel.getId());
                } else {
                    SystemDetailActivity.startActivity(mContext, pushModel.getMsgId());
                }
                break;
            case MsgType.COMMON_PROBLEM:
                HelpBackActivity.startActivity(mContext, 0);
                break;
            case MsgType.VOICE_COMMAND:
                HelpBackActivity.startActivity(mContext, 1);
                break;
            case MsgType.ABOUT_US:
                HelpBackActivity.startActivity(mContext, 2);
                break;
            case MsgType.BOOK:
                BookDetailActivity.startActivity(mContext, contentId);
                break;
            case MsgType.IDEO_BOOK:
                BookIdeoDetailActivity.startActivity(mContext, contentId);
                break;
        }

    }

    public static final Object getTaskIcon(int taskType) {
        /**
         * 1-测评 2-音频 3-话术
         */
        switch (taskType) {
            case 1:
                return R.mipmap.ic_msg_type_test;
            case 2:
                return R.mipmap.ic_msg_type_audio;
            default:
                return R.mipmap.ic_msg_type_talk;
        }
    }

    public static final Object getIcon(int type) {
        Object icon = null;
        switch (type) {
            case MsgType.TASK:
                icon = R.mipmap.ic_launcher;
                break;
            case MsgType.TESTING:
                icon = R.mipmap.ic_msg_type_test;
                break;
            case MsgType.TOOL_PACKAGE:
                icon = R.mipmap.ic_msg_type_tool;
                break;
            case MsgType.TOPIC_DIALOG:
                icon = R.mipmap.ic_msg_type_talk;
                break;
            case MsgType.MUSIC:
                icon = R.mipmap.ic_msg_type_audio;
                break;
            case MsgType.VIDEO:
                icon = R.mipmap.ic_msg_type_video;
                break;
            case MsgType.H5:
                icon = R.drawable.ic_msg_type_link;
                break;
            case MsgType.SYSTEM:
                icon = R.mipmap.ic_msg_type_system;
                break;
            case MsgType.COMMON_PROBLEM:
                icon = R.mipmap.ic_msg_type_problem;
                break;
            case MsgType.VOICE_COMMAND:
                icon = R.mipmap.ic_msg_type_voice;
                break;
            case MsgType.ABOUT_US:
                icon = R.mipmap.ic_msg_type_abut;
                break;
            case MsgType.BOOK:
                icon = R.mipmap.ic_msg_type_book;
                break;
            case MsgType.IDEO_BOOK:
                icon = R.mipmap.ic_msg_type_ideo;
                break;
            default:
                icon = R.mipmap.ic_msg_type_ideo;
                break;
        }
        return icon;
    }


}
