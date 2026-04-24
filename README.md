# OAuth2 授权服务器

[![Maven CI](https://github.com/qpzm7903/job-demo/actions/workflows/maven.yml/badge.svg)](https://github.com/qpzm7903/job-demo/actions/workflows/maven.yml)

基于 Spring Authorization Server 构建的 OAuth2 授权服务。

## 当前版本：v0.5.6

### 能做什么
- Spring Boot 3.2.x 应用可启动
- `GET /hello` 公开端点，返回 `Hello OAuth2 Server!`
- 集成 Spring Authorization Server，配置 in-memory 客户端 `demo-client`
- `POST /oauth2/token` Token 端点（client_credentials + authorization_code + refresh_token）
- JWT 格式 Access Token（RSA-256 签名），含自定义声明（`client_id`, `issued_at`）
- `GET /oauth2/jwks` JWK 集端点，供资源服务器验证 Token 签名
- `GET /oauth2/authorize` 授权码端点 + 最简登录页（Thymeleaf）
- Access Token 有效期 30 分钟，Refresh Token 有效期 1 天
- 单元测试覆盖（JUnit 5 + Spring Boot Test + JaCoCo），覆盖率 >= 60%
- H2 控制台（`/h2-console`）开发环境可访问，已配置安全放行
- 显式 H2 内存数据源配置（`jdbc:h2:mem:oauth2`）

### 如何运行

```bash
mvn clean package -DskipTests
java -jar target/oauth2-server-0.5.6.jar
```

### 运行测试

```bash
mvn test
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

# 授权码流程（需浏览器交互）：访问以下 URL 后登录 user/password
# http://localhost:8080/oauth2/authorize?response_type=code&client_id=demo-client&redirect_uri=http://localhost:8080/callback&scope=read
```

## 版本历史

| 版本 | 说明 |
|------|------|
| v0.5.6 | 开发体验完善：H2 控制台安全配置、显式数据源配置、.gitignore 清理 |
| v0.5.5 | 增强开发配置：H2 控制台、Security 日志、.gitignore 完善 |
| v0.5.4 | 修复 README CI badge URL 为实际仓库地址 |
| v0.5.3 | 添加 CI badge 到 README |
| v0.5.2 | 新增 GitHub Actions CI 工作流 |
| v0.5.1 | 修复测试健壮性：bad credentials 测试改为直接验证 401 状态码 |
| v0.5.0 | 补单元测试 + JaCoCo 覆盖率 >= 60% |
| v0.4.0 | 授权码流程 + Thymeleaf 登录页 + Refresh Token |
| v0.3.0 | 显式 JWT 定制 + Token 设置 + /oauth2/jwks 端点 |
| v0.2.0 | 集成 Spring Authorization Server + /oauth2/token |
| v0.1.0 | 项目骨架 + /hello 端点 |
