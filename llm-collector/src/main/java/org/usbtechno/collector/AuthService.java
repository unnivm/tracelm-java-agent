package org.usbtechno.collector;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AuthService {

    public static final String SESSION_COOKIE = "tracelm_session";

    @Inject
    UserAccountRepository userAccountRepository;

    @Inject
    UserSessionRepository userSessionRepository;

    @Inject
    PasswordHasher passwordHasher;

    @Inject
    @ConfigProperty(name = "collector.auth.session-days")
    int sessionDays;

    @Transactional
    public AuthUserResponse signup(SignupRequest request) {
        Optional<UserAccount> existing = userAccountRepository.findByEmail(request.email);
        if (existing.isPresent()) {
            throw new WebApplicationException("An account with this email already exists", Response.Status.CONFLICT);
        }

        UserAccount user = new UserAccount();
        user.name = request.name.trim();
        user.email = request.email.trim();
        user.passwordHash = passwordHasher.hash(request.password);
        userAccountRepository.persist(user);

        return new AuthUserResponse(user);
    }

    public UserAccount authenticate(String email, String password) {
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new WebApplicationException("Invalid email or password", Response.Status.UNAUTHORIZED));

        if (!passwordHasher.verify(password, user.passwordHash)) {
            throw new WebApplicationException("Invalid email or password", Response.Status.UNAUTHORIZED);
        }

        return user;
    }

    @Transactional
    public NewCookie createSessionCookie(UserAccount user) {
        UserSession session = new UserSession();
        session.token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        session.user = user;
        session.expiresAt = Instant.now().plus(Duration.ofDays(sessionDays));
        userSessionRepository.persist(session);

        return new NewCookie.Builder(SESSION_COOKIE)
                .value(session.token)
                .httpOnly(true)
                .path("/")
                .maxAge((int) Duration.ofDays(sessionDays).getSeconds())
                .sameSite(NewCookie.SameSite.LAX)
                .build();
    }

    @Transactional
    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            userSessionRepository.deleteById(token);
        }
    }

    public Optional<UserAccount> findUserBySessionToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return userSessionRepository.findActive(token).map(session -> session.user);
    }

    public NewCookie clearSessionCookie() {
        return new NewCookie.Builder(SESSION_COOKIE)
                .value("")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite(NewCookie.SameSite.LAX)
                .build();
    }
}
