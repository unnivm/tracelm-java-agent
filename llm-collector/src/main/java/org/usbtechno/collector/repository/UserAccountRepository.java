package org.usbtechno.collector.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.usbtechno.collector.domain.UserAccount;

import java.util.Optional;

@ApplicationScoped
public class UserAccountRepository implements PanacheRepositoryBase<UserAccount, Long> {

    public Optional<UserAccount> findByEmail(String email) {
        return find("email", email == null ? null : email.trim().toLowerCase()).firstResultOptional();
    }
}
