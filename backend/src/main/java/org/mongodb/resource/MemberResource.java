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
import org.slf4j.MDC;
import java.util.UUID;

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
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        Log.info("Getting member by id: " + id);
        
        Optional<Member> result = memberService.findById(id);
        Log.info("Member found: " + result.isPresent());
        
        MDC.clear();
        return result.map(member -> Response.ok(member).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @RolesAllowed({"ADMIN", "USER"})
    public Response listMembers(
        @QueryParam("size") Optional<Integer> size,
        @QueryParam("cursor") Optional<String> cursor
    ) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        JsonWebToken token = jwt.get();
        boolean isAdmin = securityContext.get().isUserInRole("ADMIN");
        String email = token.getClaim("email");
        int pageSize = size.orElse(10);

        Log.info(String.format("Listing members - User: %s, Role: %s, PageSize: %d", 
                email, isAdmin ? "ADMIN" : "USER", pageSize));
        
        if (!isAdmin) {
            Log.info("Non-admin user, returning own member data");
            Optional<Member> member = memberService.findByEmail(email);
            MDC.clear();
            return member.map(m -> new CursorPage<Member>(List.of(m), null))
                    .map(page -> Response.ok(page).build())
                    .orElse(Response.status(Response.Status.NOT_FOUND).build());
        }

        if (pageSize <= 0) {
            MDC.clear();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Size must be greater than 0")
                    .build();
        }

        CursorPage<Member> result = memberService.findAll(pageSize, cursor.orElse(null));
        Log.info("Returning " + result.data().size() + " members");
        
        MDC.clear();
        return Response.ok(result).build();
    }
    
    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response updateMember(@Valid @NotNull UpsertMemberViewModel memberViewModel, @PathParam("id") String id) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        Log.info("Updating member: " + id);
        
        if (id == null || id.isBlank()) {
            MDC.clear();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new KitchenSinkError("id cannot be blank"))
                    .build();
        }

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
        Log.info("Member updated successfully");
        
        MDC.clear();
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response deleteMember(@PathParam("id") String id) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        Log.info("Deleting member: " + id);
        
        if (id == null || id.isBlank()) {
            MDC.clear();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new KitchenSinkError("id cannot be blank"))
                    .build();
        }
        
        memberService.deleteById(id);
        Log.info("Member deleted successfully");
        
        MDC.clear();
        return Response.noContent().build();
    }
}
