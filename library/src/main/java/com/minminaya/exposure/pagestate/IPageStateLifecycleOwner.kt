package com.minminaya.exposure.pagestate


/**
 *
 * 页面展示状态 life cycle 拥有者对外接口
 *
 * Author: minminaya  😊😊😊
 * Email: minminaya@gmail.com
 * Date: 2022/3/21 17:50
 *
 */
interface IPageStateLifecycleOwner {
    /**
     * 获取页面展示状态周期持有者
     * @return PageLifeCycleHolder
     */
    fun getPageStateLifecycle(): PageLifeCycleHolder

    fun initPageLifeCycleHolder()

}