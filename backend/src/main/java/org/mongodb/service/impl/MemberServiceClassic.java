package org.mongodb.service.impl;

import java.util.Objects;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.mongodb.model.CursorPage;
import org.mongodb.model.Member;
import org.mongodb.repository.MemberRepo;

import org.mongodb.service.MemberService;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MemberServiceClassic implements MemberService {
    private final MemberRepo memberRepo;

    public MemberServiceClassic(MemberRepo memberRepo) {
        this.memberRepo = memberRepo;
    }

    @Override
    public Optional<Member> findById(String id) {
        Log.info("Service: Finding member by id: " + id);
        Optional<Member> result = memberRepo.findById(id);
        Log.info("Service: Member found: " + result.isPresent());
        return result;
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        Log.info("Service: Finding member by email: " + email);
        Optional<Member> result = memberRepo.findByEmail(email);
        Log.info("Service: Member found by email: " + result.isPresent());
        return result;
    }

    @Override
    public CursorPage<Member> findAll(int size, String cursor) {
        Log.info("Service: Finding all members with size: " + size + ", cursor: " + cursor);
        CursorPage<Member> result = memberRepo.listMembersPage(size, cursor);
        Log.info("Service: Found " + result.data().size() + " members");
        return result;
    }

    @Override
    public void register(Member member) {
        Log.info("Service: Registering member: " + member.email());
        memberRepo.register(member);
        Log.info("Service: Member registered successfully");
    }

    @Override
    public void deleteById(String id) {
        Log.info("Service: Deleting member by id: " + id);
        memberRepo.deleteById(id);
        Log.info("Service: Member deleted successfully");
    }

    @Override
    public void update(Member member) {
        Log.info("Service: Updating member: " + member.id());
        memberRepo.update(member);
        Log.info("Service: Member updated successfully");
    }
    
}
