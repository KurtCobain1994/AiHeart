package com.thfw.robotheart.activitys.me;

import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thfw.base.base.IModel;
import com.thfw.base.face.OnRvItemListener;
import com.thfw.base.models.OrganizationModel;
import com.thfw.base.net.CommonParameter;
import com.thfw.base.net.ResponeThrowable;
import com.thfw.base.presenter.OrganizationPresenter;
import com.thfw.base.utils.EmptyUtil;
import com.thfw.base.utils.ToastUtil;
import com.thfw.robotheart.R;
import com.thfw.robotheart.adapter.OrganSelectChildrenAdapter;
import com.thfw.robotheart.adapter.OrganSelectedAdapter;
import com.thfw.robotheart.view.TitleRobotView;
import com.thfw.ui.base.RobotBaseActivity;
import com.thfw.ui.dialog.LoadingDialog;
import com.thfw.ui.widget.LoadingView;
import com.thfw.user.login.UserManager;
import com.trello.rxlifecycle2.LifecycleProvider;

import java.util.ArrayList;
import java.util.List;

public class SelectOrganizationActivity extends RobotBaseActivity<OrganizationPresenter> implements OrganizationPresenter.OrganizationUi<IModel> {


    private com.thfw.robotheart.view.TitleRobotView mTitleRobotView;
    private androidx.recyclerview.widget.RecyclerView mRvSelected;
    private androidx.recyclerview.widget.RecyclerView mRvSelectChildren;
    private com.thfw.ui.widget.LoadingView mLoadingView;
    private OrganizationModel mOrganizationModel;

    private ArrayList<OrganizationModel.OrganizationBean> mSelecteds = new ArrayList<>();
    private OrganSelectedAdapter mOranSelectedAdapter;
    private OrganSelectChildrenAdapter mOrganSelectChildrenAdapter;
    private android.widget.Button mBtConfirm;

    @Override
    public int getContentView() {
        return R.layout.activity_select_organization;
    }

    @Override
    public OrganizationPresenter onCreatePresenter() {
        return new OrganizationPresenter(this);
    }

    @Override
    public void initView() {

        mTitleRobotView = (TitleRobotView) findViewById(R.id.titleRobotView);
        mRvSelected = (RecyclerView) findViewById(R.id.rv_selected);
        mRvSelected.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mRvSelectChildren = (RecyclerView) findViewById(R.id.rv_select_children);
        mRvSelectChildren.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mLoadingView = (LoadingView) findViewById(R.id.loadingView);
        mBtConfirm = (Button) findViewById(R.id.bt_confirm);
        mBtConfirm.setOnClickListener(v -> {
            if (EmptyUtil.isEmpty(mSelecteds)) {
                return;
            }
            LoadingDialog.show(SelectOrganizationActivity.this, "加载中");
            mPresenter.onSelectOrganization(String.valueOf(mSelecteds.get(mSelecteds.size() - 1).getId()));
        });


        List<OrganizationModel.OrganizationBean> list = CommonParameter.getOrganizationSelected();
        if (list != null) {
            mSelecteds.addAll(list);
        }

    }

    @Override
    public void initData() {
        mPresenter.onGetOrganizationList();
    }

    @Override
    public LifecycleProvider getLifecycleProvider() {
        return this;
    }

    @Override
    public void onSuccess(IModel data) {
        if (data instanceof OrganizationModel) {
            mOrganizationModel = (OrganizationModel) data;
            if (mOrganizationModel == null && mOrganizationModel.getOrganization() == null) {
                mLoadingView.showEmpty();
                return;
            }
            if (mSelecteds.isEmpty()) {
                mSelecteds.add(mOrganizationModel.getOrganization());
            }
            mOranSelectedAdapter = new OrganSelectedAdapter(mSelecteds);
            mOranSelectedAdapter.setOnRvItemListener(new OnRvItemListener<OrganizationModel.OrganizationBean>() {
                @Override
                public void onItemClick(List<OrganizationModel.OrganizationBean> list, int position) {

                    if (position == mSelecteds.size() - 1) {
                        mOrganSelectChildrenAdapter.setDataListNotify(mSelecteds.get(position).getChildren());
                        return;
                    }
                    for (int i = mSelecteds.size() - 1; i > position; i--) {
                        mSelecteds.remove(i);
                    }
                    mOranSelectedAdapter.notifyDataSetChanged();
                    mOrganSelectChildrenAdapter.setDataListNotify(mSelecteds.get(position).getChildren());

                }
            });

            mRvSelected.setAdapter(mOranSelectedAdapter);


            mLoadingView.hide();


            mOrganSelectChildrenAdapter = new OrganSelectChildrenAdapter(mOrganizationModel.getOrganization().getChildren());
            mOrganSelectChildrenAdapter.setOnRvItemListener(new OnRvItemListener<OrganizationModel.OrganizationBean>() {
                @Override
                public void onItemClick(List<OrganizationModel.OrganizationBean> list, int position) {
                    OrganizationModel.OrganizationBean bean = list.get(position);
                    mSelecteds.add(bean);
                    mOranSelectedAdapter.notifyDataSetChanged();
                    mOrganSelectChildrenAdapter.setDataListNotify(bean.getChildren());
                }
            });
            mRvSelectChildren.setAdapter(mOrganSelectChildrenAdapter);

            mOranSelectedAdapter.getOnRvItemListener().onItemClick(mSelecteds, mSelecteds.size() - 1);

        } else {
            CommonParameter.setOrganizationSelected(mSelecteds);
            UserManager.getInstance().getUser().setOrganList(mSelecteds);
            UserManager.getInstance().notifyUserInfo();
            LoadingDialog.hide();
            ToastUtil.show("选择成功");
            finish();
        }
    }

    @Override
    public void onFail(ResponeThrowable throwable) {
        if (!EmptyUtil.isEmpty(mSelecteds)) {
            LoadingDialog.hide();
            return;
        }
        mLoadingView.showFail(v -> {
            initData();
        });
    }
}