package cn.lori.bean.populator.annotation;

import cn.lori.bean.populator.service.PopulateService;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface PopulatorCategory {

    String[] codes() default "";

    String name() default "";

    /**
     * true, only populate the fields which annotated by PopulatorItem
     */
    boolean populateAnnotatedFieldsOnly() default false;

    /**
     * true, will refresh by timer task, the interval pls refer to: {@link PopulateService#syncIntervalInMinutes()}
     */
    boolean autoSync() default false;
}



