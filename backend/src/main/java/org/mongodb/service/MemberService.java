package org.mongodb.service;

import java.util.Optional;

import org.mongodb.model.CursorPage;
import org.mongodb.model.Member;
import org.mongodb.resource.viewmodel.UpsertMemberViewModel;

public interface MemberService {
    Optional<Member> findById(String id);
    CursorPage<Member> findAll(int size, String cursor);
    void save(UpsertMemberViewModel member);
    void update(UpsertMemberViewModel member, String id);
    void deleteById(String id);
}
