package com.minminaya.exposure.container;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.annotation.CallSuper;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.minminaya.exposure.pagestate.IPageStateLifecycleOwner;
import com.minminaya.exposure.pagestate.PageLifeCycleHolder;
import com.minminaya.exposure.pagestate.PageState;

/**
 * Author: minminaya  😊😊😊
 * Email: minminaya@gmail.com
 * Date: 2022/3/18 16:39
 */
@Keep
@SuppressWarnings("unused")
public class WrapExposureActivity extends ComponentActivity implements IPageStateLifecycleOwner {

    private PageLifeCycleHolder mPageLifeCycleHolder;

    @NonNull
    @Override
    public PageLifeCycleHolder getPageStateLifecycle() {
        if (mPageLifeCycleHolder == null) {
            initPageLifeCycleHolder();
        }
        return mPageLifeCycleHolder;
    }

    @Override
    public void initPageLifeCycleHolder() {
        mPageLifeCycleHolder = new PageLifeCycleHolder(getLifecycle());
    }

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPageLifeCycleHolder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPageStateLifecycle().onPageState(PageState.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getPageStateLifecycle().onPageState(PageState.INVISIBLE);
    }

}
