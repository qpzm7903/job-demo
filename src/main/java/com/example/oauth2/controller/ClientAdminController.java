package com.example.oauth2.controller;

import com.example.oauth2.entity.Client;
import com.example.oauth2.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户端管理 API
 * 需使用 client_credentials token 访问
 */
@RestController
@RequestMapping("/admin/clients")
public class ClientAdminController {

    private static final Logger log = LoggerFactory.getLogger(ClientAdminController.class);

    private final ClientRepository clientRepository;
    private final RegisteredClientRepository registeredClientRepository;

    public ClientAdminController(ClientRepository clientRepository,
                                 RegisteredClientRepository registeredClientRepository) {
        this.clientRepository = clientRepository;
        this.registeredClientRepository = registeredClientRepository;
    }

    /**
     * 注册新 OAuth2 客户端
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> registerClient(@RequestBody Map<String, Object> body) {
        String clientId = (String) body.get("client_id");
        String clientSecret = (String) body.get("client_secret");
        @SuppressWarnings("unchecked")
        List<String> scopes = (List<String>) body.get("scopes");

        if (clientId == null || clientId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "client_id 不能为空"));
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "client_secret 不能为空"));
        }

        // 检查 client_id 是否已存在
        if (clientRepository.findByClientId(clientId).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "client_id 已存在: " + clientId));
        }

        // 构建 RegisteredClient 并保存（同时写入 JPA 和 Spring Authorization Server 的仓库）
        RegisteredClient registeredClient = RegisteredClient.withId(java.util.UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret("{noop}" + clientSecret)
                .clientAuthenticationMethod(
                        org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:8080/callback")
                .scopes(s -> {
                    if (scopes != null) {
                        scopes.forEach(s::add);
                    } else {
                        s.add("read");
                    }
                })
                .tokenSettings(org.springframework.security.oauth2.server.authorization.settings.TokenSettings.builder()
                        .accessTokenTimeToLive(java.time.Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(java.time.Duration.ofDays(1))
                        .reuseRefreshTokens(false)
                        .build())
                .clientSettings(org.springframework.security.oauth2.server.authorization.settings.ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .build())
                .build();

        registeredClientRepository.save(registeredClient);

        log.info("注册新客户端: clientId={}", clientId);

        // 查回 JPA 实体返回
        Client saved = clientRepository.findByClientId(clientId).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    /**
     * 列出所有已注册客户端
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listClients() {
        List<Map<String, Object>> clients = clientRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clients);
    }

    /**
     * 删除客户端
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        if (!clientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        clientRepository.deleteById(id);
        log.info("删除客户端: id={}", id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toResponse(Client client) {
        return Map.of(
                "id", client.getId(),
                "client_id", client.getClientId(),
                "scopes", client.getScopes() != null ? client.getScopes() : "",
                "grant_types", client.getGrantTypes(),
                "created_at", client.getCreatedAt().toString()
        );
    }
}
