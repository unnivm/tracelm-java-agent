/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.usbtechno.collector.security;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.usbtechno.collector.auth.AuthService;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Provider
@ApplicationScoped
@Priority(Priorities.AUTHENTICATION)
public class ApiKeyAuthFilter implements ContainerRequestFilter {

    @Inject
    @ConfigProperty(name = "collector.security.api-key")
    Optional<String> configuredApiKey;

    @Inject
    AuthService authService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String apiKeyConfig = configuredApiKey
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElse(null);

        if (apiKeyConfig == null) {
            return;
        }

        String path = requestContext.getUriInfo().getPath();
        if (!path.startsWith("traces") || "OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        var sessionCookie = requestContext.getCookies().get(AuthService.SESSION_COOKIE);
        if (sessionCookie != null && authService.findUserBySessionToken(sessionCookie.getValue()).isPresent()) {
            return;
        }

        String apiKey = requestContext.getHeaderString("X-API-Key");
        if ((apiKey == null || apiKey.isBlank())) {
            String authorization = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (authorization != null && authorization.startsWith("Bearer ")) {
                apiKey = authorization.substring("Bearer ".length());
            }
        }

        if (!apiKeyConfig.equals(apiKey)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of(
                            "error", "Unauthorized",
                            "message", "Missing or invalid API key"
                    ))
                    .build());
        }
    }
}
