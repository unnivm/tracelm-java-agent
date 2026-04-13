package org.usbtechno.collector;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class UserSessionRepository implements PanacheRepositoryBase<UserSession, String> {

    public Optional<UserSession> findActive(String token) {
        return find("token = ?1 and expiresAt > ?2", token, Instant.now()).firstResultOptional();
    }
}
