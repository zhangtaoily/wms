package com.wms.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHolder implements BeanFactoryPostProcessor, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public ApplicationContextHolder() {
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }

    @Override
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static class Properties {

        public static Long longValue(String name, Long defaultValue) {
            return applicationContext.getEnvironment().getProperty(name, Long.class, defaultValue);
        }

        public static Long longValue(String name) {
            return applicationContext.getEnvironment().getProperty(name, Long.class);
        }

        public static Boolean booleanValue(String name, Boolean defaultValue) {
            return applicationContext.getEnvironment().getProperty(name, Boolean.class, defaultValue);
        }

        public static Boolean booleanValue(String name) {
            return applicationContext.getEnvironment().getProperty(name, Boolean.class);
        }

        public static String value(String name, String defaultValue) {
            return applicationContext.getEnvironment().getProperty(name, defaultValue);
        }

        public static String value(String name) {
            return applicationContext.getEnvironment().getProperty(name);
        }

        public static Integer intValue(String name) {
            return ApplicationContextHolder.getApplicationContext().getEnvironment().getProperty(name, Integer.class);
        }

        public static Integer intValue(String name, Integer defaultValue) {
            return ApplicationContextHolder.getApplicationContext().getEnvironment().getProperty(name, Integer.class, defaultValue);
        }
    }

}
