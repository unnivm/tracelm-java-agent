package org.usbtechno.collector;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.Set;

@Provider
@ApplicationScoped
@Priority(Priorities.AUTHORIZATION)
public class SessionAuthFilter implements ContainerRequestFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "",
            "index.html",
            "login.html",
            "signup.html",
            "auth/signup",
            "auth/login",
            "auth/logout",
            "auth/me"
    );

    @Inject
    AuthService authService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        if (isPublicPath(path) || isStaticAsset(path) || "OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        if (path.startsWith("traces")) {
            Cookie cookie = requestContext.getCookies().get(AuthService.SESSION_COOKIE);
            if (cookie != null && authService.findUserBySessionToken(cookie.getValue()).isPresent()) {
                return;
            }
            return;
        }

        if (path.endsWith("dashboard.html")) {
            Cookie cookie = requestContext.getCookies().get(AuthService.SESSION_COOKIE);
            if (cookie != null && authService.findUserBySessionToken(cookie.getValue()).isPresent()) {
                return;
            }
            requestContext.abortWith(Response.status(Response.Status.SEE_OTHER)
                    .header("Location", "/login.html")
                    .build());
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.contains(path);
    }

    private boolean isStaticAsset(String path) {
        return path.startsWith("q/") || path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png")
                || path.endsWith(".jpg") || path.endsWith(".svg") || path.endsWith(".ico");
    }
}
