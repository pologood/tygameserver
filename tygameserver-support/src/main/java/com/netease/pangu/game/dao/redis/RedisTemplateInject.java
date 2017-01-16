package com.netease.pangu.game.dao.redis;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RedisTemplateInject {
    public String value() default "";
}
