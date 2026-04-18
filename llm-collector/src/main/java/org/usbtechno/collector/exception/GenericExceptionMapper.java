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

package org.usbtechno.collector.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.usbtechno.collector.dto.ApiError;

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
