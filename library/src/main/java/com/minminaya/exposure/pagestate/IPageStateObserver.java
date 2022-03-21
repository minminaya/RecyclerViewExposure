package com.minminaya.exposure.pagestate;


/**
 * 页面状态的观察者抽象接口
 *
 * Author: minminaya  😊😊😊
 * Email: minminaya@gmail.com
 * Date: 2022/3/16 20:40
 */
public interface IPageStateObserver {
    default void onPageState(PageState pageState) {

    }
}
