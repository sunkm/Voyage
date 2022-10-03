package com.manchuan.tools.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 随机键盘类型
 *
 * @author Henley
 * @date 2018/6/25 14:38
 */
@IntDef({
        RandomType.TYPE_NUMBER,
        RandomType.TYPE_LETTER,
        RandomType.TYPE_SYMBOL
})
@Retention(RetentionPolicy.SOURCE)
public @interface RandomType {

    /**
     * 数字
     */
    int TYPE_NUMBER = 1;
    /**
     * 字母
     */
    int TYPE_LETTER = 2;
    /**
     * 符号
     */
    int TYPE_SYMBOL = 3;

}

