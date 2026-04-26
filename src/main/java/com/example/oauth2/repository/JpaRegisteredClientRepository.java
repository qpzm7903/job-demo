package com.example.oauth2.repository;

import com.example.oauth2.entity.Client;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 JPA 的 RegisteredClientRepository 实现
 * 将 Client 实体与 Spring Authorization Server 的 RegisteredClient 互相转换
 */
public class JpaRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientRepository clientRepository;

    public JpaRegisteredClientRepository(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        // 幂等：如果 client_id 已存在则更新，否则新建
        Optional<Client> existing = clientRepository.findByClientId(registeredClient.getClientId());
        Client client = existing.orElseGet(Client::new);
        populateEntity(client, registeredClient);
        clientRepository.save(client);
    }

    @Override
    public RegisteredClient findById(String id) {
        // id 是 registeredClientId（UUID 字符串）
        return clientRepository.findAll().stream()
                .filter(c -> id.equals(c.getRegisteredClientId()))
                .findFirst()
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    /**
     * Client 实体 → RegisteredClient
     */
    private RegisteredClient toRegisteredClient(Client client) {
        Set<String> scopes = parseCommaSeparated(client.getScopes());
        Set<AuthorizationGrantType> grantTypes = parseCommaSeparated(client.getGrantTypes()).stream()
                .map(AuthorizationGrantType::new)
                .collect(Collectors.toSet());

        RegisteredClient.Builder builder = RegisteredClient.withId(client.getRegisteredClientId())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);

        grantTypes.forEach(builder::authorizationGrantType);

        scopes.forEach(builder::scope);

        Set<String> redirectUris = parseCommaSeparated(client.getRedirectUris());
        redirectUris.forEach(builder::redirectUri);

        builder.tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .refreshTokenTimeToLive(Duration.ofDays(1))
                .reuseRefreshTokens(false)
                .build());

        builder.clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(false)
                .build());

        return builder.build();
    }

    /**
     * RegisteredClient → Client 实体（填充字段）
     */
    private void populateEntity(Client client, RegisteredClient registeredClient) {
        client.setRegisteredClientId(registeredClient.getId());
        client.setClientId(registeredClient.getClientId());
        client.setClientSecret(registeredClient.getClientSecret());
        client.setScopes(String.join(",", registeredClient.getScopes()));
        client.setGrantTypes(registeredClient.getAuthorizationGrantTypes().stream()
                .map(AuthorizationGrantType::getValue)
                .collect(Collectors.joining(",")));
        client.setRedirectUris(String.join(",", registeredClient.getRedirectUris()));
    }

    private Set<String> parseCommaSeparated(String value) {
        if (!StringUtils.hasText(value)) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }
}
