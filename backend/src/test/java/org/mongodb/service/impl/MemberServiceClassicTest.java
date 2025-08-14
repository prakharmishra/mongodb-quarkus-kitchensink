package org.mongodb.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongodb.model.Member;
import org.mongodb.repository.MemberRepo;
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
        Member member = new Member("123", "John Doe", "", "1234567890");
        when(memberRepo.findById("123")).thenReturn(Optional.of(member));

        Optional<Member> result = memberService.findById("123");

        assertTrue(result.isPresent());
        assertEquals(member, result.get());
        verify(memberRepo).findById("123");
    }

    @Test
    void testFindByIdReturnsEmpty() {
        when(memberRepo.findById("notfound")).thenReturn(Optional.empty());

        Optional<Member> result = memberService.findById("notfound");

        assertFalse(result.isPresent());
        verify(memberRepo).findById("notfound");
    }

    @Test
    void testFindAllReturnsList() {
        Member member1 = new Member("123", "John Doe", "", "1234567890");
        Member member2 = new Member("234", "John Doe 2", "", "1234567890");
        List<Member> members = Arrays.asList(member1, member2);
        when(memberRepo.findAll()).thenReturn(members);

        List<Member> result = memberService.findAll();

        assertEquals(2, result.size());
        assertEquals(members, result);
        verify(memberRepo).findAll();
    }

    @Test
    void testSaveCallsRepoSave() {
        Member member = new Member("123", "John Doe", "", "1234567890");

        memberService.save(member);

        verify(memberRepo).save(member);
    }

    @Test
    void testDeleteByIdCallsRepoDelete() {
        memberService.deleteById("456");

        verify(memberRepo).deleteById("456");
    }
}