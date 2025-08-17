package org.mongodb.service.impl;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongodb.model.CursorPage;
import org.mongodb.model.Member;
import org.mongodb.repository.MemberRepo;
import org.mongodb.resource.viewmodel.UpsertMemberViewModel;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MemberServiceClassicTest {

    private MemberRepo memberRepo;
    private MemberServiceClassic memberService;

    @BeforeEach
    void setUp() {
        memberRepo = mock(MemberRepo.class);
        memberService = new MemberServiceClassic(memberRepo);
    }

    @Test
    void testFindByIdReturnsMember() {
        // Given
        Member member = new Member(new ObjectId("689f3a9598292c14bf413125"), "user-id", "John Doe", "john@example.com", "1234567890");
        when(memberRepo.findById("123")).thenReturn(Optional.of(member));

        // When
        Optional<Member> result = memberService.findById("123");

        // Then
        assertTrue(result.isPresent());
        assertEquals(member, result.get());
        verify(memberRepo).findById("123");
    }

    @Test
    void testFindByIdReturnsEmpty() {
        // Given
        when(memberRepo.findById("notfound")).thenReturn(Optional.empty());

        // When
        Optional<Member> result = memberService.findById("notfound");

        // Then
        assertFalse(result.isPresent());
        verify(memberRepo).findById("notfound");
    }

    @Test
    void testFindAllReturnsPage() {
        // Given
        Member member1 = new Member(new ObjectId("689f3a9598292c14bf413125"), "user1", "John Doe", "john@example.com", "1234567890");
        Member member2 = new Member(new ObjectId("234689f3a9598214bf413125"), "user2", "Jane Doe", "jane@example.com", "0987654321");
        List<Member> members = Arrays.asList(member1, member2);
        String nextCursor = "234";
        CursorPage<Member> page = new CursorPage<>(members, nextCursor);
        
        when(memberRepo.listMembersPage(10, "123")).thenReturn(page);

        // When
        CursorPage<Member> result = memberService.findAll(10, "123");

        // Then
        assertEquals(2, result.data().size());
        assertEquals(nextCursor, result.nextCursor());
        verify(memberRepo).listMembersPage(10, "123");
    }

    @Test
    void testSaveNewMember() {
        // Given
        UpsertMemberViewModel viewModel = new UpsertMemberViewModel(
            "test-id",
            "John Doe",
            "john@example.com",
            "1234567890"
        );

        // When
        memberService.register(viewModel, viewModel.userId(), viewModel.email());

        // Then
        verify(memberRepo).register(argThat(member -> 
            member.id() == null &&
            member.userId().equals("test-id") &&
            member.name().equals("John Doe") &&
            member.email().equals("john@example.com") &&
            member.phoneNumber().equals("1234567890")
        ));
    }

    @Test
    void testUpdateExistingMember() {
        // Given
        String existingId = "689f3a9598292c14bf413125";
        UpsertMemberViewModel viewModel = new UpsertMemberViewModel(
            "689f3a9598292c14bf413125",
            "John Doe Updated",
            "john.updated@example.com",
            "9876543210"
        );

        // When
        memberService.update(viewModel, existingId);

        // Then
        verify(memberRepo).update(argThat(member -> 
            member.id().equals(new ObjectId(existingId)) &&
            member.name().equals("John Doe Updated") &&
            member.email().equals("john.updated@example.com") &&
            member.phoneNumber().equals("9876543210")
        ));
    }

    @Test
    void testDeleteByIdCallsRepoDelete() {
        // Given
        String id = "456";

        // When
        memberService.deleteById(id);

        // Then
        verify(memberRepo).deleteById(id);
    }
}