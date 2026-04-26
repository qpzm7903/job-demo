package com.example.oauth2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Token 自省端点集成测试 (RFC 7662)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TokenIntrospectionTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * 使用 demo-client 的 client_credentials 获取 access_token
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
     * 使用客户端认证调用自省端点
     */
    private ResponseEntity<Map> introspectToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("demo-client", "demo-secret");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>("token=" + token, headers);
        return restTemplate.exchange("/oauth2/introspect", HttpMethod.POST, request, Map.class);
    }

    @Test
    void introspect_validToken_shouldReturnActive() {
        String accessToken = getAccessToken();
        ResponseEntity<Map> response = introspectToken(accessToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("active")).isEqualTo(true);
        assertThat(response.getBody()).containsKey("client_id");
        assertThat(response.getBody().get("client_id")).isEqualTo("demo-client");
        assertThat(response.getBody().get("token_type")).isEqualTo("Bearer");
        assertThat(response.getBody()).containsKey("exp");
    }

    @Test
    void introspect_validToken_shouldContainScopeAndSub() {
        String accessToken = getAccessToken();
        ResponseEntity<Map> response = introspectToken(accessToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("active")).isEqualTo(true);
        // RFC 7662 要求返回 scope 和 sub
        assertThat(response.getBody()).containsKey("scope");
        assertThat(response.getBody().get("scope")).isNotNull();
        assertThat(response.getBody()).containsKey("sub");
    }

    @Test
    void introspect_invalidToken_shouldReturnInactive() {
        ResponseEntity<Map> response = introspectToken("invalid-token-value");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("active")).isEqualTo(false);
    }

    @Test
    void introspect_withoutClientAuth_shouldReturnUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>("token=some-token", headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/oauth2/introspect", HttpMethod.POST, request, String.class);
        // 未认证时，Spring Security 会重定向到登录页（302）或返回 401
        assertThat(response.getStatusCode().value()).isIn(302, 401);
    }
}
