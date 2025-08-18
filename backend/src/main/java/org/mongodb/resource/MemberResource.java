package org.mongodb.resource;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.mongodb.model.CursorPage;
import org.mongodb.model.Member;
import org.mongodb.resource.viewmodel.KitchenSinkError;
import org.mongodb.resource.viewmodel.UpsertMemberViewModel;
import org.mongodb.service.MemberService;

import io.quarkus.logging.Log;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Provider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@ApplicationScoped
@Path("/api/members")
@Produces(MediaType.APPLICATION_JSON)
public class MemberResource {
    private final MemberService memberService;
    private final Provider<JsonWebToken> jwt;
    private final Provider<SecurityContext> securityContext;

    public MemberResource(MemberService memberService, Provider<JsonWebToken> jwt, Provider<SecurityContext> securityContext) {
        this.memberService = memberService;
        this.jwt = jwt;
        this.securityContext = securityContext;
    }
    
    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response getMemberById(String id) {
        return memberService.findById(id)
                .map(member -> Response.ok(member).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @RolesAllowed({"ADMIN", "USER"})
    public Response listMembers(
        @QueryParam("size") Optional<Integer> size,
        @QueryParam("cursor") Optional<String> cursor
    ) {
        JsonWebToken token = jwt.get();
        
        // Check for ADMIN role using SecurityContext
        boolean isAdmin = securityContext.get().isUserInRole("ADMIN");
        String email = token.getClaim("email");

        Log.info(String.format("User %s is %s", email, isAdmin ? "ADMIN" : "USER"));
        
        // If user is not ADMIN, return their own member data
        if (!isAdmin) {
            return memberService.findByEmail(email)
                    .map(member -> new CursorPage<Member>(List.of(member), null))
                    .map(page -> Response.ok(page).build())
                    .orElse(Response.status(Response.Status.NOT_FOUND).build());
        }

        int pageSize = size.orElse(10);
        if (pageSize <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Size must be greater than 0")
                    .build();
        }

        return Response.ok(memberService.findAll(pageSize, cursor.orElse(null))).build();
    }
    
    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response updateMember(@Valid @NotNull UpsertMemberViewModel memberViewModel, @PathParam("id") String id) {
        if (id == null || id.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new KitchenSinkError("id cannot be blank"))
                    .build();
        }

        // Only allow updates to first name, last name, and phone number
        Member member = new Member(
            new ObjectId(id),
            null,
            null,
            memberViewModel.firstName(),
            memberViewModel.lastName(),
            null,
            memberViewModel.phoneNumber()
        );

        memberService.update(member);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response deleteMember(@PathParam("id") String id) {
        if (id == null || id.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new KitchenSinkError("id cannot be blank"))
                    .build();
        }
        memberService.deleteById(id);
        return Response.noContent().build();
    }
}
