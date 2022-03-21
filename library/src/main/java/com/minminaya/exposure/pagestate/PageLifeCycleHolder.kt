package com.minminaya.exposure.pagestate

import androidx.annotation.MainThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 页面可见不可见状态Life Cycle 控制器
 *
 * Author: minminaya  😊😊😊
 * Email: minminaya@gmail.com
 * Date: 2022/3/16 20:40
 */
class PageLifeCycleHolder(private val lifecycle: Lifecycle) : LifecycleObserver,
    IPageStateObserver {

    var pageState: PageState = PageState.INVISIBLE

    private val pageLifeCycleObserverList by lazy {
        return@lazy mutableListOf<IPageLifeCycleObserver>()
    }

    /**
     * 内部会自动解绑
     *
     * @param observer IPageLifeCycleObserver
     */
    @MainThread
    fun addPageObserver(observer: IPageLifeCycleObserver) {
        if (pageLifeCycleObserverList.contains(observer)) {
            return
        }
        pageLifeCycleObserverList.add(observer)
    }

    @MainThread
    fun removePageObserver(observer: IPageLifeCycleObserver) {
        pageLifeCycleObserverList.remove(observer)
    }

    private fun onDestroy() {
        pageLifeCycleObserverList.forEach {
            it.onDestroy()
        }
        lifecycle.removeObserver(this)
        pageLifeCycleObserverList.clear()
    }

    override fun onPageState(pageState: PageState) {
        if (this.pageState == pageState) {
            //避免相同状态回调多次
            return
        }
        this.pageState = pageState
        pageLifeCycleObserverList.forEach {
            it.onPageState(pageState)
            when (pageState) {
                PageState.VISIBLE -> {
                    it.onPageVisible()
                }
                PageState.INVISIBLE -> {
                    it.onPageInvisible()
                }
            }
        }
    }

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                onDestroy()
            }
        })
    }

}