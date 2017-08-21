package com.hujian.switcher.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hujian06 on 2017/8/21.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Switcher {

    /**
     * first choose
     * @return
     */
    String switchToExecutorServiceType() default "";

    /**
     * second choose
     * @return
     */
    String switchToExecutorServiceName() default "";

    /**
     * third choose
     * @return
     */
    String CreateType() default "";

}
