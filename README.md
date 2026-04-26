# job-demo

OAuth2 授权服务器示例项目，基于 Spring Boot 3.2 + Spring Authorization Server 1.x。

## 当前版本: v0.8.0

## 功能概览

| 版本 | 功能 |
|------|------|
| v0.4.0 | `authorization_code` 授权流 + 登录页 |
| v0.5.0 | 单元测试覆盖率 >= 60% |
| v0.6.0 | 客户端管理 API (`/admin/clients`)，JPA 持久化，Bearer 鉴权 |
| v0.7.0 | Token 自省端点（RFC 7662 `POST /oauth2/introspect`） |
| v0.8.0 | Refresh Token 流（authorization_code 签发 + grant_type=refresh_token 换发） |

## 技术栈

- JDK 17 + Maven
- Spring Boot 3.2.x
- Spring Authorization Server 1.x
- H2 内存数据库
- JUnit 5 + Spring Boot Test

## 构建与运行

```bash
mvn clean package -DskipTests
java -jar target/oauth2-server-*.jar
```

## API 端点

| 端点 | 说明 |
|------|------|
| `GET /hello` | 公开端点 |
| `POST /oauth2/token` | Token 端点（client_credentials / authorization_code / refresh_token） |
| `GET /oauth2/jwks` | JWK 集端点 |
| `GET /oauth2/authorize` | 授权码流程端点 |
| `POST /oauth2/introspect` | Token 自省端点（RFC 7662） |
| `POST /admin/clients` | 注册新客户端（需 Bearer token） |
| `GET /admin/clients` | 列出客户端（需 Bearer token） |
| `DELETE /admin/clients/{id}` | 删除客户端（需 Bearer token） |

## 快速验证

```bash
# 获取 access_token
curl -X POST http://localhost:8080/oauth2/token \
  -u demo-client:demo-secret \
  -d "grant_type=client_credentials&scope=read"

# 自省 token
curl -X POST http://localhost:8080/oauth2/introspect \
  -u demo-client:demo-secret \
  -d "token=<your-access-token>"

# 使用 refresh_token 换发新 token
curl -X POST http://localhost:8080/oauth2/token \
  -u demo-client:demo-secret \
  -d "grant_type=refresh_token&refresh_token=<your-refresh-token>"
```
