# OAuth2 Server

基于 Spring Boot 3.2 + Spring Authorization Server 的 OAuth2 授权服务器示例项目。

## 当前版本: v0.8.0

## 版本历史

| 版本 | 功能 |
|------|------|
| v0.8.0 | Refresh Token 流（authorization_code 签发 + grant_type=refresh_token 换发） |
| v0.7.0 | Token 自省端点（RFC 7662 `POST /oauth2/introspect`） |
| v0.6.0 | 客户端管理 API（POST/GET/DELETE /admin/clients，JPA 持久化，Bearer 鉴权） |
| v0.5.x | OAuth2 授权服务器骨架（client_credentials / authorization_code / JWK 端点 / 单元测试 / CI） |
| v0.4.0 | authorization_code 授权流 + 登录页 |

## API 端点

| 端点 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| `/hello` | GET | 健康检查 | 无 |
| `/oauth2/token` | POST | Token 端点（含 refresh_token） | Basic Auth |
| `/oauth2/jwks` | GET | JWK 集端点 | 无 |
| `/oauth2/authorize` | GET | 授权码流程 | 表单登录 |
| `/oauth2/introspect` | POST | Token 自省（RFC 7662） | Basic Auth |
| `/admin/clients` | POST | 注册新客户端 | Bearer Token |
| `/admin/clients` | GET | 列出客户端 | Bearer Token |
| `/admin/clients/{id}` | DELETE | 删除客户端 | Bearer Token |

## 快速开始

```bash
mvn clean package -DskipTests
java -jar target/oauth2-server-*.jar
```

### 获取 Token

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -u demo-client:demo-secret \
  -d "grant_type=client_credentials&scope=read"
```

### 自省 Token

```bash
curl -X POST http://localhost:8080/oauth2/introspect \
  -u demo-client:demo-secret \
  -d "token=<your-access-token>"
```

### 使用 Refresh Token 换发

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -u demo-client:demo-secret \
  -d "grant_type=refresh_token&refresh_token=<your-refresh-token>"
```

### 管理客户端

```bash
# 列出客户端
curl -H "Authorization: Bearer <token>" http://localhost:8080/admin/clients

# 注册新客户端
curl -X POST -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"client_id":"my-app","client_secret":"my-secret","scopes":["read","write"]}' \
  http://localhost:8080/admin/clients

# 删除客户端
curl -X DELETE -H "Authorization: Bearer <token>" \
  http://localhost:8080/admin/clients/1
```

## 技术栈

- JDK 17 + Maven
- Spring Boot 3.2.x
- Spring Authorization Server 1.x
- H2 内存数据库
- JPA / Hibernate
- JUnit 5 + Spring Boot Test
