package org.mongodb.repository;

import java.util.Optional;

import org.mongodb.model.CursorPage;
import org.mongodb.model.Member;

public interface MemberRepo {
    Optional<Member> findById(String id);
    CursorPage<Member> listMembersPage(int size, String cursor);
    void save(Member member);
    void update(Member member);
    void deleteById(String id);
}
