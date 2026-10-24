package com.wms.framework.config;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.exception.BackResultException;
import cn.dev33.satoken.exception.StopMatchException;
import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.strategy.SaStrategy;
import com.wms.common.utils.StringUtils;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class CrmSaInterceptor extends SaInterceptor {
    public CrmSaInterceptor() {
    }

    public CrmSaInterceptor(SaParamFunction<Object> auth) {
        this.auth = auth;
    }

    public CrmSaInterceptor isAnnotation(boolean isAnnotation) {
        this.isAnnotation = isAnnotation;
        return this;
    }

    public CrmSaInterceptor setAuth(SaParamFunction<Object> auth) {
        this.auth = auth;
        return this;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //免登录
        if (StringUtils.isNotEmpty(request.getParameter("oa-token")) &&
            request.getParameter("oa-token").equals("wms2024")) {
            return true;
        }

        try {
            if (this.isAnnotation && handler instanceof HandlerMethod) {
                Method method = ((HandlerMethod) handler).getMethod();
                if ((Boolean) SaStrategy.instance.isAnnotationPresent.apply(method, SaIgnore.class)) {
                    return true;
                }

                SaStrategy.instance.checkMethodAnnotation.accept(method);
            }

            this.auth.run(handler);
        } catch (StopMatchException var5) {
        } catch (BackResultException var6) {
            if (response.getContentType() == null) {
                response.setContentType("text/plain; charset=utf-8");
            }

            response.getWriter().print(var6.getMessage());
            return false;
        }

        return true;
    }

}

