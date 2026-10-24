# 云梭智能仓储管理系统（WMS）是基于 RuoYi-Vue-Plus 同构增强基座开发的纱线纺织仓储执行系统，
为浙江云梭纺织股份有限公司定制。覆盖入库/出库闭环、库内管理（移库/盘存）、立库终端与 PDA、ERP 同步对账全链路。

**版本**: 5.0.0  
**技术栈**: Spring Boot 2.7.18 + Vue 2 + Element UI  
**数据库**: MySQL 8.0+ / Redis  

## 项目结构

```
wms/
├── wms-admin/                 # 主应用（单体启动入口）
├── wms-common/                # 公共模块（枚举/工具/异常）
├── wms-core/                  # 核心实体与引擎接口模块
├── wms-framework/             # 框架配置（安全/缓存/拦截）
├── wms-sdk/
│   └── wms-oss/               # 对象存储客户端（文件上传）
├── wms-service/
│   ├── wms-service-common/    # 动态列表/表单引擎、状态机
│   ├── wms-service-system/    # 系统管理（用户/角色/字典/字段配置）
│   ├── wms-service-base/      # 单据域（出入库/移库/盘存/台账/ERP同步桩）
│   ├── wms-service-sync/      # 同步补偿任务与对账（M7）
│   ├── wms-service-asrs/      # 立库服务（预留）
│   └── wms-service-job/       # 定时任务执行器
├── wms-extend/
│   ├── wms-monitor-admin/     # Spring Boot Admin 监控中心
│   └── wms-xxl-job-admin/     # XXL-Job 调度中心
├── wms-ui/                    # 前端（PC 管理端 + 立库扫码终端 /pda /terminal/scan）
└── script/                    # 初始化 SQL 与部署交付物
```

## 技术栈详情

### 后端技术栈
- **框架**: Spring Boot 2.7.18
- **ORM**: MyBatis-Plus 3.5.4
- **数据库**: MySQL 8.0+
- **缓存**: Redis + Redisson
- **安全**: Sa-Token 1.37.0
- **配置中心**: Nacos
- **任务调度**: XXL-Job 2.4.0
- **文档**: SpringDoc OpenAPI 3.0
- **工具库**: Hutool 5.8.22

### 前端技术栈
- **框架**: Vue 3 + Element Plus
- **构建工具**: Vite
- **包管理**: pnpm
- **HTTP客户端**: Alova
- **路由**: Vue Router 4
- **状态管理**: Pinia

### 开发工具
- **JDK**: 1.8+
- **Maven**: 3.6+
- **Node.js**: 16+
- **IDE**: VS Code / IntelliJ IDEA

## 环境要求

### 开发环境
- Java 8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+
- Maven 3.6+

### 生产环境
- Windows Server 2019 / CentOS 7+
- 内存: 32G+
- CPU: 32核心+
- 数据库: MySQL 8.0+
- 缓存: Redis 6.0+

```

### 2. 后端启动

```bash
# 编译项目
mvn clean compile

# 启动开发环境
mvn spring-boot:run -Pdev

# 或直接运行jar包
java -jar wms-admin/target/wms-admin.jar --spring.profiles.active=dev
```

### 3. 前端启动

```bash
# 进入前端目录
cd wms-ui

# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev

# 构建生产版本
pnpm build
```

## 配置文件说明

### 数据库配置

**开发环境** (`application-dev.yml`):
```yaml
spring:
  datasource:
    url: jdbc:mysql://192.168.0.75:3306/crm?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: test123456
```

**生产环境** (`application-prod.yml`):
```yaml
spring:
  datasource:
    url: jdbc:mysql://192.168.0.91:3306/crm?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: wms2024
    password: Wms123!@#
```

### Nacos配置中心

系统使用Nacos作为配置中心，配置地址：`192.168.0.92:8848`

## 核心功能模块

### 1. 客户管理
- 客户基本信息管理
- 客户联系人管理
- 客户信用管理
- 客户分组管理

### 2. 销售管理
- 销售合同管理
- 销售订单管理
- 销售报价管理
- 销售目标管理

### 3. 产品管理
- 产品信息管理
- 产品分类管理
- 产品库存管理
- 产品价格管理

### 4. 财务管理
- 应收账款管理
- 付款计划管理
- 发票管理
- 财务报表

### 5. 系统管理
- 用户权限管理
- 角色管理
- 菜单管理
- 数据字典
- 系统监控

## 集成功能

### ERP集成
- 与金蝶K3 Cloud深度集成
- 数据双向同步
- 业务单据对接

### 报表系统
- 集成帆软报表
- 自定义报表模板
- 数据可视化展示

### 工作流
- 业务流程审批
- 自定义工作流
- 审批记录跟踪

## 开发规范

### 代码规范
- 遵循阿里巴巴Java开发规范
- 使用MyBatis-Plus代码生成器
- 统一异常处理
- 统一返回格式

### 数据库规范
- 表名使用下划线分隔
- 主键使用雪花算法生成
- 逻辑删除字段统一使用`del_flag`
- 时间字段统一使用`create_time`、`update_time`

### API规范
- RESTful API设计
- 统一响应格式
- 接口文档自动生成

## 部署说明

### Windows部署

1. **安装依赖服务**
   - MySQL 8.0+
   - Redis 6.0+
   - Nginx 1.20+

2. **配置Nginx**
```nginx
server {
    listen       80;
    server_name  localhost;
    
    # 前端静态资源
    location / {
        root   html/dist;
        index  index.html index.htm;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端API代理
    location /prod-api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

3. **启动服务**
```bash
# 启动后端服务
java -jar wms-admin.jar --spring.profiles.active=prod

# 启动Nginx
nginx.exe
```

## 常见问题

### 1. 数据库连接失败
**问题**: Communications link failure
**解决方案**: 
- 检查MySQL服务是否启动
- 验证数据库连接配置
- 检查防火墙设置

### 2. 时区配置错误
**问题**: The server time zone value is unrecognized
**解决方案**:
- 在JDBC URL中添加`serverTimezone=Asia/Shanghai`
- 修改MySQL服务器时区设置

### 3. 前端构建失败
**问题**: 依赖安装失败
**解决方案**:
- 使用pnpm替代npm
- 清除缓存重新安装
- 检查Node.js版本兼容性

### 4. 权限认证失败
**问题**: Token验证失败
**解决方案**:
- 检查Sa-Token配置
- 验证Redis连接
- 检查Token过期时间设置

## 维护指南

### 日志管理
- 日志文件位置: `logs/`
- 日志级别配置: `logback-plus.xml`
- 日志分割策略: 按天分割，保留30天

### 监控告警
- 系统监控: Spring Boot Admin
- 性能监控: JVM监控
- 业务监控: 自定义监控指标

### 备份策略
- 数据库备份: 每日自动备份
- 配置文件备份: 版本控制
- 日志文件备份: 定期归档

## 技术支持

### 开发团队
- 项目经理: 方垚嘉
- 后端开发: 技术团队
- 前端开发: 技术团队
- 测试团队: 质量保证组

## 版本历史

### v5.0.0 (当前版本)
- 优化信用管理模块
- 修复数据库连接问题
- 提升系统性能

### v4.8.0
- 集成金蝶K3 Cloud API
- 新增报表功能
- 优化用户界面

### v4.7.0
- 重构权限管理系统
- 新增工作流引擎
- 优化数据同步机制

---

**注意**: 本文档会随着系统更新而持续维护，请定期查看最新版本。