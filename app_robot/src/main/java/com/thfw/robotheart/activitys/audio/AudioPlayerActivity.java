package com.thfw.robotheart.activitys.audio;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.makeramen.roundedimageview.RoundedImageView;
import com.thfw.base.api.HistoryApi;
import com.thfw.base.face.MyAnimationListener;
import com.thfw.base.face.OnRvItemListener;
import com.thfw.base.models.AudioEtcDetailModel;
import com.thfw.base.models.AudioEtcModel;
import com.thfw.base.models.ChatEntity;
import com.thfw.base.models.CommonModel;
import com.thfw.base.models.TaskMusicEtcModel;
import com.thfw.base.net.ResponeThrowable;
import com.thfw.base.presenter.AudioPresenter;
import com.thfw.base.presenter.HistoryPresenter;
import com.thfw.base.presenter.TaskPresenter;
import com.thfw.base.utils.DataChangeHelper;
import com.thfw.base.utils.EmptyUtil;
import com.thfw.base.utils.HourChangeHelper;
import com.thfw.base.utils.LogUtil;
import com.thfw.base.utils.ToastUtil;
import com.thfw.robotheart.AudioService;
import com.thfw.robotheart.R;
import com.thfw.robotheart.activitys.RobotBaseActivity;
import com.thfw.robotheart.adapter.AudioItemAdapter;
import com.thfw.robotheart.constants.UIConfig;
import com.thfw.robotheart.util.AudioModel;
import com.thfw.robotheart.util.DialogRobotFactory;
import com.thfw.robotheart.util.ExoPlayerFactory;
import com.thfw.robotheart.view.TitleRobotView;
import com.thfw.ui.dialog.TDialog;
import com.thfw.ui.dialog.base.BindViewHolder;
import com.thfw.ui.utils.GlideUtil;
import com.thfw.ui.utils.VideoGestureHelper;
import com.thfw.ui.widget.LoadingView;
import com.thfw.ui.widget.ShowChangeLayout;
import com.trello.rxlifecycle2.LifecycleProvider;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * 正念冥想播放音频
 */
public class AudioPlayerActivity extends RobotBaseActivity<AudioPresenter> implements
        VideoGestureHelper.VideoGestureListener, AudioPresenter.AudioUi<AudioEtcDetailModel> {


    private static final String KEY_RECOMMEND = "key.recommend";
    private static TaskMusicEtcModel mStaticTaskEtcModel;
    boolean flDurationEnd = true;
    private StyledPlayerView mAudioView;
    private LoadingView mPbBar;
    private View btPlay;
    private View btPause;
    private ProgressBar mPbLoading;
    private TitleRobotView mTitleView;
    private RoundedImageView mRivEtc;
    private ObjectAnimator animation;
    private ArrayList<AudioEtcDetailModel.AudioItemModel> mAudios = new ArrayList<>();
    private SimpleExoPlayer player;
    private ImageView mIvPlayCateLogue;
    private RecyclerView mRvList;
    private ConstraintLayout mFlContent;
    private ImageView mIvCollect;
    private AudioEtcModel mModel;
    private AudioEtcDetailModel.AudioItemModel mItemModel;
    private AudioEtcDetailModel mDetailModel;
    private TitleRobotView mTitleRobotView;
    private ConstraintLayout mClContent;
    private TextView mTvEtcTitle;
    private TextView mTvEtcTitleLogcate;
    private TextView mTvAudioTitle;
    private PlayerListener playerListener;
    private boolean requestIng = false;
    private boolean audioByPagePause;
    private boolean autoFinished;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.MyBinder mMyBinder = (AudioService.MyBinder) service;
            AudioService mMyService = mMyBinder.getService();
            mMyBinder.setMusic(new AudioModel());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private VideoGestureHelper ly_VG;
    private ShowChangeLayout scl;
    private AudioManager mAudioManager;
    private int maxVolume = 0;
    private int oldVolume = 0;
    private int newProgress = 0, oldProgress = 0;
    private Window mWindow;
    private WindowManager.LayoutParams mLayoutParams;
    private boolean isTask;
    private int mMusicId;
    private ArrayList<TaskMusicEtcModel.MusicListBean> mTaskEtcModel;

    public static void startActivity(Context context, AudioEtcModel audioEtcModel) {
        ((Activity) context).startActivityForResult(new Intent(context, AudioPlayerActivity.class)
                .putExtra(KEY_DATA, audioEtcModel), ChatEntity.TYPE_RECOMMEND_AUDIO_ETC);
    }

    public static void startActivity(Context context, AudioEtcModel audioEtcModel, TaskMusicEtcModel taskMusicEtcModel) {
        mStaticTaskEtcModel = taskMusicEtcModel;
        ((Activity) context).startActivityForResult(new Intent(context, AudioPlayerActivity.class)
                .putExtra(KEY_DATA, audioEtcModel), ChatEntity.TYPE_RECOMMEND_AUDIO_ETC);
    }

    public static void startActivity(Context context, AudioEtcDetailModel.AudioItemModel audioItemModel) {
        ((Activity) context).startActivityForResult(new Intent(context, AudioPlayerActivity.class)
                .putExtra(KEY_RECOMMEND, audioItemModel), ChatEntity.TYPE_RECOMMEND_AUDIO);
    }

    @Override
    public int getContentView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        return R.layout.activity_audio_player;
    }

    @Override
    public AudioPresenter onCreatePresenter() {
        return new AudioPresenter(this);
    }

    @Override
    public void initView() {

        mModel = (AudioEtcModel) getIntent().getSerializableExtra(KEY_DATA);
        mItemModel = (AudioEtcDetailModel.AudioItemModel) getIntent().getSerializableExtra(KEY_RECOMMEND);
        if (mModel == null && mItemModel == null) {
            ToastUtil.show("参数错误");
            finish();
            return;
        }


        mAudioView = (StyledPlayerView) findViewById(R.id.audio_view);
        mIvCollect = (ImageView) findViewById(R.id.iv_collect);

        mRivEtc = (RoundedImageView) findViewById(R.id.riv_etc);

        mIvPlayCateLogue = (ImageView) findViewById(R.id.iv_play_catelogue);
        mPbBar = findViewById(R.id.loadingView);
        // todo 背景模糊
//        Bitmap bitmap = BitmapUtil.getResourceBitmap(mContext, R.mipmap.cat);
//        mIvBlurBg.setImageBitmap(PaletteUtil.doBlur(bitmap, 2, true));
        btPlay = findViewById(R.id.exo_play);
        btPause = findViewById(R.id.exo_pause);

        mPbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        mTitleView = (TitleRobotView) findViewById(R.id.titleRobotView);
        mRvList = (RecyclerView) findViewById(R.id.rv_list);
        mFlContent = (ConstraintLayout) findViewById(R.id.cl_content);
        mRvList.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        mIvPlayCateLogue.setOnClickListener(v -> {
            audioLogcatue();
        });
        mFlContent.setOnClickListener(v -> {
            audioLogcatue();
        });


        mTitleRobotView = (TitleRobotView) findViewById(R.id.titleRobotView);
        mClContent = (ConstraintLayout) findViewById(R.id.cl_content);
        mTvEtcTitle = (TextView) findViewById(R.id.tv_etc_title);
        mTvEtcTitleLogcate = (TextView) findViewById(R.id.tv_etc_title_logcate);
        mTvAudioTitle = (TextView) findViewById(R.id.tv_audio_title);

        initGesture();
    }

    /**
     * 目录显示隐藏
     */
    private void audioLogcatue() {
        if (mFlContent.getVisibility() == View.VISIBLE) {

            mFlContent.animate().alpha(0f).setListener(new MyAnimationListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mFlContent.setVisibility(View.GONE);
                }
            }).start();
        } else {
            if (mRvList.getAdapter() == null) {
                AudioItemAdapter audioItemAdapter = new AudioItemAdapter(mAudios);
                audioItemAdapter.setCurrentIndex(player.getCurrentWindowIndex());
                audioItemAdapter.setOnRvItemListener(new OnRvItemListener<AudioEtcDetailModel.AudioItemModel>() {
                    @Override
                    public void onItemClick(List<AudioEtcDetailModel.AudioItemModel> list, int position) {
                        player.seekTo(position, 0);
                    }
                });
                mRvList.setAdapter(audioItemAdapter);
            } else {
                if (player != null) {
                    ((AudioItemAdapter) mRvList.getAdapter()).setCurrentIndex(player.getCurrentWindowIndex());
                }
            }
            mFlContent.setAlpha(0f);
            mFlContent.setVisibility(View.VISIBLE);
            flDurationEnd = false;
            mFlContent.animate().alpha(1f).setListener(new MyAnimationListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    flDurationEnd = true;
                }
            });
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void etcAnimState(boolean play) {
        if (play) {
            if (animation == null) {
                animation = ObjectAnimator.ofFloat(mRivEtc, "rotation", 0, 360);
                animation.setRepeatCount(ObjectAnimator.INFINITE);
                animation.setRepeatMode(ObjectAnimator.RESTART);
                animation.setDuration(10000);
                animation.setInterpolator(new LinearInterpolator());
                animation.start();
            } else {
                animation.resume();
            }
        } else {
            if (animation != null) {
                animation.pause();
            }
        }

    }

    @Override
    public void initData() {

        if (mItemModel != null) {
            mAudios.add(mItemModel);
            GlideUtil.load(mContext, mItemModel.getImg(), mRivEtc);
            setAudioData();
            mPbBar.hide();
            autoFinished = mItemModel.isAutoFinished();
        } else {
            autoFinished = mModel.isAutoFinished();

            // 正念冥想任务
            if (mStaticTaskEtcModel != null) {
                if (mStaticTaskEtcModel.getMusicList() != null) {
                    mTaskEtcModel = new ArrayList<>();
                    mTaskEtcModel.addAll(mStaticTaskEtcModel.getMusicList());
                    isTask = !EmptyUtil.isEmpty(mTaskEtcModel);
                }
                mStaticTaskEtcModel = null;
            }


            GlideUtil.load(mContext, mModel.getImg(), mRivEtc);
            mPresenter.getAudioEtcInfo(mModel.getId());
            mPbBar.showLoadingNoText();
        }
    }

    private void setAudioData() {
        if (EmptyUtil.isEmpty(mAudios)) {
            ToastUtil.show("没有音频资源");
            return;
        }

        btPlay.setOnClickListener(v -> {
            ExoPlayerFactory.getExoPlayer().play();
        });

        btPause.setOnClickListener(v -> {
            ExoPlayerFactory.getExoPlayer().pause();
        });


        btPlay.setVisibility(View.GONE);
        btPause.setVisibility(View.VISIBLE);


        ExoPlayerFactory.with(mContext).builder(ExoPlayerFactory.EXO_AUDIO);
        player = ExoPlayerFactory.getExoPlayer();

        playerListener = new PlayerListener();
        playerListener.setPbBar(mPbBar);
        player.addListener(playerListener);
        mAudioView.setPlayer(player);

        // 任务完成状态初始化
        if (mTaskEtcModel != null) {
            SparseIntArray statusMap = new SparseIntArray();
            for (TaskMusicEtcModel.MusicListBean bean : mTaskEtcModel) {
                statusMap.put(bean.getId(), bean.getStatus());
            }
            for (AudioEtcDetailModel.AudioItemModel mediaItem : mAudios) {
                mediaItem.status = statusMap.get(mediaItem.getId());
            }
        }

        for (AudioEtcDetailModel.AudioItemModel mediaItem : mAudios) {
            LogUtil.d(TAG, "url = " + mediaItem.getSfile());
            player.addMediaItem(MediaItem.fromUri(mediaItem.getSfile()));
//            player.addMediaSource(buildMediaSource(Uri.parse(mediaItem.getSfile())));
        }
        if (mDetailModel != null) {
            GlideUtil.load(mContext, mDetailModel.getCollectionInfo().getImg(), mRivEtc);
            // 是否收藏
            mIvCollect.setSelected(mDetailModel.getCollectionInfo().getCollected() != 0);
            mIvCollect.setOnClickListener(v -> {
                addCollect();
            });
            mTvEtcTitleLogcate.setText(mDetailModel.getCollectionInfo().getTitle() + mAudios.size() + "课时");
            mTvEtcTitle.setText("所属章节：" + mDetailModel.getCollectionInfo().getTitle()
                    + "  " + (mDetailModel.getCollectionInfo().getListenHistorySize()) + "/" + mAudios.size());
            mTvAudioTitle.setText(mAudios.get(mDetailModel.getCollectionInfo().getLastHourIndex()).getTitle());
            player.seekTo(mDetailModel.getCollectionInfo().getLastHourIndex(), 0);
        } else {
            mIvCollect.setVisibility(View.GONE);
            mTvEtcTitle.setVisibility(View.GONE);
            findViewById(R.id.exo_next).setVisibility(View.GONE);
            findViewById(R.id.exo_prev).setVisibility(View.GONE);
            mIvPlayCateLogue.setVisibility(View.GONE);
            mTvAudioTitle.setText(mAudios.get(0).getTitle());
            player.seekTo(0, 0);
        }

        mMusicId = mAudios.get(player.getCurrentWindowIndex()).getId();

        player.prepare();
        // Start the playback.
        player.play();

        Intent mServiceIntent = new Intent(mContext, AudioService.class);
        mContext.bindService(mServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    // 重定向
    public DefaultDataSourceFactory buildDataSourceFactory() {

        DefaultBandwidthMeter mDefaultBandwidthMeter = new DefaultBandwidthMeter();
        DefaultDataSourceFactory upstreamFactory = new DefaultDataSourceFactory(
                mContext,
                mDefaultBandwidthMeter,
                new DefaultHttpDataSourceFactory("audio/mpeg", 15000, 15000, true));
        return upstreamFactory;
    }

    private MediaSource buildMediaSource(Uri uri) {
        DefaultBandwidthMeter mDefaultBandwidthMeter = new DefaultBandwidthMeter();
        // 重定向 301 302 http 2 https
        DefaultDataSourceFactory upstreamFactory = new DefaultDataSourceFactory(this, mDefaultBandwidthMeter, new DefaultHttpDataSourceFactory(UIConfig.getUserAgent(), null, 15000, 15000, true));

        return new ProgressiveMediaSource.Factory(upstreamFactory).createMediaSource(uri);
        // 已弃用，无相关类
//        return new ExtractorMediaSource.Factory(upstreamFactory). createMediaSource(uri); }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null && player.isPlaying()) {
            audioByPagePause = true;
            player.pause();
        } else {
            audioByPagePause = false;
        }
    }

    @Override
    protected void onResume() {
        if (audioByPagePause && player != null && !player.isPlaying()) {
            player.play();
        }
        super.onResume();
        mAudioView.setUseController(true);
        mAudioView.setControllerHideOnTouch(false);
        mAudioView.setControllerShowTimeoutMs(0);
        mAudioView.showController();
    }


    //=============== 开始 视频手势 =======================//

    @Override
    public int getStatusBarColor() {
        return STATUSBAR_TRANSPARENT;
    }

    @Override
    public LifecycleProvider getLifecycleProvider() {
        return this;
    }

    @Override
    public void onSuccess(AudioEtcDetailModel data) {
        if (mDetailModel != null) {
            return;
        }
        mPbBar.hide();
        if (data != null) {
            mDetailModel = data;
            mAudios.addAll(data.getMusicList());
            setAudioData();
        }

    }

    @Override
    public void onFail(ResponeThrowable throwable) {
        if (mDetailModel != null) {
            return;
        }
        mPbBar.showFail(v -> {
            initData();
        });
    }

    public void addCollect() {
        if (requestIng || mDetailModel == null) {
            return;
        }
        requestIng = true;
        mIvCollect.setSelected(!mIvCollect.isSelected());
        new HistoryPresenter(new HistoryPresenter.HistoryUi<CommonModel>() {
            @Override
            public LifecycleProvider getLifecycleProvider() {
                return AudioPlayerActivity.this;
            }

            @Override
            public void onSuccess(CommonModel data) {
                requestIng = false;
                ToastUtil.show(mIvCollect.isSelected() ? UIConfig.COLLECTED : UIConfig.COLLECTED_UN);
            }

            @Override
            public void onFail(ResponeThrowable throwable) {
                requestIng = false;
                ToastUtil.show(mIvCollect.isSelected() ? "收藏失败" : "取消收藏失败");
                mIvCollect.setSelected(!mIvCollect.isSelected());
            }
        }).addCollect(HistoryApi.TYPE_COLLECT_AUDIO, mDetailModel.getCollectionInfo().getId());
    }

    @Override
    public void finish() {
        setResult(RESULT_OK);
        super.finish();
    }

    /**
     * 完成正念冥想任务
     *
     * @param id
     */
    private void onFinishMusic(int id) {
        if (mDetailModel == null) {
            LogUtil.d(TAG, "onFinishMusic - begin return");
            return;
        }
        for (AudioEtcDetailModel.AudioItemModel itemModel : mAudios) {
            if (mMusicId == itemModel.getId()) {
                itemModel.status = 1;
                if (mFlContent.getVisibility() == View.VISIBLE) {
                    mRvList.getAdapter().notifyDataSetChanged();
                }
                break;
            }
        }

        new TaskPresenter(new TaskPresenter.TaskUi<CommonModel>() {
            @Override
            public LifecycleProvider getLifecycleProvider() {
                return AudioPlayerActivity.this;
            }

            @Override
            public void onSuccess(CommonModel data) {
                LogUtil.d(TAG, "onFinishMusic - onSuccess");
            }

            @Override
            public void onFail(ResponeThrowable throwable) {
                LogUtil.d(TAG, "onFinishMusic - onFail");
            }
        }).onFinishMusic(id, mDetailModel.getCollectionInfo().getId());
        LogUtil.d(TAG, "onFinishMusic - begin");
    }

    @Override
    public void onDestroy() {
        if (mDetailModel != null && mDetailModel.getCollectionInfo() != null) {
            DataChangeHelper.collectChange(mIvCollect, mDetailModel.getCollectionInfo().getId());
        }
        super.onDestroy();
        ExoPlayerFactory.release();
        try {
            mContext.unbindService(connection);
        } catch (Exception e) {
        }
    }

    private void initGesture() {
        ly_VG = new VideoGestureHelper(mContext, mAudioView);
        ly_VG.setVideoGestureListener(this);

        scl = (ShowChangeLayout) findViewById(R.id.scl);

        //初始化获取音量属性
        mAudioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        //下面这是设置当前APP亮度的方法配置
        mWindow = getWindow();
        mLayoutParams = mWindow.getAttributes();

    }

    @Override
    public void onDown(MotionEvent e) {
        oldVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onEndFF_REW(MotionEvent e) {
    }

    @Override
    public void onVolumeGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, "onVolumeGesture: oldVolume " + oldVolume);
        int value = ly_VG.getHeight() / maxVolume;
        int newVolume = (int) ((e1.getY() - e2.getY()) / value + oldVolume);

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_PLAY_SOUND);

        Log.d(TAG, "onVolumeGesture: value" + value);
        Log.d(TAG, "onVolumeGesture: newVolume " + newVolume);
        //要强行转Float类型才能算出小数点，不然结果一直为0
        int volumeProgress = (int) (newVolume / Float.valueOf(maxVolume) * 100);
        if (volumeProgress >= 50) {
            scl.setImageResource(R.drawable.ic_volume_higher_w);
        } else if (volumeProgress > 0) {
            scl.setImageResource(R.drawable.ic_volume_lower_w);
        } else {
            scl.setImageResource(R.drawable.ic_volume_off_w);
        }
        scl.setProgress(volumeProgress);
        scl.show();
    }

    @Override
    public void onBrightnessGesture(MotionEvent e1, MotionEvent e2, float distanceX,
                                    float distanceY) {
        onVolumeGesture(e1, e2, distanceX, distanceY);
    }

    @Override
    public void onFF_REWGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, "onFF_REWGesture: ");
    }

    @Override
    public void onSingleTapGesture(MotionEvent e) {
        Log.d(TAG, "onSingleTapGesture: ");
    }

    @Override
    public void onDoubleTapGesture(MotionEvent e) {
        Log.d(TAG, "onDoubleTapGesture: ");
    }

    @Override
    public void onDoubleTapUp(MotionEvent e) {

    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d(TAG, "onLongPress: ");
    }

    @Override
    public void onBackPressed() {
        if (isTask) {
            for (AudioEtcDetailModel.AudioItemModel itemModel : mAudios) {
                if (itemModel.status != 1) {
                    showAudioTaskStop();
                    return;
                }
            }

        }
        super.onBackPressed();
    }

    private void showAudioTaskStop() {
        DialogRobotFactory.createCustomDialog(AudioPlayerActivity.this, new DialogRobotFactory.OnViewCallBack() {
            @Override
            public void callBack(TextView mTvTitle, TextView mTvHint, TextView mTvLeft, TextView mTvRight, View mVLineVertical) {
                mTvHint.setText("您尚未完成任务，确认退出？");
                mTvLeft.setText("直接退出");
                mTvRight.setText("继续完成");
                mTvTitle.setVisibility(View.GONE);
            }

            @Override
            public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                tDialog.dismiss();
                if (view.getId() == R.id.tv_left) {
                    finish();
                }
            }
        });
    }

    public class PlayerListener implements Player.Listener {
        private LoadingView mPbBar;

        public void setPbBar(LoadingView mPbBar) {
            this.mPbBar = mPbBar;
        }

        public LoadingView getLoading() {
            return mPbBar;
        }

        private void uiPlayOrPause(boolean play) {
            LoadingView mPbBar = getLoading();
            if (mPbBar == null) {
                return;
            }
            if (play) {
                mPbBar.hide();
            } else {
                mPbBar.showLoadingNoText();
            }
            if (play) {
                etcAnimState(true);
            }
        }


        @Override
        public void onPlaybackStateChanged(int state) {
            if (isTask && state == Player.STATE_ENDED) {
                onFinishMusic(mMusicId);
            }
            if (state == Player.STATE_READY || state == Player.STATE_ENDED) {
                uiPlayOrPause(true);
                if (autoFinished && state == Player.STATE_ENDED) {
                    finish();
                    return;
                }
            } else {
                uiPlayOrPause(false);
            }
            LoadingView mPbBar = getLoading();
            if (mPbBar == null) {
                return;
            }
            if ("error".equals(String.valueOf(mPbBar.getTag()))) {
                mPbBar.hide();
            }

        }

        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            btPause.setVisibility(!playWhenReady ? View.GONE : View.VISIBLE);
            btPlay.setVisibility(playWhenReady ? View.GONE : View.VISIBLE);
            etcAnimState(playWhenReady);
        }

        /**
         * 列表播放曲目切换监听
         *
         * @param mediaItem
         * @param reason
         */
        @Override
        public void onMediaItemTransition(@Nullable @org.jetbrains.annotations.Nullable MediaItem mediaItem, int reason) {
            LogUtil.d(TAG, "onMediaItemTransition reason = " + reason);
            if (isTask && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                onFinishMusic(mMusicId);
            }
            if (autoFinished && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                finish();
                return;
            }
            // 列表播放曲目切换监听
            if (mDetailModel == null) {
                return;
            }
            LogUtil.d("onMediaItemTransition -> " + mediaItem.playbackProperties.uri);
            mTvAudioTitle.setText(mAudios.get(player.getCurrentWindowIndex()).getTitle());

            mTvEtcTitle.setText("所属章节：" + mDetailModel.getCollectionInfo().getTitle()
                    + "  " + (player.getCurrentWindowIndex() + 1) + "/" + mAudios.size());
            int musicId = mAudios.get(player.getCurrentWindowIndex()).getId();
            mMusicId = musicId;
            mPresenter.addAudioHistory(musicId, mDetailModel.getCollectionInfo().getId());
            // 列表页更新
            HourChangeHelper.getInstance().notify(mDetailModel.getCollectionInfo().getId(), player.getCurrentWindowIndex() + 1, musicId);
//            if (player == null) {
//                return;
//            }
//            findViewById(R.id.exo_next).setAlpha(player.hasNext() ? 1f : 0.5f);
//            findViewById(R.id.exo_prev).setAlpha(player.hasPrevious() ? 1f : 0.5f);
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            LoadingView mPbBar = getLoading();
            if (mPbBar == null) {
                return;
            }
            mPbBar.setTag("error");
            mPbBar.showLoadingNoText();
            uiPlayOrPause(false);

            if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                IOException cause = error.getSourceException();
                if (cause instanceof HttpDataSource.HttpDataSourceException) {
                    // An HTTP error occurred.
                    HttpDataSource.HttpDataSourceException httpError = (HttpDataSource.HttpDataSourceException) cause;
                    // This is the request for which the error occurred.
                    DataSpec requestDataSpec = httpError.dataSpec;
                    // It's possible to find out more about the error both by casting and by
                    // querying the cause.
                    if (httpError instanceof HttpDataSource.InvalidResponseCodeException) {
                        // Cast to InvalidResponseCodeException and retrieve the response code,
                        // message and headers.
                        ToastUtil.show("视频资源错误");
                    } else {
                        // Try calling httpError.getCause() to retrieve the underlying cause,
                        // although note that it may be null.

                        if (httpError.getCause() instanceof UnknownHostException) {
                            ToastUtil.show("网络异常，请检查网络");
                        } else {
                            ToastUtil.show("未知错误" + httpError.getCause());
                        }
                    }
                }
            } else if (error.type == ExoPlaybackException.TYPE_RENDERER) {
                ToastUtil.show("渲染错误 - TYPE_UNEXPECTED");
            } else if (error.type == ExoPlaybackException.TYPE_UNEXPECTED) {
                ToastUtil.show("未知错误 - TYPE_UNEXPECTED");
            } else if (error.type == ExoPlaybackException.TYPE_REMOTE) {
                ToastUtil.show("网络错误");
            }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
            // 下一首按键
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
            // 上一首按键
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            // 播放/暂停按键
            if (player!= null) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.play();
                }
                return true;
            }
        }
        // 还可以添加更多按键操作，可以参阅 KeyEvent 类
        return super.onKeyDown(keyCode, event);
    }
}