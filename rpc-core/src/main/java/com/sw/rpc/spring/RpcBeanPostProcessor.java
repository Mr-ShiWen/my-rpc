package com.sw.rpc.spring;

import com.sw.rpc.assist.annotation.RpcServerInfo;
import com.sw.rpc.core.server.RpcServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(value = "spring.rpc.server", havingValue = "on", matchIfMissing = true)
public class RpcBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    RpcServer rpcServer;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RpcServerInfo rpcServerInfo = bean.getClass().getAnnotation(RpcServerInfo.class);
        if (rpcServerInfo != null) {
            rpcServer.addServiceWithBean(bean);
            log.info("successfully add service to rpcService, service class:{}", bean.getClass().getName());
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
