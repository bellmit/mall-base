package com.yan.mall.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Created by huyan on 2021/12/8.
 * TIME: 8:48
 * DESC:
 */
public class CommonUtil {

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj.toString().trim().isEmpty()) {
            return true;
        }
        if ((obj instanceof Collection)) {
            return ((Collection) obj).size() == 0;
        }
        if ((obj instanceof Map)) {
            return ((Map) obj).size() == 0;
        }
        return false;
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }
}
