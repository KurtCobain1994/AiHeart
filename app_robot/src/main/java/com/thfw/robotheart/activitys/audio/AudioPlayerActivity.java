package com.thfw.robotheart.activitys.audio;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.view.View;
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
import com.thfw.base.models.CommonModel;
import com.thfw.base.net.ResponeThrowable;
import com.thfw.base.presenter.AudioPresenter;
import com.thfw.base.presenter.HistoryPresenter;
import com.thfw.base.utils.EmptyUtil;
import com.thfw.base.utils.LogUtil;
import com.thfw.base.utils.ToastUtil;
import com.thfw.robotheart.AudioService;
import com.thfw.robotheart.R;
import com.thfw.robotheart.adapter.AudioItemAdapter;
import com.thfw.robotheart.util.AudioModel;
import com.thfw.robotheart.util.ExoPlayerFactory;
import com.thfw.robotheart.view.TitleRobotView;
import com.thfw.ui.base.RobotBaseActivity;
import com.thfw.ui.utils.GlideUtil;
import com.thfw.ui.widget.LoadingView;
import com.trello.rxlifecycle2.LifecycleProvider;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class AudioPlayerActivity extends RobotBaseActivity<AudioPresenter> implements AudioPresenter.AudioUi<AudioEtcDetailModel> {


    private StyledPlayerView mAudioView;
    private ImageView mIvBlurBg;
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
    boolean flDurationEnd = true;
    private ConstraintLayout mFlContent;
    private ImageView mIvCollect;
    private AudioEtcModel mModel;
    private AudioEtcDetailModel mDetailModel;
    private TitleRobotView mTitleRobotView;
    private ConstraintLayout mClContent;
    private TextView mTvEtcTitle;
    private TextView mTvEtcTitleLogcate;
    private TextView mTvAudioTitle;
    private PlayerListener playerListener;
    private boolean requestIng = false;
    private boolean audioByPagePause;

    public static void startActivity(Context context, AudioEtcModel audioEtcModel) {
        context.startActivity(new Intent(context, AudioPlayerActivity.class)
                .putExtra(KEY_DATA, audioEtcModel));
    }

    @Override
    public int getContentView() {
        return R.layout.activity_audio_player;
    }

    @Override
    public AudioPresenter onCreatePresenter() {
        return new AudioPresenter(this);
    }

    @Override
    public void initView() {

        mModel = (AudioEtcModel) getIntent().getSerializableExtra(KEY_DATA);
        if (mModel == null) {
            ToastUtil.show("参数错误");
            finish();
            return;
        }

        mAudioView = (StyledPlayerView) findViewById(R.id.audio_view);
        mIvBlurBg = (ImageView) findViewById(R.id.iv_blur_bg);
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
                mTvEtcTitleLogcate.setText(mModel.getTitle() + mModel.getMusicSize() + "课时");
                AudioItemAdapter audioItemAdapter = new AudioItemAdapter(mAudios);
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
        GlideUtil.load(mContext, mModel.getImg(), mRivEtc);
        mPresenter.getAudioEtcInfo(mModel.getId());
        mPbBar.showLoadingNoText();
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

        // 是否收藏
        mIvCollect.setSelected(mDetailModel.getCollectionInfo().getCollected() != 0);
        mIvCollect.setOnClickListener(v -> {
            addCollect();
        });
        // 章节与课时
        mTvEtcTitle.setText("所属章节：" + mModel.getTitle());
        mTvAudioTitle.setText(mAudios.get(0).getTitle());

        ExoPlayerFactory.with(mContext).builder(ExoPlayerFactory.EXO_AUDIO);
        player = ExoPlayerFactory.getExoPlayer();

        playerListener = new PlayerListener();
        playerListener.setPbBar(mPbBar);
        player.addListener(playerListener);
        mAudioView.setPlayer(player);
        for (AudioEtcDetailModel.AudioItemModel mediaItem : mAudios) {
            LogUtil.d(TAG, "url = " + mediaItem.getSfile());
//            player.addMediaItem(MediaItem.fromUri(mediaItem.getSfile()));
            player.addMediaSource(buildMediaSource(Uri.parse(mediaItem.getSfile())));
        }

        player.prepare();
        // Start the playback.
        player.play();

        Intent mServiceIntent = new Intent(mContext, AudioService.class);
        mContext.bindService(mServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    //重定向
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
        DefaultDataSourceFactory upstreamFactory = new DefaultDataSourceFactory(this, mDefaultBandwidthMeter, new DefaultHttpDataSourceFactory("exoplayer-codelab", null, 15000, 15000, true));

        return new ProgressiveMediaSource.Factory(upstreamFactory).createMediaSource(uri);
        // 已弃用，无相关类
//        return new ExtractorMediaSource.Factory(upstreamFactory). createMediaSource(uri); }
    }


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
        mPbBar.hide();
        if (data != null) {
            mDetailModel = data;
            mAudios.addAll(data.getMusicList());
            setAudioData();
        }

    }

    @Override
    public void onFail(ResponeThrowable throwable) {
        mPbBar.showFail(v -> {
            initData();
        });
    }

    public void addCollect() {
        if (requestIng) {
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
                ToastUtil.show(mIvCollect.isSelected() ? "收藏成功" : "取消收藏成功");
            }

            @Override
            public void onFail(ResponeThrowable throwable) {
                requestIng = false;
                ToastUtil.show(mIvCollect.isSelected() ? "收藏失败" : "取消收藏失败");
                mIvCollect.setSelected(!mIvCollect.isSelected());
            }
        }).addCollect(HistoryApi.TYPE_COLLECT_AUDIO, mModel.getId());
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

            if (state == Player.STATE_READY || state == Player.STATE_ENDED) {
                uiPlayOrPause(true);
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
            // 列表播放曲目切换监听
            LogUtil.d("onMediaItemTransition -> " + mediaItem.playbackProperties.uri);
            mTvAudioTitle.setText(mAudios.get(player.getCurrentWindowIndex()).getTitle());
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
    public void onDestroy() {
        super.onDestroy();
        ExoPlayerFactory.release();
        try {
            mContext.unbindService(connection);
        } catch (Exception e) {

        }
    }
}