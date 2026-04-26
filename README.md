# job-demo

OAuth2 Server 示例项目 — 用于 AI Agent Pipeline 冒烟测试，验证 Skills 注入与 Settings 权限预配置。

## 时间戳

2026-04-26 04:07:31 UTC

## 项目结构

- **语言/框架**: Java + Spring Boot + Spring Security OAuth2
- **构建工具**: Maven (`pom.xml`)
- **CI/CD**: GitHub Actions (`.github/workflows/maven.yml`)

## 仓库文件列表

```
.agent-context/api-contracts.md
.agent-context/consumes.md
.agent-context/critical-flows.md
.agent-context/domain-glossary.md
.agent-context/entity-graph.md
.agent-context/module-map.md
.agent-context/pitfalls.md
.github/workflows/maven.yml
.gitignore
README.md
docs/refactor-review-20260424.md
plan.md
pom.xml
prompt.md
smoke-test-result.md
src/main/java/com/example/oauth2/Oauth2ServerApplication.java
src/main/java/com/example/oauth2/config/SecurityConfig.java
src/main/java/com/example/oauth2/controller/HelloController.java
src/main/java/com/example/oauth2/controller/LoginController.java
src/main/resources/application.yml
src/main/resources/templates/login.html
src/test/java/com/example/oauth2/OAuth2IntegrationTest.java
src/test/java/com/example/oauth2/Oauth2ServerApplicationTests.java
```
