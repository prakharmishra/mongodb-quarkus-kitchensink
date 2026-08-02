package org.mongodb.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongodb.model.CursorPage;
import org.mongodb.model.Member;
import org.mongodb.resource.viewmodel.KitchenSinkError;
import org.mongodb.resource.viewmodel.UpsertMemberViewModel;
import org.mongodb.service.MemberService;

import jakarta.inject.Provider;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Component tests for {@link MemberResource} CRUD operations with a mocked service layer.
 */
class MemberResourceComponentTest {

    private MemberService memberService;
    private JsonWebToken jwt;
    private SecurityContext securityContext;
    private MemberResource memberResource;

    private static final ObjectId MEMBER_ID = new ObjectId("507f1f77bcf86cd799439011");
    private static final Member SAMPLE_MEMBER = new Member(
            MEMBER_ID,
            "user-id",
            "johndoe",
            "John",
            "Doe",
            "john@example.com",
            "1234567890"
    );

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        memberService = mock(MemberService.class);
        jwt = mock(JsonWebToken.class);
        securityContext = mock(SecurityContext.class);

        Provider<JsonWebToken> jwtProvider = mock(Provider.class);
        Provider<SecurityContext> securityContextProvider = mock(Provider.class);
        when(jwtProvider.get()).thenReturn(jwt);
        when(securityContextProvider.get()).thenReturn(securityContext);

        memberResource = new MemberResource(memberService, jwtProvider, securityContextProvider);
    }

    // --- Read: get by id ---

    @Test
    void getMemberById_returnsOkWhenFound() {
        when(memberService.findById(MEMBER_ID.toString())).thenReturn(Optional.of(SAMPLE_MEMBER));

        Response response = memberResource.getMemberById(MEMBER_ID.toString());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(SAMPLE_MEMBER, response.getEntity());
        verify(memberService).findById(MEMBER_ID.toString());
    }

    @Test
    void getMemberById_returnsNotFoundWhenMissing() {
        when(memberService.findById("missing")).thenReturn(Optional.empty());

        Response response = memberResource.getMemberById("missing");

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(memberService).findById("missing");
    }

    // --- Read: list ---

    @Test
    void listMembers_asAdmin_returnsPagedMembers() {
        when(securityContext.isUserInRole("ADMIN")).thenReturn(true);
        when(jwt.getClaim("email")).thenReturn("admin@example.com");

        CursorPage<Member> page = new CursorPage<>(List.of(SAMPLE_MEMBER), "next-cursor");
        when(memberService.findAll(10, null)).thenReturn(page);

        Response response = memberResource.listMembers(Optional.empty(), Optional.empty());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(page, response.getEntity());
        verify(memberService).findAll(10, null);
        verify(memberService, never()).findByEmail(anyString());
    }

    @Test
    void listMembers_asAdmin_usesSizeAndCursor() {
        when(securityContext.isUserInRole("ADMIN")).thenReturn(true);
        when(jwt.getClaim("email")).thenReturn("admin@example.com");

        CursorPage<Member> page = new CursorPage<>(List.of(SAMPLE_MEMBER), null);
        when(memberService.findAll(5, "cursor-abc")).thenReturn(page);

        Response response = memberResource.listMembers(Optional.of(5), Optional.of("cursor-abc"));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(page, response.getEntity());
        verify(memberService).findAll(5, "cursor-abc");
    }

    @Test
    void listMembers_asAdmin_rejectsNonPositiveSize() {
        when(securityContext.isUserInRole("ADMIN")).thenReturn(true);
        when(jwt.getClaim("email")).thenReturn("admin@example.com");

        Response response = memberResource.listMembers(Optional.of(0), Optional.empty());

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Size must be greater than 0", response.getEntity());
        verify(memberService, never()).findAll(anyInt(), any());
    }

    @Test
    void listMembers_asUser_returnsOwnMember() {
        when(securityContext.isUserInRole("ADMIN")).thenReturn(false);
        when(jwt.getClaim("email")).thenReturn("john@example.com");
        when(memberService.findByEmail("john@example.com")).thenReturn(Optional.of(SAMPLE_MEMBER));

        Response response = memberResource.listMembers(Optional.of(10), Optional.empty());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        CursorPage<Member> page = (CursorPage<Member>) response.getEntity();
        assertEquals(1, page.data().size());
        assertEquals(SAMPLE_MEMBER, page.data().get(0));
        assertNull(page.nextCursor());
        verify(memberService).findByEmail("john@example.com");
        verify(memberService, never()).findAll(anyInt(), any());
    }

    @Test
    void listMembers_asUser_returnsNotFoundWhenNoMember() {
        when(securityContext.isUserInRole("ADMIN")).thenReturn(false);
        when(jwt.getClaim("email")).thenReturn("unknown@example.com");
        when(memberService.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Response response = memberResource.listMembers(Optional.empty(), Optional.empty());

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(memberService).findByEmail("unknown@example.com");
    }

    // --- Update ---

    @Test
    void updateMember_callsServiceAndReturnsOk() {
        UpsertMemberViewModel viewModel = new UpsertMemberViewModel(
                null,
                "Jane",
                "Smith",
                null,
                "0987654321"
        );

        Response response = memberResource.updateMember(viewModel, MEMBER_ID.toString());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(memberService).update(argThat(member ->
                MEMBER_ID.equals(member.id())
                        && "Jane".equals(member.firstName())
                        && "Smith".equals(member.lastName())
                        && "0987654321".equals(member.phoneNumber())
                        && member.email() == null
                        && member.userId() == null
                        && member.username() == null
        ));
    }

    @Test
    void updateMember_rejectsBlankId() {
        UpsertMemberViewModel viewModel = new UpsertMemberViewModel(
                null,
                "Jane",
                "Smith",
                null,
                "0987654321"
        );

        Response response = memberResource.updateMember(viewModel, "   ");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertInstanceOf(KitchenSinkError.class, response.getEntity());
        verify(memberService, never()).update(any());
    }

    @Test
    void updateMember_rejectsNullId() {
        UpsertMemberViewModel viewModel = new UpsertMemberViewModel(
                null,
                "Jane",
                "Smith",
                null,
                "0987654321"
        );

        Response response = memberResource.updateMember(viewModel, null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(memberService, never()).update(any());
    }

    // --- Delete ---

    @Test
    void deleteMember_callsServiceAndReturnsNoContent() {
        Response response = memberResource.deleteMember(MEMBER_ID.toString());

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(memberService).deleteById(MEMBER_ID.toString());
    }

    @Test
    void deleteMember_rejectsBlankId() {
        Response response = memberResource.deleteMember("  ");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertInstanceOf(KitchenSinkError.class, response.getEntity());
        verify(memberService, never()).deleteById(anyString());
    }

    @Test
    void deleteMember_rejectsNullId() {
        Response response = memberResource.deleteMember(null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(memberService, never()).deleteById(any());
    }
}
