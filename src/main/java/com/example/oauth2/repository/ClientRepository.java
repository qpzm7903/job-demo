package com.example.oauth2.repository;

import com.example.oauth2.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * OAuth2 客户端 JPA 仓库
 */
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByClientId(String clientId);

    void deleteByClientId(String clientId);
}
