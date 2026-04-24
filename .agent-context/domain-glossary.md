# 业务术语表（服务内）

**服务所在业务域**的专有名词与代码实体的映射。解决 agent"不知道这个词指什么代码"的问题。

## 字段规范

每个术语必须填：
- `术语`：中文或英文，项目里实际用的那个
- `定义`：≤2 行，业务视角
- `代码锚点`：对应的类、包、表、字段（必填）
- `别名`：常见的错误叫法（避免 agent 用错词）
- `相关术语`：关联的其他术语

## 硬约束

- 禁止写常识性词汇（"用户""订单"这种需要写只写它在**本项目里的特殊含义**）
- 每条必须有代码锚点，否则判不合格
- 上限 150 条，超过必须按子域拆分

---

## job-demo 实际术语

### demo-client

- **定义**：本 OAuth2 服务器内置的唯一测试客户端
- **代码锚点**：`SecurityConfig#registeredClientRepository`
- **别名**：test-client（❌ 不用此称呼）
- **scopes**：read
- **授权类型**：client_credentials、authorization_code、refresh_token

### JWK / JWKS

- **定义**：JWT 签名密钥的 JSON 表示；JWKS 是密钥集合
- **代码锚点**：`SecurityConfig#jwkSource`、端点 `GET /oauth2/jwks`
- **相关术语**：JWT、RS256
- **缓存策略**：响应带 `Cache-Control: public, max-age=3600`（`SecurityConfig#jwksCacheHeaderFilter`）

### client_credentials 授权流

- **定义**：机器到机器场景，客户端凭自身身份换 token，无需用户参与
- **代码锚点**：无专用类；由 SAS 默认 TokenEndpointFilter 处理
- **典型调用**：`curl -u demo-client:demo-secret -d grant_type=client_credentials /oauth2/token`

### authorization_code 授权流

- **定义**：用户在 /login 认证 → /oauth2/authorize 同意 → 302 回调带 code → /oauth2/token 换 token
- **代码锚点**：`SecurityConfig` + `LoginController` + `login.html` 模板

### Access Token / Refresh Token

- **定义**：
  - Access Token：短期（30min）签发给资源访问方的凭证
  - Refresh Token：长期，用于换新的 Access Token
- **代码锚点**：`SecurityConfig#tokenSettings`

---

## 示例

### 会话（Session）

- **定义**：用户登录后签发的一次授权凭证，包含身份与权限快照
- **代码锚点**：`auth-core/src/main/java/.../SessionData.java`、Redis key `session:*`
- **别名**：token（❌ 不准确，token 是 session 的序列化形式）
- **相关术语**：RefreshToken、AccessToken、PermissionSnapshot

### 降权（Downgrade）

- **定义**：用户在管理员操作下丧失某些权限，但账号仍有效
- **代码锚点**：`UserService.downgrade` 方法、`user.profile.changed` 事件的 `changedFields=["role"]` 场景
- **别名**：disable（❌ 错误，disable 是账号级，降权只影响权限）
- **相关术语**：Session 失效、PermissionSnapshot

### 组织单元（OrgUnit）

- **定义**：用户归属的部门/团队/分公司的统一抽象
- **代码锚点**：`UserProfile.orgUnitId` 字段、`OrgUnit` 实体
- **别名**：dept、team（均为旧代码遗留，**新代码必须统一使用 OrgUnit**）

---

## ❌ 反例

- ❌ "用户：使用系统的人" —— 常识，无信息量
- ❌ 没有代码锚点 —— agent 无法定位
- ❌ 术语和别名混乱，新老代码共用 —— 不如不写
