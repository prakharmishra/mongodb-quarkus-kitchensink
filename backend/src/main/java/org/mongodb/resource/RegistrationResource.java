package org.mongodb.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.mongodb.model.RegistrationData;
import org.mongodb.repository.MemberRepo;
import org.mongodb.resource.viewmodel.RegistrationRequest;
import org.mongodb.resource.viewmodel.UpsertMemberViewModel;
import org.mongodb.service.MemberService;
import org.mongodb.model.Member;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/api/registration")
@Produces(MediaType.APPLICATION_JSON)
public class RegistrationResource {

    private final Provider<JsonWebToken> jwtProvider;
    private final MemberService memberService;

    public RegistrationResource(Provider<JsonWebToken> jwtProvider, MemberService memberService) {
        this.jwtProvider = jwtProvider;
        this.memberService = memberService;
    }

    @GET
    @RolesAllowed("USER")
    public Response getRegistrationData() {
        JsonWebToken jwt = jwtProvider.get();
        if (jwt == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String userId = jwt.getSubject();
        String email = jwt.getClaim("email");
        String firstName = jwt.getClaim("given_name");
        String lastName = jwt.getClaim("family_name");
        String username = jwt.getClaim("preferred_username");
        
        boolean isComplete = memberService.findByEmail(email).isPresent();
        
        RegistrationData data = new RegistrationData(
            userId,
            username != null ? username : email,
            email,
            firstName != null ? firstName : "",
            lastName != null ? lastName : "",
            java.time.Instant.now().toString(),
            isComplete
        );
        
        return Response.ok(data).build();
    }

    @PUT
    @Path("/complete")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response completeRegistration(@Valid @NotNull UpsertMemberViewModel request) {
        JsonWebToken jwt = jwtProvider.get();
        if (jwt == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String userId = jwt.getSubject();
        String email = jwt.getClaim("email");
        String username = jwt.getClaim("preferred_username");

        Member member = new Member(
            null,
            userId,
            username,
            request.firstName(),
            request.lastName(),
            email,
            request.phoneNumber()
        );
        
        memberService.register(member);
        
        return Response.status(Response.Status.CREATED).build();
    }
}
