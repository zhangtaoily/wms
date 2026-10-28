# 云梭智能仓储管理系统（WMS）

基于 RuoYi-Vue-Plus 4.8.0 二次开发的仓储管理系统，通过动态表单引擎驱动出入库单据、库存台账、立库作业等业务闭环。

## 仓库组成

仓库根目录除工程本体 `wms/` 外，还包含配套设计与原型资产：

| 路径 | 说明 |
| --- | --- |
| `wms/` | 工程本体（Maven 多模块后端 + wms-ui 前端） |
| `prototype/` | 交互式静态原型（依据生产 WMS 界面还原） |
| `WMS开发计划.html` | M1~M7 迭代开发计划 |
| `WMS智能仓储系统PRD.html` | 产品需求文档 |
| `wms_table_design.sql` | 业务表结构设计 |

## 软件架构

### 技术栈

- 后端：Spring Boot 2.7.18 / Java 8 / Sa-Token / MyBatis-Plus / Redisson / Hutool
- 前端：Vue 2 + Element UI（vue-cli）

### 后端模块

```
wms
├── wms-admin            启动模块（fat jar，默认端口 8888）
├── wms-framework        框架封装（Web、安全、数据源等配置）
├── wms-common           通用工具与基础组件
├── wms-core             核心域
├── wms-service          业务服务
│   ├── wms-service-base     业务单据 / 动态表单引擎
│   ├── wms-service-system   系统管理
│   ├── wms-service-asrs     立库作业（仓储任务、作业任务、作业日志）
│   ├── wms-service-sync     外部同步（ERP 同步桩）
│   ├── wms-service-job      任务调度
│   └── wms-service-common   业务公共
├── wms-sdk
│   └── wms-oss              对象存储
└── wms-extend
    ├── wms-monitor-admin    Spring Boot Admin 监控中心
    └── wms-xxl-job-admin    XXL-Job 调度中心
```

### 动态表单引擎

业务单据（入库单、出库通知、出库单、仓储任务、作业任务、作业日志等）基于动态表单引擎实现，核心要点：

- 功能配置种子表：`sys_func_module`（功能/菜单）、`sys_field_config`（列表列配置）、`sys_auto_code_set`（单据自动取号）
- 单据状态机：pactState `0 草稿 → 1 制单中 → 2 审核中 → 3 通过 / 4 驳回`，支持提交、生效、驳回、红冲
- 单据生效后通过 `afterCommit` 异步触发 ERP 同步（wms-service-sync）
- 出库扣账逐仓位条件 UPDATE 防超扣，红冲自动生成反向单据并回加库存

## 环境要求

- JDK 8、Maven 3.8+
- Node.js 16+（构建脚本已内置 `--openssl-legacy-provider`，兼容新版 Node）
- MySQL（数据库 `wms`）
- Redis

## 快速开始

### 后端

```bash
cd wms
mvn -pl wms-admin -am clean package -DskipTests
java -jar wms-admin/target/wms-admin.jar
```

- 默认 profile `dev`，服务端口 `8888`
- 数据库、Redis 连接配置见 `wms-admin/src/main/resources/application-dev.yml`

### 默认账号

| 账号 | 密码 | 说明 |
| --- | --- | --- |
| `admin` | `admin123` | 超级管理员，拥有全部权限 |

- 登录验证码默认关闭（`captchaEnabled=false`）
- 新建用户初始密码由系统参数 `sys.user.initPassword` 控制（当前为 `Xa888888.`），用户管理页「默认密码」按钮可查看

### 前端

```bash
cd wms/wms-ui
npm install
npm run dev          # 开发模式，端口 80，代理 /dev-api -> http://localhost:8888
npm run build:prod   # 生产构建
```

## 相关文档

- [部署运维手册](wms/script/deploy/部署运维手册.md)
- [迭代评审记录](wms/docs/迭代评审记录.md)
- [SQL 脚本](wms/script/sql/)：M5/M6/M7 初始化、菜单调整、CRM 遗留清理等
