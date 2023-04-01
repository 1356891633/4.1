package com.aabb.util;

import java.util.Collection;

/**
 * 工具类
 */
public class CommonUtil {


    /**
     * 判断集合容易是否为空
     *
     * @param collection
     * @return
     */
    public static Boolean isEmptyCollection(Collection collection) {
        return collection == null || collection.isEmpty();
    }


}
