package com.thfw.base.net;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.thfw.base.base.UI;
import com.thfw.base.utils.GsonUtil;
import com.thfw.base.utils.LogUtil;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jishuaipeng on 2018/11/7.
 */
public class OkHttpUtil {

    private static final String TAG = OkHttpUtil.class.getSimpleName();

    private static final int DEFAULT_TIMEOUT = 15;
    // 通用拦截
    private static final CommonInterceptor mCommonInterceptor = new CommonInterceptor();

    private static final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

    // Retrofit要求 baseUrl 以 '/' 为结尾
    private static final Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

    // 声明日志拦截器
    private static final HttpLoggingInterceptor httpLoggingInterceptor =
            new HttpLoggingInterceptor(s -> {
                LogUtil.i(TAG, s);
            });
    private static BaseCodeListener mBaseCodeListener;

    public static synchronized <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, null);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl) {
        if (!TextUtils.isEmpty(baseUrl)) {
            retrofitBuilder.baseUrl(baseUrl);
        } else {
            retrofitBuilder.baseUrl(ApiHost.getHost());
        }
        // 清空拦截
        clientBuilder.interceptors().clear();
        // 设置通用拦截器
        clientBuilder.interceptors().add(mCommonInterceptor);
        // 设定日志级别
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(httpLoggingInterceptor);

        // 设置证书
//        try {
//            clientBuilder.sslSocketFactory(RqbTrustManager.getInstance().getSSLSocketFactory());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return retrofitBuilder.client(clientBuilder.build()).build()
                .create(serviceClass);
    }

    private static <T> ObservableTransformer<T, T> transformerScheduler() {
        return new ObservableTransformer() {
            @Override
            public ObservableSource apply(Observable upstream) {
                return upstream.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).filter(new Predicate<HttpResult>() {
                            @Override
                            public boolean test(@NonNull HttpResult result) throws Exception {
                                return dispatchResult(result);
                            }
                        });
            }
        };
    }

    /**
     * response拦截预处理
     */
    private static synchronized boolean dispatchResult(Object response) {
        if (response == null) {
            return true;
        }
        HttpResult httpResult = getHttpResult(response);
        if (httpResult == null) {
            return true;
        }

        if (httpResult.isSuccessful()) {
            return true;
        } else {
            if (httpResult.getCode() == HttpResult.FAIL_TOKEN) {
                // token 失效
                LogUtil.d(TAG, "OkHttpUtil.mBaseCodeListener.onCode(BaseCodeListener.LOGOUT)");
                if (OkHttpUtil.mBaseCodeListener != null) {
                    OkHttpUtil.mBaseCodeListener.onCode(BaseCodeListener.LOGOUT);
                }

            }
        }
        return true;
    }

    @Nullable
    private static HttpResult getHttpResult(Object response) {
        HttpResult httpResult = null;

        if (response instanceof HttpResult) {
            httpResult = (HttpResult) response;
        } else if (response instanceof ResponseBody) {
            ResponseBody responseBody = null;
            try {
                responseBody = (ResponseBody) response;
                httpResult = GsonUtil.fromJson(responseBody.string(), HttpResult.class);
            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage());
            } finally {
                if (responseBody != null) {
                    responseBody.close();
                }
            }
        } else {
            LogUtil.d(TAG, "todo");
        }
        return httpResult;
    }

    @SuppressLint("CheckResult")
    public static <T extends Object, U extends UI> void request(Observable<HttpResult<T>> observable, U resultUI) {
        if (resultUI.getLifecycleProvider() != null) {
            observable = observable.compose(resultUI.getLifecycleProvider().bindToLifecycle());
        }
        observable.compose(OkHttpUtil.transformerScheduler())
                .subscribe(httpResult -> {
                    if (httpResult.isSuccessful()) {
                        resultUI.onSuccess(httpResult.getData());
                    } else {
                        resultUI.onFail(new ResponeThrowable(httpResult.getCode(), httpResult.getMsg()));
                    }
                }, exception -> {
                    resultUI.onFail(ExceptionHandle.handleException(exception));
                });
    }

    public static void request(String url, MultipartBody multipartBody, Callback callback) {
        OkHttpClient httpClient = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder()
                .addHeader("Token", CommonInterceptor.getToken())
                .url(url)
                .post(multipartBody);
        Request request = requestBuilder.build();
        httpClient.newCall(request).enqueue(callback);
        LogUtil.d("MultipartBody -> request ");
    }

    @SuppressLint("CheckResult")
    public static <T extends Object, U extends UI> void requestByHttpResult(Observable<HttpResult<T>> observable, U resultUI) {
        if (resultUI.getLifecycleProvider() != null) {
            observable = observable.compose(resultUI.getLifecycleProvider().bindToLifecycle());
        }
        observable.compose(OkHttpUtil.transformerScheduler())
                .subscribe(httpResult -> {
                    resultUI.onSuccess(httpResult);
                }, exception -> {
                    resultUI.onFail(ExceptionHandle.handleException(exception));
                });
    }

    public static void setBaseCodeListener(BaseCodeListener mBaseCodeListener) {
        OkHttpUtil.mBaseCodeListener = mBaseCodeListener;
    }
}
