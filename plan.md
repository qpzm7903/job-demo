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
| v0.5.4 | 修复 README CI badge URL 为实际仓库地址 | ✅ 已完成 |
| v0.5.5 | 增强开发配置与忽略文件规则 | ✅ 已完成 |
| v0.5.6 | 完善开发体验：H2 控制台安全配置、显式数据源、.gitignore 清理 | ✅ 已完成 |
| v0.6.0 | 客户端管理 API（POST/GET/DELETE /admin/clients，JPA 持久化，Bearer 鉴权） | ✅ 已完成 |
| v0.7.0 | Token 自省端点（RFC 7662 POST /oauth2/introspect，返回 active/sub/scope/exp） | ✅ 已完成 |
| v0.8.0 | Refresh Token 流（authorization_code 签发 refresh_token + grant_type=refresh_token 换发） | ✅ 已完成 |
| v0.9.0 | Token Revocation 端点（RFC 7009 POST /oauth2/revoke，吊销 access_token / refresh_token） | ✅ 已完成 |

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
| v0.6.0 | `POST /admin/clients` | 注册新 OAuth2 客户端 |
| v0.6.0 | `GET /admin/clients` | 列出已注册客户端 |
| v0.6.0 | `DELETE /admin/clients/{id}` | 删除客户端 |
| v0.7.0 | `POST /oauth2/introspect` | Token 自省端点（RFC 7662） |
| v0.8.0 | `POST /oauth2/token` (grant_type=refresh_token) | 使用 refresh_token 换发新 access_token |
| v0.9.0 | `POST /oauth2/revoke` | Token 吊销端点（RFC 7009） |
