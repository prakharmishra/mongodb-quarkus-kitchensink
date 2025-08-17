package org.mongodb.filter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.mongodb.repository.MemberRepo;

@Provider
@PreMatching
@ApplicationScoped
public class RegistrationCheckFilter implements ContainerRequestFilter {

    @Inject
    JsonWebToken jwt;

    @Inject
    MemberRepo memberRepo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Skip check for registration endpoints and OPTIONS requests
        String path = requestContext.getUriInfo().getPath();
        if (path.startsWith("/api/registration") || 
            "OPTIONS".equals(requestContext.getMethod())) {
            return;
        }

        String email = jwt.getClaim("email");
        if (email != null && !memberRepo.findByEmail(email).isPresent()) {
            Response response = Response
                .status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse("User registration required", "/auth/register"))
                .build();
            requestContext.abortWith(response);
        }
    }
}

record ErrorResponse(String message, String registrationUrl) {}
