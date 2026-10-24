/*
package com.wms;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.wms.common.utils.ApplicationContextHolder;
import com.wms.domain.SysInterfaceLog;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class SysInterfaceLogTest {


    @Test
    public void add(){
        List<SysInterfaceLog> sysInterfaceLogs=new ArrayList<>();
        SysInterfaceLog sysInterfaceLog =new SysInterfaceLog();
        sysInterfaceLog.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        sysInterfaceLog.setInterfaceId(UUID.randomUUID().toString().replaceAll("-", ""));
        sysInterfaceLog.setVersion(1);
        sysInterfaceLog.setIndex(1);
        sysInterfaceLog.setStatus(String.valueOf("status"));
        sysInterfaceLog.setOrgId("1");
        sysInterfaceLog.setSystemId("1");
        sysInterfaceLog.setPurposrId("1");
        sysInterfaceLog.setUrl("1");
        sysInterfaceLog.setRequestHead("1");
        sysInterfaceLog.setRequestParm("1");
        sysInterfaceLog.setIsSort(true);
        sysInterfaceLog.setPactId("1");
        sysInterfaceLog.setModelId("1");
        sysInterfaceLog.setCreateTime(LocalDateTime.now());

        sysInterfaceLogs.add(sysInterfaceLog);

        MybatisBatch<SysInterfaceLog> mybatisBatch = new MybatisBatch<SysInterfaceLog> (ApplicationContextHolder.getBean(SqlSessionFactory.class),
            sysInterfaceLogs);
        MybatisBatch.Method<SysInterfaceLog> method = new MybatisBatch.Method<>(SysInterfaceLogMapper.class);
        mybatisBatch.execute(method.insert());
    }

}
*/
