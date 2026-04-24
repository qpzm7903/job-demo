# 对外 API 契约（服务内）

本服务**对外暴露**的接口清单。骨架由 `scripts/extract_api_contracts.py` 从 `@RestController` 自动生成，业务含义人工补。

## 字段规范

每个 API 必须填：
- `path + method`
- `Controller 锚点`：类全限定名 + 方法名
- `业务含义`：**必须人工补**（≤2 行）
- `请求参数`：关键业务字段（审计字段略）
- `响应结构`：关键业务字段
- `鉴权要求`：需要哪些角色/权限
- `幂等性`：是 / 否（写操作必填）
- `限流策略`：QPS 阈值、用户维度/IP 维度
- `错误码`：业务错误码清单
- `版本演进规则`

## 硬约束

- 所有接口必须有 `业务含义` 字段，缺失判不合格
- 写操作必须标注幂等性，缺失判不合格
- 被其他服务调用的接口**必须同时登记到根级 `integration-contracts.md`**

---

## 示例

### `POST /api/v1/auth/login`（示例 ✅）

- **Controller 锚点**：`com.example.auth.web.AuthController#login`
- **业务含义**：用户名密码登录，校验通过后签发 AccessToken + RefreshToken
- **请求参数**：
  - `username`：string，必填
  - `password`：string，必填，前端 SHA256 预哈希
  - `captchaToken`：string，连续失败 3 次后必填
- **响应结构**：
  - `accessToken`、`refreshToken`、`expiresIn`、`userProfile`（精简版）
- **鉴权**：无（公开）
- **幂等性**：否（每次登录都签发新 token）
- **限流**：按 IP 5 QPS，按 username 0.5 QPS
- **错误码**：
  - `AUTH-LOGIN-001` 用户不存在
  - `AUTH-LOGIN-002` 密码错误（不区分 001 和 002 对外返回，日志区分）
  - `AUTH-LOGIN-003` 需要验证码
  - `AUTH-LOGIN-004` 账号被锁定
- **版本演进**：响应字段只增不删

### `GET /api/v1/users/{id}`（示例 ✅）

- **Controller 锚点**：`com.example.user.web.UserController#getUserById`
- **业务含义**：查询用户档案，支持被 service-a 在会话签发时调用
- **请求参数**：`id`（path）、`fields`（query，可选，投影字段）
- **响应结构**：`UserProfileDTO`（见 `com.example.user.dto.UserProfileDTO`）
- **鉴权**：需 `user:read` 权限；service-a 走内部服务间凭证（`X-Service-Token`）
- **幂等性**：是（只读）
- **限流**：默认 100 QPS/service
- **错误码**：`USER-QUERY-001` 用户不存在、`USER-QUERY-002` 无权查看
- **版本演进**：字段只增不删；删字段走 `@Deprecated` + 一版本过渡期
- **跨服务契约**：同步登记到 `repo-root/.agent-context/integration-contracts.md` 的 `auth->user:getProfile`

---

## 自动生成区

<!-- BEGIN AUTO-GENERATED -->

_本区由 `extract_api_contracts.py` 自动生成于刷新时。_
_Controller **2** 个，Endpoint **2** 个。_

### HelloController
- **FQN**：`com.example.oauth2.controller.HelloController`
- **路径**：`/tmp/job-demo-ctx-work/src/main/java/com/example/oauth2/controller/HelloController.java`
- **Base**：`/`
- **Endpoints**：
  - `GET /hello` → `hello`

### LoginController
- **FQN**：`com.example.oauth2.controller.LoginController`
- **路径**：`/tmp/job-demo-ctx-work/src/main/java/com/example/oauth2/controller/LoginController.java`
- **Base**：`/`
- **Endpoints**：
  - `GET /login` → `login`

<!-- END AUTO-GENERATED -->

## 人工增补区

<!-- BEGIN HUMAN-CURATED -->

### 自定义 Controller 的业务含义

#### GET /hello（HelloController#hello）
- **业务含义**：公开健康检查端点，不鉴权，用于确认应用已启动
- **鉴权**：无（SecurityConfig 中 permitAll）
- **幂等性**：是（只读）
- **响应**：纯文本字符串

#### GET /login（LoginController#login）
- **业务含义**：Thymeleaf 表单登录页，仅用于 authorization_code 流程中的用户认证
- **鉴权**：无（Spring Security 自动放行）
- **幂等性**：是
- **模板**：`src/main/resources/templates/login.html`

### SAS 框架内置端点（**自动抽取抓不到，必须人工登记**）

Spring Authorization Server 通过 `OAuth2AuthorizationServerConfigurer` 动态注册的端点：

#### POST /oauth2/token
- **业务含义**：OAuth2 Token 端点，签发 access_token / refresh_token / id_token
- **鉴权**：client_id + client_secret（basic auth 或表单体）
- **支持授权类型**：`client_credentials`、`authorization_code`、`refresh_token`
- **响应格式**：JSON，含 access_token（JWT 格式）、token_type=Bearer、expires_in 等
- **配置位置**：`SecurityConfig#registeredClientRepository`、`SecurityConfig#tokenSettings`
- **错误**：400 invalid_grant / 401 invalid_client

#### GET /oauth2/jwks
- **业务含义**：JWK Set 公钥端点，token 校验方拉取公钥验证 JWT 签名
- **鉴权**：无（公开）
- **幂等性**：是
- **性能优化**：**已启用 HTTP 缓存** `Cache-Control: public, max-age=3600`（见 `SecurityConfig#jwksCacheHeaderFilter`）
- **响应格式**：JWK Set JSON（RFC 7517）

#### GET /oauth2/authorize
- **业务含义**：OAuth2 授权端点，authorization_code 流程的用户交互起点
- **鉴权**：用户必须已登录（表单登录），否则跳转 /login
- **响应**：302 跳转到 `redirect_uri`，URL 中带 code

#### 其它 SAS 元数据端点
- `GET /.well-known/oauth-authorization-server` —— OAuth2 服务器元数据
- `GET /.well-known/openid-configuration` —— OIDC 发现文档（若启用 OIDC）

### 已配置的客户端

| client_id | scopes | 授权类型 | redirect_uri |
|-----------|--------|---------|-------------|
| demo-client | read | client_credentials, authorization_code, refresh_token | http://127.0.0.1:8080/login/oauth2/code/demo-client |

客户端配置位于 `SecurityConfig#registeredClientRepository`。

<!-- END HUMAN-CURATED -->

---

## ❌ 反例

- ❌ 只列 path 和参数 —— 这是 Swagger 的活，不是上下文的活
- ❌ "需要登录"作为鉴权说明 —— 没说需要什么权限
- ❌ 没有错误码清单 —— agent 改接口时不知道已有错误码的坑
