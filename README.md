# OAuth2 授权服务器

基于 Spring Authorization Server 构建的 OAuth2 授权服务。

## 当前版本：v0.3.0

### 能做什么
- Spring Boot 3.2.x 应用可启动
- `GET /hello` 公开端点，返回 `Hello OAuth2 Server!`
- 集成 Spring Authorization Server，配置 in-memory 客户端 `demo-client`
- `POST /oauth2/token` Token 端点（client_credentials 授权流）
- JWT 格式 Access Token（RSA-256 签名），含自定义声明（`client_id`, `issued_at`）
- `GET /oauth2/jwks` JWK 集端点，供资源服务器验证 Token 签名
- Access Token 有效期 30 分钟

### 如何运行

```bash
mvn clean package -DskipTests
java -jar target/oauth2-server-0.3.0.jar
```

### 验证

```bash
# 公开端点
curl http://localhost:8080/hello
# → Hello OAuth2 Server!

# 获取 Token（client_credentials）
curl -u "demo-client:demo-secret" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=read" \
  http://localhost:8080/oauth2/token
# → {"access_token":"eyJ...","scope":"read","token_type":"Bearer","expires_in":1799}

# 查看 JWK 公钥
curl http://localhost:8080/oauth2/jwks
# → {"keys":[{"kty":"RSA","kid":"...","n":"...","e":"AQAB"}]}
```

## 版本历史

| 版本 | 说明 |
|------|------|
| v0.3.0 | 显式 JWT 定制 + Token 设置 + /oauth2/jwks 端点 |
| v0.2.0 | 集成 Spring Authorization Server + /oauth2/token |
| v0.1.0 | 项目骨架 + /hello 端点 |
