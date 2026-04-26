package com.example.oauth2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 客户端管理 API 集成测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClientAdminControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * 获取有效的 Bearer token（使用 demo-client 的 client_credentials）
     */
    private HttpHeaders getAuthHeaders() {
        // 先获取 token
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setBasicAuth("demo-client", "demo-secret");
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> tokenRequest = new HttpEntity<>("grant_type=client_credentials&scope=read", tokenHeaders);

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                "/oauth2/token", HttpMethod.POST, tokenRequest, Map.class);
        assertThat(tokenResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void adminEndpoint_withoutToken_shouldReturn401() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/admin/clients", HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void registerClient_happyPath_shouldReturn201() {
        HttpHeaders headers = getAuthHeaders();
        Map<String, Object> body = Map.of(
                "client_id", "test-client-" + System.currentTimeMillis(),
                "client_secret", "test-secret",
                "scopes", List.of("read", "write")
        );
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/admin/clients", HttpMethod.POST, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKey("client_id");
        assertThat(response.getBody().get("client_id").toString()).startsWith("test-client-");
    }

    @Test
    void registerClient_duplicateClientId_shouldReturn409() {
        HttpHeaders headers = getAuthHeaders();
        String clientId = "dup-client-" + System.currentTimeMillis();
        Map<String, Object> body = Map.of(
                "client_id", clientId,
                "client_secret", "test-secret",
                "scopes", List.of("read")
        );
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // 第一次注册应成功
        ResponseEntity<Map> first = restTemplate.exchange(
                "/admin/clients", HttpMethod.POST, request, Map.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 第二次相同 client_id 应返回 409
        ResponseEntity<Map> second = restTemplate.exchange(
                "/admin/clients", HttpMethod.POST, request, Map.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void registerClient_missingClientId_shouldReturn400() {
        HttpHeaders headers = getAuthHeaders();
        Map<String, Object> body = Map.of(
                "client_secret", "test-secret"
        );
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/admin/clients", HttpMethod.POST, request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void listClients_shouldReturnList() {
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                "/admin/clients", HttpMethod.GET, request, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // 至少有启动时初始化的 demo-client
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void deleteClient_existing_shouldReturn204() {
        HttpHeaders headers = getAuthHeaders();

        // 先注册一个客户端
        String clientId = "del-client-" + System.currentTimeMillis();
        Map<String, Object> body = Map.of(
                "client_id", clientId,
                "client_secret", "test-secret",
                "scopes", List.of("read")
        );
        HttpEntity<Map<String, Object>> createReq = new HttpEntity<>(body, headers);
        ResponseEntity<Map> created = restTemplate.exchange(
                "/admin/clients", HttpMethod.POST, createReq, Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Long id = ((Number) created.getBody().get("id")).longValue();

        // 删除该客户端
        HttpEntity<Void> deleteReq = new HttpEntity<>(headers);
        ResponseEntity<Void> deleteResp = restTemplate.exchange(
                "/admin/clients/" + id, HttpMethod.DELETE, deleteReq, Void.class);
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteClient_nonExisting_shouldReturn404() {
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/admin/clients/99999", HttpMethod.DELETE, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
