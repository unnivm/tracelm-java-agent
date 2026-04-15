package org.usbtechno.collector.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.usbtechno.collector.domain.UserSession;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class UserSessionRepository implements PanacheRepositoryBase<UserSession, String> {

    public Optional<UserSession> findActive(String token) {
        return find("token = ?1 and expiresAt > ?2", token, Instant.now()).firstResultOptional();
    }
}
