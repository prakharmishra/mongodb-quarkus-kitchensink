package org.mongodb.service;

import java.util.Optional;

import org.mongodb.model.CursorPage;
import org.mongodb.model.Member;
public interface MemberService {
    Optional<Member> findById(String id);
    Optional<Member> findByEmail(String email);
    CursorPage<Member> findAll(int size, String cursor);
    void register(Member member);
    void update(Member member);
    void deleteById(String id);
}
