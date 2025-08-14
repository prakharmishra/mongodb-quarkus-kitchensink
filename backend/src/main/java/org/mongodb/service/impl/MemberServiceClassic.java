package org.mongodb.service.impl;

import java.util.List;
import java.util.Optional;

import org.mongodb.model.Member;
import org.mongodb.repository.MemberRepo;
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
    public List<Member> findAll() {
        return memberRepo.findAll();
    }

    @Override
    public void save(Member member) {
        memberRepo.save(member);
    }

    @Override
    public void deleteById(String id) {
        memberRepo.deleteById(id);
    }
    
}
