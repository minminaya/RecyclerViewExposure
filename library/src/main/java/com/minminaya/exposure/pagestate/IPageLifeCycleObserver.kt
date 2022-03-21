package com.minminaya.exposure.pagestate

/**
 * 页面可见不可见的周期观察者
 *
 * Author: minminaya  😊😊😊
 * Email: minminaya@gmail.com
 * Date: 2022/3/16 20:40
 */
interface IPageLifeCycleObserver : IPageStateObserver {
    /**
     * 页面可见的时候回调
     * @return Unit
     */
    fun onPageVisible()

    /**
     * 页面不可见的时候回调
     * @return Unit
     */
    fun onPageInvisible()

    fun onDestroy()
}