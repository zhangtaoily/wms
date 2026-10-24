package com.wms.framework.config;

import cn.hutool.core.net.NetUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.wms.common.interceptor.SqlInjectionInterceptor;
import com.wms.framework.handler.CreateAndUpdateMetaObjectHandler;
import com.wms.framework.interceptor.PlusDataPermissionInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

/**
 * mybatis-plus配置类(下方注释有插件介绍)
 *
 * @author Lion Li
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Configuration
@MapperScan("${mybatis-plus.mapperPackage}")
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 数据权限处理
        interceptor.addInnerInterceptor(dataPermissionInterceptor());
        // 分页插件
        interceptor.addInnerInterceptor(paginationInnerInterceptor());
        // 乐观锁插件
        interceptor.addInnerInterceptor(optimisticLockerInnerInterceptor());

        return interceptor;
    }

    /**
     * 数据权限拦截器
     */
    public PlusDataPermissionInterceptor dataPermissionInterceptor() {
        return new PlusDataPermissionInterceptor();
    }

    /**
     * 分页插件，自动识别数据库类型
     */
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 明确设置数据库类型为MySQL（根据项目实际使用的数据库）
        paginationInnerInterceptor.setDbType(com.baomidou.mybatisplus.annotation.DbType.MYSQL);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInnerInterceptor.setMaxLimit(-1L);
        // 分页合理化
        paginationInnerInterceptor.setOverflow(true);
        return paginationInnerInterceptor;
    }

    /**
     * 乐观锁插件
     */
    public OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor() {
        return new OptimisticLockerInnerInterceptor();
    }

    /**
     * 元对象字段填充控制器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new CreateAndUpdateMetaObjectHandler();
    }

    /**
     * 使用网卡信息绑定雪花生成器
     * 防止集群雪花ID重复
     */
    @Bean
    public IdentifierGenerator idGenerator() {
        return new DefaultIdentifierGenerator(NetUtil.getLocalhost());
    }

    /**
     * PaginationInnerInterceptor 分页插件，自动识别数据库类型
     * https://baomidou.com/pages/97710a/
     * OptimisticLockerInnerInterceptor 乐观锁插件
     * https://baomidou.com/pages/0d93c0/
     * MetaObjectHandler 元对象字段填充控制器
     * https://baomidou.com/pages/4c6bcf/
     * ISqlInjector sql注入器
     * https://baomidou.com/pages/42ea4a/
     * BlockAttackInnerInterceptor 如果是对全表的删除或更新操作，就会终止该操作
     * https://baomidou.com/pages/f9a237/
     * IllegalSQLInnerInterceptor sql性能规范插件(垃圾SQL拦截)
     * IdentifierGenerator 自定义主键策略
     * https://baomidou.com/pages/568eb2/
     * TenantLineInnerInterceptor 多租户插件
     * https://baomidou.com/pages/aef2f2/
     * DynamicTableNameInnerInterceptor 动态表名插件
     * https://baomidou.com/pages/2a45ff/
     */

    /**
     * SQL注入检测拦截器
     */
    @Bean
    @ConditionalOnProperty(name = "sql.injection.detection.enabled", havingValue = "true", matchIfMissing = false)
    public SqlInjectionInterceptor sqlInjectionInterceptor() {
        SqlInjectionInterceptor interceptor = new SqlInjectionInterceptor();

        // 配置拦截器属性
        Properties properties = new Properties();
        properties.setProperty("enabled", "true");
        properties.setProperty("logWarning", "true");
        properties.setProperty("throwException", "false"); // 生产环境建议设置为false

        interceptor.setProperties(properties);
        return interceptor;
    }

    /**
     * 使用BeanPostProcessor来注册拦截器，避免循环依赖
     */
    @Bean
    @ConditionalOnProperty(name = "sql.injection.detection.enabled", havingValue = "true", matchIfMissing = false)
    public BeanPostProcessor sqlInjectionInterceptorProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                // 当SqlSessionFactory Bean初始化完成后，注册拦截器
                if (bean instanceof SqlSessionFactory) {
                    SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) bean;
                    SqlInjectionInterceptor interceptor = sqlInjectionInterceptor();

                    org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
                    if (!configuration.getInterceptors().contains(interceptor)) {
                        configuration.addInterceptor(interceptor);
                    }
                }
                return bean;
            }
        };
    }
}
