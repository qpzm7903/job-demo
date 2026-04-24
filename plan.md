# OAuth2 授权服务 — 实现路线图

## 版本规划

| 版本 | 目标 | 状态 |
|------|------|------|
| v0.1.0 | Maven 骨架 + Spring Boot 3 可启动 + `GET /hello` | ✅ 已完成 |
| v0.2.0 | 集成 Spring Authorization Server，配置 in-memory 客户端，暴露 `/oauth2/token` | ✅ 已完成 |
| v0.3.0 | 支持 `client_credentials` 授权流；JWT Token；`/oauth2/jwks` | ✅ 已完成 |
| v0.4.0 | 支持 `authorization_code` 授权流 + 最简登录页（Thymeleaf） | ✅ 已完成 |
| v0.5.0 | 为核心 Service / Controller 补单元测试，覆盖率 >= 60% | ✅ 已完成 |
| v0.5.1 | 修复测试健壮性：bad credentials 改为直接验证 401 状态码 | ✅ 已完成 |
| v0.5.2 | 新增 GitHub Actions CI 工作流（Maven 构建 + 测试） | ✅ 已完成 |
| v0.5.3 | README 添加 CI badge + 版本号更新 | ✅ 已完成 |

## 技术栈

- JDK 17 + Maven
- Spring Boot 3.2.x
- Spring Authorization Server（`spring-boot-starter-oauth2-authorization-server`）
- H2 内存数据库
- JUnit 5 + Spring Boot Test

## 构建与运行

```bash
mvn clean package -DskipTests
java -jar target/oauth2-server-*.jar
```

## 验证入口

| 版本 | 端点 | 说明 |
|------|------|------|
| v0.1.0 | `GET /hello` | 公开端点，返回 "Hello OAuth2 Server!" |
| v0.2.0 | `POST /oauth2/token` | Token 端点（client_credentials） |
| v0.3.0 | `GET /oauth2/jwks` | JWK 集端点 |
| v0.4.0 | `GET /oauth2/authorize` | 授权码流程端点 |
