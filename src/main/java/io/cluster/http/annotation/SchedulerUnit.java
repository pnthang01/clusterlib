/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.http.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author thangpham
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface SchedulerUnit {

    public String name();

    public long delay() default 3;

    public long period() default 3;
    
    public TimeUnit timeUnit() default TimeUnit.SECONDS ;
    
    public int type() default FIX_RATE_TYPE;
    
    public String schedulePattern() default "";
    
    public static int FIX_RATE_TYPE = 1;
    public static int FIX_TIME_TYPE = 2;
}
