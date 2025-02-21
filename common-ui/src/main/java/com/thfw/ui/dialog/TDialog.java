package com.thfw.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.thfw.base.utils.LogUtil;
import com.thfw.ui.R;
import com.thfw.ui.dialog.base.BaseDialogFragment;
import com.thfw.ui.dialog.base.BindViewHolder;
import com.thfw.ui.dialog.base.TController;
import com.thfw.ui.dialog.listener.OnBindViewListener;
import com.thfw.ui.dialog.listener.OnViewClickListener;


/**
 * 1.0.0版本: 弹窗实现基本功能
 * OnBindViewListener
 * 1.1.0版本: 添加点击事件封装回调方法
 * addOnClickListener()
 * setOnViewClickListener()
 * 1.2.0版本:
 * 分离出列表弹窗TListDialog
 * 解决弹窗按Home键时出现的bug
 * 1.3.0版本:
 * 处理setCancelable()方法,禁止弹窗点击取消
 * 弹窗内容直接传入View: setDialogView()
 * 1.3.1版本:
 * 添加弹窗隐藏时回调监听方法:setOnDismissListener()
 *
 * @author Timmy
 * @time 2018/1/4 16:28
 * @GitHub https://github.com/Timmy-zzh/TDialog
 **/
public class TDialog extends BaseDialogFragment {

    private static final String KEY_TCONTROLLER = "TController";
    private static final float WIDTHASPECT = 0.8f;
    private static final float DIMAMOUNT = 0.3f;
    private static final float HEIGHTASPECT = 0.6f;
    private static Context appContext;
    protected TController tController;

    public TDialog() {
        tController = new TController();
    }

    public static void init(Context appContext) {
        TDialog.appContext = appContext.getApplicationContext();
    }

    /**
     * 当设备旋转时,会重新调用onCreate,进行数据恢复
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            tController = (TController) savedInstanceState.getSerializable(KEY_TCONTROLLER);
        }
    }

    /**
     * 进行数据保存
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_TCONTROLLER, tController);
        super.onSaveInstanceState(outState);
    }

    /**
     * 弹窗消失时回调方法
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        DialogInterface.OnDismissListener onDismissListener = tController.getOnDismissListener();
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    /**
     * 获取弹窗xml布局界面
     */
    @Override
    protected int getLayoutRes() {
        return tController.getLayoutRes();
    }

    @Override
    protected View getDialogView() {
        return tController.getDialogView();
    }

    @Override
    protected void bindView(View view) {
        //控件点击事件处理
        BindViewHolder viewHolder = new BindViewHolder(view, this);
        if (tController.getIds() != null && tController.getIds().length > 0) {
            for (int id : tController.getIds()) {
                viewHolder.addOnClickListener(id);
            }
        }
        //回调方法获取到布局,进行处理
        if (tController.getOnBindViewListener() != null) {
            tController.getOnBindViewListener().bindView(viewHolder);
        }
    }

    @Override
    public int getGravity() {
        return tController.getGravity();
    }

    @Override
    public float getDimAmount() {
        return tController.getDimAmount();
    }

    @Override
    public int getDialogHeight() {
        return tController.getHeight();
    }

    @Override
    public int getDialogWidth() {
        return tController.getWidth();
    }

    @Override
    public String getFragmentTag() {
        return tController.getTag();
    }

    public OnViewClickListener getOnViewClickListener() {
        return tController.getOnViewClickListener();
    }

    @Override
    protected boolean isCancelableOutside() {
        return tController.isCancelableOutside();
    }

    @Override
    protected int getDialogAnimationRes() {
        return tController.getDialogAnimationRes();
    }

    @Override
    protected DialogInterface.OnKeyListener getOnKeyListener() {
        return tController.getOnKeyListener();
    }

    public TDialog show() {
        Log.d(TAG, "show");
        try {
            FragmentTransaction ft = tController.getFragmentManager().beginTransaction();
            ft.add(this, tController.getTag());
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            Log.e("TDialog", e.toString());
        }
        return this;
    }

    /*********************************************************************
     * 使用Builder模式实现
     *
     */
    public static class Builder {

        TController.TParams params;

        public Builder(FragmentManager fragmentManager) {
            params = new TController.TParams();
            params.mFragmentManager = fragmentManager;
        }

        /**
         * 传入弹窗xmL布局文件
         *
         * @param layoutRes
         * @return
         */
        public Builder setLayoutRes(@LayoutRes int layoutRes) {
            params.mLayoutRes = layoutRes;
            return this;
        }

        /**
         * 直接传入控件
         *
         * @param view
         * @return
         */
        public Builder setDialogView(View view) {
            params.mDialogView = view;
            return this;
        }

        /**
         * 设置弹窗宽度(单位:像素)
         *
         * @param widthPx
         * @return
         */
        public Builder setWidth(int widthPx) {
            params.mWidth = widthPx;
            return this;
        }

        /**
         * 设置弹窗高度(px)
         *
         * @param heightPx
         * @return
         */
        public Builder setHeight(int heightPx) {
            params.mHeight = heightPx;
            return this;
        }

        /**
         * 设置弹窗宽度是屏幕宽度的比例 0 -1
         */
        public Builder setScreenWidthAspect(Context context, float widthAspect) {
            params.mWidth = (int) (getScreenWidth(context) * widthAspect);
            return this;
        }

        /**
         * 设置弹窗高度是屏幕高度的比例 0 -1
         */
        public Builder setScreenHeightAspect(Context context, float heightAspect) {
            params.mHeight = (int) (getScreenHeight(context) * heightAspect);
            return this;
        }

        /**
         * 设置弹窗高度是屏幕高度的比例 0 -1
         */
        public Builder setScreenHeightAspect(Context context) {
            params.mHeight = (int) (getScreenHeight(context) * HEIGHTASPECT);
            return this;
        }


        /**
         * 设置弹窗在屏幕中显示的位置
         *
         * @param gravity
         * @return
         */
        public Builder setGravity(int gravity) {
            params.mGravity = gravity;
            return this;
        }

        /**
         * 设置弹窗在弹窗区域外是否可以取消
         *
         * @param cancel
         * @return
         */
        public Builder setCancelableOutside(boolean cancel) {
            params.mIsCancelableOutside = cancel;
            return this;
        }

        /**
         * 弹窗dismiss时监听回调方法
         *
         * @param dismissListener
         * @return
         */
        public Builder setOnDismissListener(DialogInterface.OnDismissListener dismissListener) {
            params.mOnDismissListener = dismissListener;
            return this;
        }


        /**
         * 设置弹窗背景透明度(0-1f)
         *
         * @param dim
         * @return
         */
        public Builder setDimAmount(float dim) {
            params.mDimAmount = dim;
            return this;
        }

        public Builder setTag(String tag) {
            params.mTag = tag;
            return this;
        }

        /**
         * 通过回调拿到弹窗布局控件对象
         *
         * @param listener
         * @return
         */
        public Builder setOnBindViewListener(OnBindViewListener listener) {
            params.bindViewListener = listener;
            return this;
        }

        /**
         * 添加弹窗控件的点击事件
         *
         * @param ids 传入需要点击的控件id
         * @return
         */
        public Builder addOnClickListener(int... ids) {
            params.ids = ids;
            return this;
        }

        /**
         * 弹窗控件点击回调
         *
         * @param listener
         * @return
         */
        public Builder setOnViewClickListener(OnViewClickListener listener) {
            params.mOnViewClickListener = listener;
            return this;
        }

        /**
         * 设置弹窗动画
         *
         * @param animationRes
         * @return
         */
        public Builder setDialogAnimationRes(int animationRes) {
            params.mDialogAnimationRes = animationRes;
            return this;
        }

        public Builder setDialogNotAnimation() {
            params.mDialogAnimationRes = -1;
            return this;
        }

        /**
         * 监听弹窗后，返回键点击事件
         */
        public Builder setOnKeyListener(DialogInterface.OnKeyListener keyListener) {
            params.mKeyListener = keyListener;
            return this;
        }

        /**
         * 真正创建TDialog对象实例
         *
         * @return
         */
        public TDialog create() {
            TDialog dialog = new TDialog();
            Log.d(TAG, "create");
            // 将数据从Buidler的DjParams中传递到DjDialog中
            setDefalutParams();
            params.apply(dialog.tController);
            return dialog;
        }

        /**
         * 设置默认参数
         */
        public void setDefalutParams() {
            if (params.mDialogAnimationRes == 0) {
                LogUtil.i("setDefalutParams -> mDialogAnimationRes");
                params.mDialogAnimationRes = R.style.animate_dialog;
            } else if (params.mDialogAnimationRes == -1) {
                params.mDialogAnimationRes = 0;
            }
            if (params.mDimAmount <= 0) {
                LogUtil.i("setDefalutParams -> mDimAmount");
                params.mDimAmount = DIMAMOUNT;
            }
            if (params.mWidth <= 0 && appContext != null) {
                LogUtil.i("setDefalutParams -> WIDTHASPECT");
                setScreenWidthAspect(appContext, WIDTHASPECT);
            }
        }
    }
}
