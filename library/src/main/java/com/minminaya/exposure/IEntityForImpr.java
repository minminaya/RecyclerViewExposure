package com.minminaya.exposure;


/**
 * 统计 RecyclerView 曝光的基类，需要做统计曝光的列表 Adapter 实现该借口，getId只要返回entity的唯一标识码即可
 * <p>
 * Author: minminaya  😊😊😊
 * Email: minminaya@gmail.com
 * Date: 2022/3/16 20:40
 */
public interface IEntityForImpr {

    /**
     * @return 返回item唯一标识
     */
    String getIdForImpr();
}
