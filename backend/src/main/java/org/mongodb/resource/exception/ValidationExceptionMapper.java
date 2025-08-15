package org.mongodb.resource.exception;

import jakarta.annotation.Priority;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.mongodb.resource.viewmodel.KitchenSinkError;

@Provider
@Priority(1)
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String[] errors = exception.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .toArray(String[]::new);

        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new KitchenSinkError(errors))
                .build();
    }
}
