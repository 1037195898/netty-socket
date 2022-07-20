package com.annotation;

import java.lang.annotation.*;

/**
 * 数据处理标签
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface SocketAction {

    /** 注册的事件编号 */
    int value();

}
