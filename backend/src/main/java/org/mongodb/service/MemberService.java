package org.mongodb.service;

import java.util.List;
import java.util.Optional;

import org.mongodb.model.Member;

public interface MemberService {
    Optional<Member> findById(String id);
    List<Member> findAll();
    void save(Member member);
    void deleteById(String id);
}
