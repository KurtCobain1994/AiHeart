package com.thfw.mobileheart.activity;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.opensource.svgaplayer.SVGAImageView;
import com.thfw.base.base.IPresenter;
import com.thfw.base.face.SimpleUpgradeStateListener;
import com.thfw.base.models.MoodLivelyModel;
import com.thfw.base.models.OrganizationModel;
import com.thfw.base.models.OrganizationSelectedModel;
import com.thfw.base.models.TalkModel;
import com.thfw.base.net.BaseCodeListener;
import com.thfw.base.net.CommonParameter;
import com.thfw.base.net.OkHttpUtil;
import com.thfw.base.net.ResponeThrowable;
import com.thfw.base.presenter.OrganizationPresenter;
import com.thfw.base.presenter.UserInfoPresenter;
import com.thfw.base.timing.TimingHelper;
import com.thfw.base.timing.WorkInt;
import com.thfw.base.utils.BuglyUtil;
import com.thfw.base.utils.EmptyUtil;
import com.thfw.base.utils.HandlerUtil;
import com.thfw.base.utils.LogUtil;
import com.thfw.base.utils.SharePreferenceUtil;
import com.thfw.base.utils.ToastUtil;
import com.thfw.mobileheart.MyApplication;
import com.thfw.mobileheart.R;
import com.thfw.mobileheart.activity.login.LoginActivity;
import com.thfw.mobileheart.activity.mood.StatusActivity;
import com.thfw.mobileheart.activity.service.AutoUpdateService;
import com.thfw.mobileheart.activity.talk.ChatActivity;
import com.thfw.mobileheart.constants.AnimFileName;
import com.thfw.mobileheart.fragment.HomeFragment;
import com.thfw.mobileheart.fragment.MeFragment;
import com.thfw.mobileheart.fragment.MessageFragment;
import com.thfw.mobileheart.push.MyPreferences;
import com.thfw.mobileheart.push.helper.PushHelper;
import com.thfw.mobileheart.push.tester.UPushAlias;
import com.thfw.mobileheart.util.ActivityLifeCycle;
import com.thfw.mobileheart.util.DialogFactory;
import com.thfw.mobileheart.util.FragmentLoader;
import com.thfw.mobileheart.util.MoodLivelyHelper;
import com.thfw.mobileheart.util.MsgCountManager;
import com.thfw.mobileheart.view.SimpleAnimatorListener;
import com.thfw.ui.dialog.TDialog;
import com.thfw.ui.dialog.base.BindViewHolder;
import com.thfw.ui.dialog.listener.OnViewClickListener;
import com.thfw.ui.voice.tts.TtsHelper;
import com.thfw.ui.voice.tts.TtsModel;
import com.thfw.user.login.LoginStatus;
import com.thfw.user.login.UserManager;
import com.thfw.user.login.UserObserver;
import com.thfw.user.models.User;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.umeng.message.PushAgent;
import com.umeng.message.api.UPushRegisterCallback;

import java.util.ArrayList;
import java.util.List;


/**
 * 首页
 * Created By jishuaipeng on 2021/7/02
 */
public class MainActivity extends BaseActivity implements Animator.AnimatorListener, MsgCountManager.OnCountChangeListener {

    // 心情打卡今日第一次 记录
    private static final String KEY_MOOD_HINT = "key.mood.first";
    private static MainActivity mainActivity;
    /**
     * 机构信息和用户信息初始化标识
     */
    private static boolean initUserInfo;
    private static boolean initOrganization;
    private static boolean initUmeng;
    private static boolean initTts;
    private static boolean moodHint;
    // 登录动画是否显示，为了不频繁获取sp数据
    private static boolean showLoginAnim;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private androidx.constraintlayout.widget.ConstraintLayout mMainRoot;
    private androidx.constraintlayout.widget.ConstraintLayout mMainRoot2;
    private android.widget.LinearLayout mLlHome;
    private android.widget.ImageView mIvHome;
    private android.widget.TextView mTvHome;
    private android.widget.LinearLayout mLlMessage;
    private android.widget.ImageView mIvMessage;
    private android.widget.TextView mTvMessage;
    private android.widget.LinearLayout mLlMe;
    private android.widget.ImageView mIvMe;
    private android.widget.TextView mTvMe;
    private FragmentLoader mFragmentLoader;
    private View mCurrent;
    private LinearLayout mLlAiChat;
    private TextView mTvMsgCount;
    private TextView mTvMsgVersion;
    private Runnable checkVersionRunnable = new Runnable() {
        @Override
        public void run() {
            checkVersion();
        }
    };
    private boolean trueResume;
    private long exitTime = 0;

    public static void setShowLoginAnim(boolean showLoginAnim) {
        MainActivity.showLoginAnim = showLoginAnim;
    }

    /**
     * 重新登录后重新获取用户相关信息
     */
    public static void resetInit() {
        initUserInfo = false;
        initOrganization = false;
        initUmeng = false;
        moodHint = false;
        initTts = false;
    }

    private static boolean hasAgreedAgreement() {
        return MyPreferences.getInstance(MyApplication.getApp()).hasAgreePrivacyAgreement();
    }

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    public static void finishMain() {
        if (mainActivity != null) {
            mainActivity.finish();
            mainActivity = null;
        }
    }

    public static boolean isFinished() {
        return EmptyUtil.isEmpty(mainActivity);
    }

    @Override
    public int getContentView() {
        return R.layout.activity_home;
    }

    @Override
    public IPresenter onCreatePresenter() {
        return null;
    }

    @Override
    public void initView() {
        mainActivity = this;
        OkHttpUtil.setBaseCodeListener(code -> {
            if (code == BaseCodeListener.LOGOUT) {
                if (UserManager.getInstance().isTrueLogin()) {
                    ToastUtil.show(R.string.token_not_valid);
                    UserManager.getInstance().logout(LoginStatus.LOGOUT_EXIT);
                    if (isMeResumed()) {
                        LoginActivity.startActivity(mContext, LoginActivity.BY_OTHER);
                    } else {
                        MyApplication.goAppHome((Activity) mContext);
                    }
                }
            }
        });
        mMainRoot = (ConstraintLayout) findViewById(R.id.main_root);
        mMainRoot2 = (ConstraintLayout) findViewById(R.id.main_root2);

        mLlHome = (LinearLayout) findViewById(R.id.ll_home);
        mIvHome = (ImageView) findViewById(R.id.iv_home);
        mTvHome = (TextView) findViewById(R.id.tv_home);

        mTvMsgCount = findViewById(R.id.tv_massage_count);
        mTvMsgVersion = findViewById(R.id.tv_massage_version);

        mLlAiChat = (LinearLayout) findViewById(R.id.ll_ai_chat);
        mLlMessage = (LinearLayout) findViewById(R.id.ll_message);
        mIvMessage = (ImageView) findViewById(R.id.iv_message);
        mTvMessage = (TextView) findViewById(R.id.tv_message);

        mLlMe = (LinearLayout) findViewById(R.id.ll_me);
        mIvMe = (ImageView) findViewById(R.id.iv_me);
        mTvMe = (TextView) findViewById(R.id.tv_me);
        // 智能聊天/倾诉吐槽
        mLlAiChat.setOnClickListener(v -> {
            DialogFactory.createSvgaDialog(MainActivity.this,
                    AnimFileName.TRANSITION_TALK,
                    new DialogFactory.OnSVGACallBack() {
                        @Override
                        public void callBack(SVGAImageView svgaImageView) {
                            ChatActivity.startActivity(mContext, new TalkModel(TalkModel.TYPE_AI));
                        }
                    });

        });

    }

    @Override
    public void initData() {

        mFragmentLoader = new FragmentLoader(getSupportFragmentManager(), R.id.fl_content)
                .add(mLlHome.getId(), new HomeFragment())
                .add(mLlMessage.getId(), new MessageFragment())
                .add(mLlMe.getId(), new MeFragment());


        View.OnClickListener mOnTabListener = v -> {
            if (mCurrent == v) {
                return;
            }
            mFragmentLoader.load(v.getId());
            setTab((LinearLayout) v, true);
            setTab((LinearLayout) mCurrent, false);
            mCurrent = v;
        };
        mLlHome.setOnClickListener(mOnTabListener);
        mLlMe.setOnClickListener(mOnTabListener);
        mLlMessage.setOnClickListener(mOnTabListener);
        mLlHome.performClick();
    }

    /**
     * 检查版本更新
     */
    private void checkVersion() {
        BuglyUtil.requestNewVersion(new SimpleUpgradeStateListener() {
            @Override
            public void onVersion(boolean hasNewVersion) {
                super.onVersion(hasNewVersion);
                Log.d("requestNewVersion", "hasNewVersion = " + hasNewVersion);
                if (isMeResumed() && !EmptyUtil.isEmpty(MainActivity.this)) {
                    mTvMsgVersion.setText("新");
                    mTvMsgVersion.setVisibility(hasNewVersion ? View.VISIBLE : View.GONE);
                    if (hasNewVersion) {
                        // 防止和欢迎动画重叠
                        if (DialogFactory.getSvgaTDialog() == null) {
                            AutoUpdateService.startUpdate(mContext);
                        }
                    }
                }
            }
        });
    }

    private void setTab(LinearLayout layout, boolean selected) {
        if (layout != null) {
            int childCount = layout.getChildCount();
            if (childCount > 0) {
                for (int i = 0; i < childCount; i++) {
                    View view = layout.getChildAt(i);
                    if (view != null) {
                        view.setSelected(selected);
                    }
                }
            }
        }
    }

    @Override
    public int getStatusBarColor() {
        return STATUSBAR_TRANSPARENT;
    }

    private void showMain() {
        if (mMainRoot.getVisibility() != View.VISIBLE) {
            // 淡入
            mMainRoot.setAlpha(0f);
            mMainRoot.setVisibility(View.VISIBLE);
            mMainRoot.animate().alpha(1f).setDuration(300).setListener(new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (EmptyUtil.isEmpty(MainActivity.this)) {
                        return;
                    }

                    mMainRoot2.setAlpha(0f);
                    mMainRoot2.setVisibility(View.VISIBLE);
                    mMainRoot2.animate().alpha(1f).setDuration(400).setListener(new SimpleAnimatorListener() {

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (EmptyUtil.isEmpty(MainActivity.this)) {
                                return;
                            }
                            getWindow().setBackgroundDrawableResource(R.drawable.page_gray_radius);
                            mMainRoot.setBackgroundColor(Color.TRANSPARENT);
                            onMeResume();
                        }
                    }).setStartDelay(1200);
                }

            }).setStartDelay(700);

        }
    }

    private void onMeResume() {
        trueResume = true;
        if (!UserManager.getInstance().isTrueLogin()) {
            LoginActivity.startActivity(mContext, LoginActivity.BY_PASSWORD);
        } else {
            mMainHandler.removeCallbacks(checkVersionRunnable);
            mMainHandler.postDelayed(checkVersionRunnable, isMeResumed2() ? 1000 : 2500);
            showSVGALogin();
            moodHintDialog();
            ttsWelcome();
            // 已登录 初始化用户信息和机构信息
            initUmeng();
            initUserInfo();
            initOrganization();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // 页面显示后才可点击
        if (trueResume) {
            return super.dispatchTouchEvent(event);
        } else {
            return true;
        }
    }

    /**
     * 显示登录欢迎动画
     */
    private void showSVGALogin() {
        if (!showLoginAnim) {
            return;
        }
        if (SharePreferenceUtil.getBoolean(LoginActivity.KEY_LOGIN_BEGIN, true)) {
            LogUtil.d(TAG, "showSVGALogin start");
            try {
                if (mLlHome != null) {
                    mLlHome.performClick();
                }
            } catch (Exception e) {
                LogUtil.d(TAG, "mLlHome.performClick() -> catch");
            }

            DialogFactory.createSvgaDialog(MainActivity.this, AnimFileName.TRANSITION_WELCOM, new DialogFactory.OnSVGACallBack() {
                @Override
                public void callBack(SVGAImageView svgaImageView) {
                    LogUtil.d(TAG, "showSVGALogin end");
                    SharePreferenceUtil.setBoolean(LoginActivity.KEY_LOGIN_BEGIN, false);
                    showLoginAnim = false;
                }
            });
        } else {
            showLoginAnim = false;
        }
    }

    /**
     * 心情打卡提醒。今日提醒一次。
     */
    private void moodHintDialog() {
        if (moodHint) {
            return;
        }
        moodHint = SharePreferenceUtil.getBoolean(KEY_MOOD_HINT
                + ActivityLifeCycle.getTodayStartTime()
                + UserManager.getInstance().getUID(), false);
        if (moodHint) {
            return;
        }
        if (DialogFactory.getSvgaTDialog() != null) {
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isMeResumed()) {
                        moodHintDialog();
                    }
                }
            }, 6000);
            return;
        }
        MoodLivelyHelper.addListener(new MoodLivelyHelper.MoodLivelyListener() {
            @Override
            public void onMoodLively(MoodLivelyModel data) {
                if (data != null && data.getUserMood() == null) {
                    DialogFactory.createMoodSignInDialog(MainActivity.this, new OnViewClickListener() {
                        @Override
                        public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                            tDialog.dismiss();
                            if (view.getId() == R.id.bt_go) {
                                StatusActivity.startActivity(mContext, true);
                            }
                        }
                    });
                }
                moodHint = true;
                SharePreferenceUtil.setBoolean(KEY_MOOD_HINT
                        + ActivityLifeCycle.getTodayStartTime()
                        + UserManager.getInstance().getUID(), moodHint);
                MoodLivelyHelper.removeListener(this);
            }
        });

    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMainRoot.getVisibility() != View.VISIBLE) {
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showMain();
                }
            }, 300);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
        if (!trueResume && mMainRoot.getVisibility() != View.VISIBLE) {
            finish();
        }
    }

    /**
     * 用户信息更新观察者
     *
     * @return
     */
    @Override
    public UserObserver addObserver() {
        return new UserObserver() {
            @Override
            public void onChanged(UserManager accountManager, User user) {
                if (accountManager.isNewLogin()) {
                    // 此时延迟两秒，是因为登录同时 activity 没有 resume 网络请求执行有误（在机器性能差的平台上）
                    HandlerUtil.getMainHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 已登录 初始化用户信息和机构信息
                            initUmeng();
                            initUserInfo();
                            initOrganization();
                        }
                    }, 2000);
                }
            }
        };
    }

    /**
     * 查询组织信息
     */
    private void initOrganization() {
        if (initOrganization) {
            return;
        }

        new OrganizationPresenter(new OrganizationPresenter.OrganizationUi<OrganizationSelectedModel>() {

            @Override
            public LifecycleProvider getLifecycleProvider() {
                return MainActivity.this;
            }

            @Override
            public void onSuccess(OrganizationSelectedModel data) {
                if (data != null) {
                    LogUtil.d(TAG, "initOrganization onSuccess ++++++++++++++++++++++ ");
                    ArrayList<OrganizationModel.OrganizationBean> mSelecteds = new ArrayList<>();
                    initSelectedList(mSelecteds, data.getOrganization());
                    CommonParameter.setOrganizationModelPhone(data);
                    CommonParameter.setOrganizationSelected(mSelecteds);
                    UserManager.getInstance().getUser().setOrganList(mSelecteds);
                    UserManager.getInstance().notifyUserInfo();
                    UPushAlias.setTag(mSelecteds.get(mSelecteds.size() - 1).getId());
                }
                initOrganization = true;
                TimingHelper.getInstance().removeWorkArriveListener(WorkInt.SECOND5_1);
            }

            @Override
            public void onFail(ResponeThrowable throwable) {
                LogUtil.d(TAG, "initOrganization onFail ++++++++++++++++++++++ ");
                TimingHelper.getInstance().addWorkArriveListener(new TimingHelper.WorkListener() {
                    @Override
                    public void onArrive() {
                        initOrganization();
                        LogUtil.d(TAG, "initOrganization onFail retry ++++++++++++++++++++++ ");
                    }

                    @Override
                    public WorkInt workInt() {
                        return WorkInt.SECOND5_1;
                    }
                });
            }
        }).onGetJoinedList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DialogFactory.getSvgaTDialog() != null) {
            DialogFactory.getSvgaTDialog().dismiss();
        }
        if (isMeResumed2()) {
            onMeResume();
        }
    }

    private void initSelectedList
            (List<OrganizationModel.OrganizationBean> list, OrganizationSelectedModel.OrganizationBean
                    bean) {
        if (bean != null) {
            OrganizationModel.OrganizationBean oBean = new OrganizationModel.OrganizationBean();
            oBean.setId(bean.getId());
            oBean.setName(bean.getName());
            list.add(oBean);
            if (bean.getChildren() != null) {
                initSelectedList(list, bean.getChildren());
            }
        }
    }

    /**
     * 查询用户信息
     */
    private void initUserInfo() {
        if (initUserInfo) {
            return;
        }

        MsgCountManager.getInstance().addOnCountChangeListener(this);

        new UserInfoPresenter(new UserInfoPresenter.UserInfoUi<User.UserInfo>() {
            @Override
            public LifecycleProvider getLifecycleProvider() {
                return MainActivity.this;
            }

            @Override
            public void onSuccess(User.UserInfo data) {
                if (data != null) {
                    initUserInfo = true;
                    LogUtil.d(TAG, "initUserInfo onSuccess ++++++++++++++++++++++ ");
                    UserManager.getInstance().getUser().setUserInfo(data);
                    UserManager.getInstance().notifyUserInfo();
                    UPushAlias.set(MyApplication.getApp(), "user_" + data.id, "user");
                    ttsWelcome();
                    TimingHelper.getInstance().removeWorkArriveListener(WorkInt.SECOND5);
                } else {
                    onFail(new ResponeThrowable(0, "data is null"));
                }
            }

            @Override
            public void onFail(ResponeThrowable throwable) {
                LogUtil.d(TAG, "initUserInfo onFail ++++++++++++++++++++++ ");
                TimingHelper.getInstance().addWorkArriveListener(new TimingHelper.WorkListener() {
                    @Override
                    public void onArrive() {
                        initUserInfo();
                        LogUtil.d(TAG, "initUserInfo onFail retry ++++++++++++++++++++++ ");
                    }

                    @Override
                    public WorkInt workInt() {
                        return WorkInt.SECOND5;
                    }
                });
            }
        }).onGetUserInfo();
    }

    private void ttsWelcome() {
        if (initTts) {
            return;
        }
        initTts = !SharePreferenceUtil.getBoolean(LoginActivity.KEY_LOGIN_BEGIN_TTS, false);
        if (initTts) {
            return;
        }
        if (isMeResumed() && initUserInfo) {
            SharePreferenceUtil.setBoolean(LoginActivity.KEY_LOGIN_BEGIN_TTS, false);
            TtsHelper.getInstance().start(new TtsModel("你好" + UserManager.getInstance().getUser().getVisibleName() + ",很高兴见到你"), null);
            initTts = true;
        }
    }

    private void initUmeng() {
        if (initUmeng) {
            return;
        }
        if (hasAgreedAgreement()) {
            PushAgent.getInstance(this).onAppStart();
            String deviceToken = PushAgent.getInstance(this).getRegistrationId();
            LogUtil.d(TAG, "deviceToken = " + deviceToken);
            initUmeng = true;
        } else {
            agreementAfterInitUmeng();
        }
    }

    /**
     * 登录状态下，即点击隐私协议同意按钮后，初始化PushSDK
     */
    private void agreementAfterInitUmeng() {
        MyPreferences.getInstance(getApplicationContext()).setAgreePrivacyAgreement(true);
        PushHelper.init(getApplicationContext());
        PushAgent.getInstance(getApplicationContext()).register(new UPushRegisterCallback() {
            @Override
            public void onSuccess(final String deviceToken) {
                LogUtil.d(TAG, "deviceToken = " + deviceToken);
                initUmeng = true;
            }

            @Override
            public void onFailure(String code, String msg) {
                Log.d("MainActivity", "code:" + code + " msg:" + msg);
            }
        });
    }

    @Override
    public void onDestroy() {
        MsgCountManager.getInstance().removeOnCountChangeListener(this);
        super.onDestroy();
        mainActivity = null;
        resetInit();
    }

    @Override
    public void onCount(int numTask, int numSystem) {
        MsgCountManager.setTextView(mTvMsgCount, numSystem + numTask);
    }

    @Override
    public void onItemState(int id, boolean read) {

    }

    @Override
    public void onReadAll(int type) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 启动页没关闭，直接退出
        if (!trueResume) {
            finish();
            MyApplication.kill();
            return true;
        }
        /*判断用户是否点击了“返回键”*/
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                MyApplication.kill();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
