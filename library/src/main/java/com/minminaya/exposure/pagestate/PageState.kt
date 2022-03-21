package com.minminaya.exposure.pagestate

/**
 *
 * 界面相对用户态的的状态枚举
 *
 * Author: minminaya  😊😊😊
 * Email: minminaya@gmail.com
 * Date: 2022/3/16 20:40
 */
enum class PageState(val number: Int) {
    /**
     * 可见状态
     */
    VISIBLE(2),

    /**
     * 不可见状态
     */
    INVISIBLE(3),
}