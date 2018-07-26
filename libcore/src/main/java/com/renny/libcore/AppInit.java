package com.renny.libcore;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by rjn on 2017/7/23.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface AppInit {

    boolean background() default false;

    boolean inChildProcess() default true;

    boolean onlyInDebug() default false;

    int priority() default 0;

    long delay() default 0L;

}
