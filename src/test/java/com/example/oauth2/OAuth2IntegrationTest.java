package com.example.oauth2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OAuth2IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void helloEndpointShouldBePublic() {
        ResponseEntity<String> response = restTemplate.getForEntity("/hello", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Hello OAuth2 Server!");
    }

    @Test
    void loginPageShouldBeAccessible() {
        ResponseEntity<String> response = restTemplate.getForEntity("/login", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("登录");
    }

    @Test
    void clientCredentialsShouldReturnToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("demo-client", "demo-secret");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials&scope=read", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/oauth2/token", HttpMethod.POST, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("access_token");
        assertThat(response.getBody().get("token_type")).isEqualTo("Bearer");
        assertThat(response.getBody().get("scope")).isEqualTo("read");
    }

    @Test
    void clientCredentialsWithBadSecretShouldFail() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("demo-client", "wrong-secret");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials&scope=read", headers);

        // 当 Basic 认证失败时，Java HttpURLConnection 在流模式下无法重试，抛出 ResourceAccessException
        assertThatThrownBy(() ->
                restTemplate.exchange("/oauth2/token", HttpMethod.POST, request, Map.class)
        ).isInstanceOf(org.springframework.web.client.ResourceAccessException.class);
    }

    @Test
    void jwksEndpointShouldReturnKeys() throws IOException {
        URL url = new URL(restTemplate.getRootUri() + "/oauth2/jwks");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        assertThat(conn.getResponseCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void authorizeEndpointShouldRedirectToLogin() throws IOException {
        URL url = new URL(restTemplate.getRootUri()
                + "/oauth2/authorize?response_type=code&client_id=demo-client"
                + "&redirect_uri=http://localhost:8080/callback&scope=read");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.connect();
        assertThat(conn.getResponseCode()).isEqualTo(HttpStatus.FOUND.value());
        String location = conn.getHeaderField("Location");
        assertThat(location).contains("/login");
    }
}
