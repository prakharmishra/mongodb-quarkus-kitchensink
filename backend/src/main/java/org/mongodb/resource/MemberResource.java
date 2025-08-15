package org.mongodb.resource;

import java.util.Optional;

import org.mongodb.resource.viewmodel.KitchenSinkError;
import org.mongodb.resource.viewmodel.UpsertMemberViewModel;
import org.mongodb.service.MemberService;

import jakarta.enterprise.context.ApplicationScoped;
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

@ApplicationScoped
@Path("/api/members")
@Produces(MediaType.APPLICATION_JSON)
public class MemberResource {
    private final MemberService memberService;

    public MemberResource(MemberService memberService) {
        this.memberService = memberService;
    }
    
    @GET
    @Path("/{id}")
    public Response getMemberById(String id) {
        return memberService.findById(id)
                .map(member -> Response.ok(member).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    public Response listMembers(
        @QueryParam("size") Optional<Integer> size,
        @QueryParam("cursor") Optional<String> cursor
    ) {
        int pageSize = size.orElse(10); // Default size if not provided
        if (pageSize <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Size must be greater than 0")
                    .build();
        }

        return Response.ok(memberService.findAll(pageSize, cursor.orElse(null))).build();
    }

    @POST
    public Response createMember(@Valid @NotNull UpsertMemberViewModel memberViewModel) {
        memberService.save(memberViewModel);
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateMember(@Valid @NotNull UpsertMemberViewModel memberViewModel, @PathParam("id") String id) {
        if (id == null || id.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new KitchenSinkError("id cannot be blank"))
                    .build();
        }

        memberService.update(memberViewModel, id);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
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
