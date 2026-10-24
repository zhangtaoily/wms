package com.wms.common.aspect;

import com.wms.common.api.ValidateAble;
import com.wms.common.utils.ListUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 对 Controller 层的参数进行统一检查
 */
@Slf4j
@Aspect
@Component
public class ControllerBOValidateAspect {

    @Pointcut("execution(* com.wms.web.controller..*Controller.*(..))")
    private void controllerMethodAspect() {
        // DO NOTHING
    }

    @Before("controllerMethodAspect()")
    public void before(JoinPoint jp) {
        ListUtils.adaptArray(jp.getArgs())
            .select(ValidateAble.class::isInstance)
            .forEach(arg -> ((ValidateAble) arg).validate());
    }
}
