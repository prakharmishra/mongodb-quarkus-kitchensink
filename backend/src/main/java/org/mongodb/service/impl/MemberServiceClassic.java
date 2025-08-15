package org.mongodb.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.mongodb.model.CursorPage;
import org.mongodb.model.Member;
import org.mongodb.repository.MemberRepo;
import org.mongodb.resource.viewmodel.UpsertMemberViewModel;
import org.mongodb.service.MemberService;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MemberServiceClassic implements MemberService {
    private final MemberRepo memberRepo;

    public MemberServiceClassic(MemberRepo memberRepo) {
        this.memberRepo = memberRepo;
    }

    @Override
    public Optional<Member> findById(String id) {
        return memberRepo.findById(id);
    }

    @Override
    public CursorPage<Member> findAll(int size, String cursor) {
        return memberRepo.listMembersPage(size, cursor);
    }

    @Override
    public void save(UpsertMemberViewModel memberViewModel) {
        Member member = new Member(
            null, // If id is present, use it; otherwise, let MongoDB generate a new one
            memberViewModel.name(),
            memberViewModel.email(),
            memberViewModel.phoneNumber(),
            List.of("user")
        );

        memberRepo.save(member);
    }

    @Override
    public void deleteById(String id) {
        memberRepo.deleteById(id);
    }

    @Override
    public void update(UpsertMemberViewModel member, String id) {
        Objects.requireNonNull(id, "id cannot be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id cannot be blank");
        }

        Member updatedMember = new Member(
            new ObjectId(id),
            member.name(),
            member.email(),
            member.phoneNumber(),
            null
        );

        memberRepo.update(updatedMember);
    }
    
}
