package org.usbtechno.collector;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException webException) {
            String message = webException.getMessage() == null ? webException.getResponse().getStatusInfo().getReasonPhrase() : webException.getMessage();
            return Response.status(webException.getResponse().getStatus())
                    .entity(new ApiError("Request failed", message, uriInfo.getPath()))
                    .build();
        }

        LOG.error("Unhandled application error", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiError("Internal server error", "An unexpected error occurred", uriInfo.getPath()))
                .build();
    }
}
