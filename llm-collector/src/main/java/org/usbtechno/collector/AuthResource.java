package org.usbtechno.collector;

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
