package org.mongodb.filter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.mongodb.model.Member;
import org.mongodb.repository.MemberRepo;
import org.mongodb.service.MemberService;

@Provider
@PreMatching
@ApplicationScoped
public class RegistrationCheckFilter implements ContainerRequestFilter {
    private final jakarta.inject.Provider<JsonWebToken> jwtProvider;
    private final MemberRepo memberRepo;
    private final boolean completeRegistrationEnabled;
    private final MemberService memberService;

    public RegistrationCheckFilter(
        jakarta.inject.Provider<JsonWebToken> jwtProvider,
        MemberRepo memberRepo,
        @ConfigProperty(name = "kitchensink.application.complete-registration.enabled")
        boolean completeRegistrationEnabled,
        MemberService memberService
    ) {
        this.jwtProvider = jwtProvider;
        this.memberRepo = memberRepo;
        this.completeRegistrationEnabled = completeRegistrationEnabled;
        this.memberService = memberService;
    }


    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Skip check for registration endpoints and OPTIONS requests
        String path = requestContext.getUriInfo().getPath();
        if (path.startsWith("/api/registration") || 
            "OPTIONS".equals(requestContext.getMethod())) {
            return;
        }

        JsonWebToken jwt = jwtProvider.get();

        String email = jwt.getClaim("email");
        if (email != null && !memberRepo.findByEmail(email).isPresent()) {
            if (completeRegistrationEnabled) {
                Response response = Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("User registration required", "/auth/register"))
                    .build();
                requestContext.abortWith(response);
            } else {
                String userId = jwt.getSubject();
                String username = jwt.getClaim("preferred_username");
                String firstName = jwt.getClaim("given_name");
                String lastName = jwt.getClaim("family_name");

                Member member = new Member(
                    null,
                    userId,
                    username,
                    firstName,
                    lastName,
                    email,
                    null
                );
                
                memberService.register(member);
            }
        }
    }
}

record ErrorResponse(String message, String registrationUrl) {}
