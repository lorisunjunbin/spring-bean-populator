package cn.lori.bean.populator.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static cn.lori.bean.populator.model.PopulatorResource.DEFAULT_TARGET;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
public @interface PopulatorItem {

    String code() default "";

    String target() default DEFAULT_TARGET;

    String categoryCode() default "";

    /**
     * will be overwritten once resource is available.
     */
    String defaultValue() default "";
}
