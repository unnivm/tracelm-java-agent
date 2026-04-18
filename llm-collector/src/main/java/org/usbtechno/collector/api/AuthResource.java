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

package org.usbtechno.collector.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.usbtechno.collector.auth.AuthService;
import org.usbtechno.collector.auth.dto.AuthUserResponse;
import org.usbtechno.collector.auth.dto.LoginRequest;
import org.usbtechno.collector.auth.dto.SignupRequest;
import org.usbtechno.collector.domain.UserAccount;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/signup")
    public Response signup(@Valid SignupRequest request) {
        AuthUserResponse user = authService.signup(request);
        UserAccount account = authService.authenticate(request.email, request.password);
        NewCookie cookie = authService.createSessionCookie(account);
        return Response.status(Response.Status.CREATED)
                .cookie(cookie)
                .entity(user)
                .build();
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        UserAccount user = authService.authenticate(request.email, request.password);
        NewCookie cookie = authService.createSessionCookie(user);
        return Response.ok(new AuthUserResponse(user))
                .cookie(cookie)
                .build();
    }

    @POST
    @Path("/logout")
    public Response logout(@CookieParam(AuthService.SESSION_COOKIE) String sessionToken) {
        authService.logout(sessionToken);
        return Response.noContent()
                .cookie(authService.clearSessionCookie())
                .build();
    }

    @GET
    @Path("/me")
    public AuthUserResponse me(@CookieParam(AuthService.SESSION_COOKIE) String sessionToken) {
        return authService.findUserBySessionToken(sessionToken)
                .map(AuthUserResponse::new)
                .orElseThrow(() -> new WebApplicationException("Not authenticated", Response.Status.UNAUTHORIZED));
    }
}
