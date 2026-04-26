package com.example.oauth2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Token Revocation 端点集成测试 (RFC 7009)
 * 验证 POST /oauth2/revoke 端点的行为
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TokenRevocationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * 使用 client_credentials 获取 access_token
     */
    private String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("demo-client", "demo-secret");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials&scope=read", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/oauth2/token", HttpMethod.POST, request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) response.getBody().get("access_token");
    }

    /**
     * 使用客户端认证调用 revocation 端点
     */
    private ResponseEntity<Void> revokeToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("demo-client", "demo-secret");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>("token=" + token, headers);
        return restTemplate.exchange("/oauth2/revoke", HttpMethod.POST, request, Void.class);
    }

    /**
     * 使用客户端认证调用 introspection 端点
     */
    private ResponseEntity<Map> introspectToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("demo-client", "demo-secret");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>("token=" + token, headers);
        return restTemplate.exchange("/oauth2/introspect", HttpMethod.POST, request, Map.class);
    }

    /**
     * 吊销有效 access_token 后，自省应返回 inactive
     */
    @Test
    void revoke_validToken_shouldInvalidateToken() {
        String accessToken = getAccessToken();

        // 吊销前 token 是活跃的
        ResponseEntity<Map> beforeRevoke = introspectToken(accessToken);
        assertThat(beforeRevoke.getBody().get("active")).isEqualTo(true);

        // 吊销 token
        ResponseEntity<Void> revokeResponse = revokeToken(accessToken);
        assertThat(revokeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 吊销后 token 应变为非活跃
        ResponseEntity<Map> afterRevoke = introspectToken(accessToken);
        assertThat(afterRevoke.getBody().get("active")).isEqualTo(false);
    }

    /**
     * RFC 7009 要求：吊销未知/无效 token 仍返回 200（防止信息泄露）
     */
    @Test
    void revoke_invalidToken_shouldReturnOk() {
        ResponseEntity<Void> response = revokeToken("invalid-token-value");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * 未认证的 revocation 请求应被拒绝
     */
    @Test
    void revoke_withoutClientAuth_shouldBeDenied() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>("token=some-token", headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/oauth2/revoke", HttpMethod.POST, request, String.class);
        assertThat(response.getStatusCode().value()).isIn(302, 401);
    }

    /**
     * 重复吊销同一 token 应幂等返回 200
     */
    @Test
    void revoke_alreadyRevokedToken_shouldReturnOk() {
        String accessToken = getAccessToken();

        // 第一次吊销
        ResponseEntity<Void> first = revokeToken(accessToken);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 第二次吊销同一 token，应幂等返回 200
        ResponseEntity<Void> second = revokeToken(accessToken);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
