package com.minminaya.exposure.container;


import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.minminaya.exposure.pagestate.IPageStateLifecycleOwner;
import com.minminaya.exposure.pagestate.PageLifeCycleHolder;
import com.minminaya.exposure.pagestate.PageState;


/**
 * Author: minminaya  😊😊😊
 * Email: minminaya@gmail.com
 * Date: 2022/3/18 16:37
 */
@Keep
@SuppressWarnings("unused")
public class WrapExposureFragment extends Fragment implements IPageStateLifecycleOwner {

    /**
     * 曾经有显示过界面
     */
    protected boolean hasResume = false;

    private PageLifeCycleHolder mPageLifeCycleHolder;

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            onFragmentVisible(true);
        }
        hasResume = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isHidden()) {
            onFragmentVisible(false);
        }
        hasResume = false;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hasResume) {
            onFragmentVisible(!hidden);
        }
    }

    /**
     * @param isVisible true 代表显示
     */
    @CallSuper
    protected void onFragmentVisible(boolean isVisible) {
        if (isVisible) {
            getPageStateLifecycle().onPageState(PageState.VISIBLE);
        } else {
            getPageStateLifecycle().onPageState(PageState.INVISIBLE);
        }
    }

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPageLifeCycleHolder();
    }

}
