# OAuth2 授权服务器

基于 Spring Authorization Server 构建的 OAuth2 授权服务。

## 当前版本：v0.1.0

### 能做什么
- Spring Boot 3.2.x 应用可启动
- `GET /hello` 公开端点，返回 `Hello OAuth2 Server!`

### 如何运行

```bash
mvn clean package -DskipTests
java -jar target/oauth2-server-0.1.0.jar
```

### 验证

```bash
curl http://localhost:8080/hello
# → Hello OAuth2 Server!
```

## 版本历史

| 版本 | 说明 |
|------|------|
| v0.1.0 | 项目骨架 + /hello 端点 |
