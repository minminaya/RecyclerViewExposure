package com.minminaya.exposure

/**
 * 列表曝光帮助类接口
 *
 * Author: minminaya  😊😊😊
 * Email: minminaya@gmail.com
 * Date: 2022/3/16
 */
interface IListImpEventHelper {

    /**
     * 获取需要过滤掉列表前面需要曝光的数量【头部item的数量】
     *
     * @return Unit
     */
    fun getHeaderPositionCount(): Int

}