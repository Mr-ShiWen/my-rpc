package com.sw.rpc.assist.annotation;

import java.lang.annotation.*;

import static com.sw.rpc.constant.Constant.DEFAULT_SERVICE_IMPL_NAME;

/**
 * @author sw
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.FIELD})
@Inherited
@Documented
public @interface RpcServerInfo {

    /**
     * Service group, default value is empty string
     */
    String implName() default DEFAULT_SERVICE_IMPL_NAME;
}
