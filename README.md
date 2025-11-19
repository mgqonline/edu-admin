# 培训学校教务管理系统（框架）

本项目根据《教务管理系统开发文档》搭建完整的多端骨架：后端（区分管理后台服务与微信端API）、管理后台前端、微信小程序。

## 项目结构

- `edu-admin-backend/` 后端（Spring Boot 2.7 + JDK 8），仅区分两个服务：
  - `admin-server/` 管理后台服务（统一响应与异常处理已内置，包含系统、课程、排课、签到、教师、财务、报表等控制器）
  - `wechat-api/` 微信端API服务（统一响应与异常处理已内置，提供学员课表、教师发起签到等接口）

- `admin-frontend/` 管理后台前端（Vue 2.7 + Ant Design Vue + Vite）
  - 路由与页面：`/attendance` 签到统计页面（课时选择、学员列表、统计卡片）
  - 代理：`/api` 代理到 `http://localhost:8080`，`/wechat/api` 代理到 `http://localhost:8090`

- `wechat-miniprogram/` 微信小程序骨架（学员端与教师端）
  - 学员端：待签到课时展示、扫码签到
  - 教师端：发起签到获取签到码

## 运行指南

### 管理后台前端

```bash
cd admin-frontend
npm install --cache .npm-cache --force
npm run dev
# 访问：http://localhost:5173
```

### 微信端后端（可选）

```bash
cd edu-admin-backend/wechat-api
mvn -q spring-boot:run -DskipTests
# 监听：8090
```

### 管理后台后端（可选）

> 当前未集成数据库。需要数据库时请自行添加数据源与ORM依赖（例如 MyBatis 或 JPA）。

```bash
cd edu-admin-backend/admin-server
mvn -q spring-boot:run -DskipTests
# 监听：8080
```

### 微信小程序

使用微信开发者工具导入 `wechat-miniprogram` 目录，启动并调试。

## 下一步建议

- 接入数据库（MySQL 8），补充实体、Mapper与Service层
- 完善登录与权限（RBAC），为管理后台接口添加鉴权拦截
- 按文档扩展签到流程、批量排课与课时费规则，并对齐接口规范
- 引入单元测试与基础CI流程（Jenkins），保障迭代质量