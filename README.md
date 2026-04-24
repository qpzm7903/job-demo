# OAuth2 授权服务器

基于 Spring Authorization Server 构建的 OAuth2 授权服务。

## 当前版本：v0.2.0

### 能做什么
- Spring Boot 3.2.x 应用可启动
- `GET /hello` 公开端点，返回 `Hello OAuth2 Server!`
- 集成 Spring Authorization Server，配置 in-memory 客户端 `demo-client`
- `POST /oauth2/token` Token 端点（client_credentials 授权流）
- JWT 格式 Access Token

### 如何运行

```bash
mvn clean package -DskipTests
java -jar target/oauth2-server-0.2.0.jar
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
# → {"access_token":"eyJ...","scope":"read","token_type":"Bearer","expires_in":299}
```

## 版本历史

| 版本 | 说明 |
|------|------|
| v0.2.0 | 集成 Spring Authorization Server + /oauth2/token |
| v0.1.0 | 项目骨架 + /hello 端点 |
