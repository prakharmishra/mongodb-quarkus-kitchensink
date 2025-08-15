package org.mongodb.resource.exception;

import jakarta.annotation.Priority;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.mongodb.resource.viewmodel.KitchenSinkError;

@Provider
@Priority(1)
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new KitchenSinkError(exception.getMessage()))
                .build();
    }
}
