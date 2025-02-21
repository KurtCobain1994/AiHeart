package com.thfw.robotheart.activitys.me;

import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.thfw.base.api.HistoryApi;
import com.thfw.base.base.IPresenter;
import com.thfw.robotheart.R;
import com.thfw.robotheart.activitys.RobotBaseActivity;
import com.thfw.robotheart.fragments.me.CollectFragment;
import com.thfw.robotheart.view.TitleRobotView;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CollectActivity extends RobotBaseActivity {


    private com.thfw.robotheart.view.TitleRobotView mTitleRobotView;
    private com.google.android.material.tabs.TabLayout mTabLayout;
    private com.google.android.material.tabs.TabItem mTiVideo;
    private com.google.android.material.tabs.TabItem mTiExercise;
    private com.google.android.material.tabs.TabItem mTiBook;
    private com.google.android.material.tabs.TabItem mTiStudy;
    private androidx.viewpager2.widget.ViewPager2 mViewPager;

    @Override
    public int getContentView() {
        return R.layout.activity_collect;
    }

    @Override
    public IPresenter onCreatePresenter() {
        return null;
    }

    @Override
    public void initView() {

        mTitleRobotView = (TitleRobotView) findViewById(R.id.titleRobotView);
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mTiVideo = (TabItem) findViewById(R.id.ti_video);
        mTiExercise = (TabItem) findViewById(R.id.ti_exercise);
        mTiBook = (TabItem) findViewById(R.id.ti_book);
        mTiStudy = (TabItem) findViewById(R.id.ti_study);
        mViewPager = (ViewPager2) findViewById(R.id.viewPager);
    }

    @Override
    public void initData() {
        int count = mTabLayout.getTabCount();
        for (int i = 0; i < count; i++) {
            TabLayout.TabView tabView = mTabLayout.getTabAt(i).view;
            LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) tabView.getLayoutParams();
            p.leftMargin = 50;
            p.rightMargin = 50;
            tabView.setLayoutParams(p);
        }
        HashMap<Integer, Fragment> collectMap = new HashMap<>();
        // 1-测评  2-文章 3-音频 4-视频 5-话术 6-思政文章 7-工具包
        collectMap.put(0, new CollectFragment(HistoryApi.TYPE_COLLECT_TEST));
        collectMap.put(1, new CollectFragment(HistoryApi.TYPE_COLLECT_TOOL));
        collectMap.put(2, new CollectFragment(HistoryApi.TYPE_COLLECT_AUDIO));
        collectMap.put(3, new CollectFragment(HistoryApi.TYPE_COLLECT_VIDEO));
//        collectMap.put(5, new CollectFragment(HistoryApi.TYPE_COLLECT_DIALOG));
        collectMap.put(4, new CollectFragment(HistoryApi.TYPE_COLLECT_BOOK));
        collectMap.put(5, new CollectFragment(HistoryApi.TYPE_COLLECT_IDEO_BOOK));

        mViewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @NotNull
            @Override
            public Fragment createFragment(int position) {
                return collectMap.get(position);
            }

            @Override
            public int getItemCount() {
                return collectMap.size();
            }
        });

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    mTabLayout.selectTab(mTabLayout.getTabAt(mViewPager.getCurrentItem()));
                }
            }
        });

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(mTabLayout.getSelectedTabPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}