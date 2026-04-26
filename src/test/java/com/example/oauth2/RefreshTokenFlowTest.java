package com.example.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Refresh Token 流集成测试
 * 验证 authorization_code 流签发 refresh_token 以及 grant_type=refresh_token 换发逻辑
 */
@SpringBootTest
@AutoConfigureMockMvc
class RefreshTokenFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Autowired
    private OAuth2AuthorizationService authorizationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * client_credentials 流不应签发 refresh_token（OAuth2 规范）
     */
    @Test
    void clientCredentials_shouldNotIssueRefreshToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth2/token")
                .param("grant_type", "client_credentials")
                .param("scope", "read")
                .with(httpBasic("demo-client", "demo-secret")))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> response = parseResponse(result);

        assertThat(response).containsKey("access_token");
        assertThat(response).doesNotContainKey("refresh_token");
    }

    /**
     * 使用 refresh_token 换发新的 access_token（happy path）
     */
    @Test
    void refreshTokenGrant_shouldReturnNewAccessToken() throws Exception {
        String refreshTokenValue = createRefreshTokenAuthorization();

        MvcResult result = mockMvc.perform(post("/oauth2/token")
                .param("grant_type", "refresh_token")
                .param("refresh_token", refreshTokenValue)
                .with(httpBasic("demo-client", "demo-secret")))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> response = parseResponse(result);

        assertThat(response).containsKey("access_token");
        assertThat(response.get("token_type")).isEqualTo("Bearer");
        // reuseRefreshTokens=false，每次使用后会签发新的 refresh_token
        assertThat(response).containsKey("refresh_token");
        // 新的 refresh_token 应与旧的不同
        assertThat(response.get("refresh_token")).isNotEqualTo(refreshTokenValue);
    }

    /**
     * 无效 refresh_token 应返回错误
     */
    @Test
    void refreshTokenGrant_withInvalidToken_shouldReturnError() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                .param("grant_type", "refresh_token")
                .param("refresh_token", "invalid-refresh-token")
                .with(httpBasic("demo-client", "demo-secret")))
                .andExpect(status().isBadRequest());
    }

    /**
     * 过期 refresh_token 应返回错误
     */
    @Test
    void refreshTokenGrant_withExpiredToken_shouldReturnError() throws Exception {
        String expiredRefreshToken = createExpiredRefreshTokenAuthorization();

        mockMvc.perform(post("/oauth2/token")
                .param("grant_type", "refresh_token")
                .param("refresh_token", expiredRefreshToken)
                .with(httpBasic("demo-client", "demo-secret")))
                .andExpect(status().isBadRequest());
    }

    /**
     * 未认证的 refresh_token 请求应被拒绝（重定向到登录或返回 401）
     */
    @Test
    void refreshTokenGrant_withoutClientAuth_shouldBeDenied() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                .param("grant_type", "refresh_token")
                .param("refresh_token", "some-token"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status).isIn(302, 401);
                });
    }

    // ---- 辅助方法 ----

    /**
     * 构造已认证用户的 Authentication 对象，存入授权的 attributes 中
     * OAuth2RefreshTokenAuthenticationProvider 在换发时需要从中读取 principal
     */
    private Authentication createUserAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                "user", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    /**
     * 创建一个包含有效 refresh_token 的 OAuth2Authorization
     */
    private String createRefreshTokenAuthorization() {
        RegisteredClient registeredClient = registeredClientRepository.findByClientId("demo-client");

        Instant issuedAt = Instant.now();

        OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                "refresh-" + java.util.UUID.randomUUID().toString(),
                issuedAt,
                issuedAt.plus(1, ChronoUnit.DAYS));

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-" + java.util.UUID.randomUUID().toString(),
                issuedAt,
                issuedAt.plus(30, ChronoUnit.MINUTES),
                Set.of("read"));

        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName("user")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authorizedScopes(Set.of("read"))
                .attribute(Principal.class.getName(), createUserAuthentication())
                .build();

        authorizationService.save(authorization);
        return refreshToken.getTokenValue();
    }

    /**
     * 创建一个包含过期 refresh_token 的 OAuth2Authorization
     */
    private String createExpiredRefreshTokenAuthorization() {
        RegisteredClient registeredClient = registeredClientRepository.findByClientId("demo-client");

        Instant issuedAt = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant expiresAt = Instant.now().minus(1, ChronoUnit.DAYS);

        OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                "expired-refresh-" + java.util.UUID.randomUUID().toString(),
                issuedAt,
                expiresAt);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "expired-access-" + java.util.UUID.randomUUID().toString(),
                issuedAt,
                Instant.now().minus(1, ChronoUnit.HOURS),
                Set.of("read"));

        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName("user")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authorizedScopes(Set.of("read"))
                .attribute(Principal.class.getName(), createUserAuthentication())
                .build();

        authorizationService.save(authorization);
        return refreshToken.getTokenValue();
    }

    private Map<String, Object> parseResponse(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    }
}
