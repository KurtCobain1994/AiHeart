package com.thfw.mobileheart.activity.read;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.common.reflect.TypeToken;
import com.thfw.base.models.BookStudyTypeModel;
import com.thfw.base.net.ResponeThrowable;
import com.thfw.base.presenter.BookPresenter;
import com.thfw.base.utils.EmptyUtil;
import com.thfw.base.utils.GsonUtil;
import com.thfw.base.utils.SharePreferenceUtil;
import com.thfw.mobileheart.R;
import com.thfw.mobileheart.activity.BaseActivity;
import com.thfw.mobileheart.fragment.list.StudyListFragment;
import com.thfw.mobileheart.view.LastTextView;
import com.thfw.ui.widget.LoadingView;
import com.thfw.ui.widget.TitleView;
import com.trello.rxlifecycle2.LifecycleProvider;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StudyHomeActivity extends BaseActivity<BookPresenter> implements BookPresenter.BookUi<List<BookStudyTypeModel>> {

    private static final String KEY_TYPE_LIST = "key_booy_study_type_list";
    private static final String KEY_HAS_VIDEO = "key.video.has";
    private List<Fragment> tabFragmentList = new ArrayList<>();
    private com.thfw.ui.widget.TitleView mTitleView;
    private com.google.android.material.tabs.TabLayout mTabLayout;
    private androidx.viewpager.widget.ViewPager mViewPager;
    private com.thfw.ui.widget.LoadingView mLoadingView;
    private com.thfw.mobileheart.view.LastTextView mTvLastAudio;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, StudyHomeActivity.class));
    }

    @Override
    public int getContentView() {
        return R.layout.activity_study_home;
    }

    @Override
    public BookPresenter onCreatePresenter() {
        return new BookPresenter(this);
    }

    @Override
    public void initView() {

        mTitleView = (TitleView) findViewById(R.id.titleView);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mLoadingView = (LoadingView) findViewById(R.id.loadingView);
        mTvLastAudio = (LastTextView) findViewById(R.id.tv_last_audio);
    }

    @Override
    public void initData() {


        //设置TabLayout和ViewPager联动
        mTabLayout.setupWithViewPager(mViewPager, false);

        Type type = new TypeToken<List<BookStudyTypeModel>>() {
        }.getType();
        List<BookStudyTypeModel> cacheModel = SharePreferenceUtil.getObject(KEY_TYPE_LIST, type);
        if (!EmptyUtil.isEmpty(cacheModel)) {
            mLoadingView.hide();
            setAdapter(cacheModel);
        }
        mPresenter.getIdeologyArticleType();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                ((StudyListFragment) tabFragmentList.get(tab.getPosition())).onReSelected();
            }
        });
    }

    private void setAdapter(List<BookStudyTypeModel> cacheModel) {
        int size = cacheModel.size();
        //添加tab
        for (int i = 0; i < size; i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(cacheModel.get(i).name));
            int contentType = cacheModel.get(i).id;
            StudyListFragment studyListFragment = new StudyListFragment(contentType);
            Bundle bundle = new Bundle();
            bundle.putSerializable(StudyListFragment.KEY_CHILD_TYPE, (ArrayList) cacheModel.get(i).getList());
            studyListFragment.setArguments(bundle);
            tabFragmentList.add(studyListFragment);

        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                getSupportFragmentManager().beginTransaction()
                        .setMaxLifecycle(tabFragmentList.get(position), Lifecycle.State.RESUMED).commit();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return tabFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return tabFragmentList.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return cacheModel.get(position).name;
            }
        });
    }

    @Override
    public LifecycleProvider getLifecycleProvider() {
        return this;
    }

    @Override
    public void onSuccess(List<BookStudyTypeModel> data) {
        if (data != null) {
            data.add(0, new BookStudyTypeModel("全部", 0));
        }
        SharePreferenceUtil.setString(KEY_TYPE_LIST, GsonUtil.toJson(data));
        mLoadingView.hide();
        if (mViewPager.getAdapter() == null) {
            setAdapter(data);
        }
    }

    @Override
    public void onFail(ResponeThrowable throwable) {
        if (mViewPager.getAdapter() == null) {
            mLoadingView.showFail(v -> {
                mPresenter.getIdeologyArticleType();
            });
        }
    }
}