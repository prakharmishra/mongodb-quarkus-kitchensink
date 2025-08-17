package org.mongodb.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.mongodb.model.CursorPage;
import org.mongodb.model.Member;
import org.mongodb.repository.MemberRepo;
import org.mongodb.repository.MongoCollectionOps;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RunOnVirtualThread
public class MongoMemberRepo implements MemberRepo {
    private final MongoClient mongoClient;
    private final MongoCollectionOps collectionOps;

    @ConfigProperty(name = "mongodb.database")
    private String databaseName;

    @ConfigProperty(name = "mongodb.collection.members", defaultValue = "members")
    private String collectionName;

    public MongoMemberRepo(MongoClient mongoClient, MongoCollectionOps collectionOps) {
        this.mongoClient = mongoClient;
        this.collectionOps = collectionOps;
    }
    
    @Override
    public Optional<Member> findById(String id) {
        Log.info("Repo: Finding member by id: " + id);
        Objects.requireNonNull(id, "Member ID cannot be null");
        if (id.isBlank()) {
            Log.info("Repo: Empty id provided");
            return Optional.empty();
        }

        Bson filter = Filters.eq("_id", new ObjectId(id));
        Member result = collectionOps.find(getCollection(), Optional.of(filter)).first();
        Log.info("Repo: Member found: " + (result != null));
        
        return Optional.ofNullable(result);
    }

    @Override
    public CursorPage<Member> listMembersPage(int size, String cursor) {
        Log.info("Repo: Listing members page with size: " + size + ", cursor: " + cursor);
        if (size <= 0) {
            Log.info("Repo: Invalid size, returning empty page");
            return CursorPage.empty();
        }

        final Optional<Bson> filter = (cursor != null && !cursor.isBlank())
                ? Optional.of(Filters.gt("_id", new ObjectId(cursor)))
                : Optional.empty();
        
        List<Member> members = collectionOps.find(getCollection(), filter)
                .sort(Sorts.ascending("_id"))
                .limit(size)
                .into(new ArrayList<Member>());

        String nextCursor = members.isEmpty() || members.size() < size 
                ? null
                : members.get(members.size() - 1).id().toString();

        Log.info("Repo: Found " + members.size() + " members, nextCursor: " + nextCursor);
        return new CursorPage<>(members, nextCursor);
    }

    @Override
    public void register(Member member) {
        Log.info("Repo: Registering member: " + member.email());
        Objects.requireNonNull(member, "Member cannot be null");
        collectionOps.insertOne(getCollection(), member);
        Log.info("Repo: Member registered successfully");
    }

    @Override
    public void deleteById(String id) {
        Log.info("Repo: Deleting member by id: " + id);
        Objects.requireNonNull(id, "Member ID cannot be null");
        if (id.isBlank()) {
            Log.info("Repo: Empty id provided for deletion");
            return;
        }

        Bson filter = Filters.eq("_id", new ObjectId(id));
        collectionOps.deleteOne(getCollection(), filter);
        Log.info("Repo: Member deleted successfully");
    }
    
    @Override
    public void update(Member member) {
        Log.info("Repo: Updating member: " + member.id());
        Objects.requireNonNull(member.id());

        Bson filter = Filters.eq("_id", member.id());
        collectionOps.updateOne(getCollection(), filter, member);
        Log.info("Repo: Member updated successfully");
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        Log.info("Repo: Finding member by email: " + email);
        Objects.requireNonNull(email, "Email cannot be null");
        if (email.isBlank()) {
            Log.info("Repo: Empty email provided");
            return Optional.empty();
        }

        Bson filter = Filters.eq("email", email);
        Member result = collectionOps.find(getCollection(), Optional.of(filter)).first();
        Log.info("Repo: Member found by email: " + (result != null));
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Member> findByUserId(String userId) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        if (userId.isBlank()) {
            return Optional.empty();
        }

        Bson filter = Filters.eq("userId", userId);
        return Optional.ofNullable(collectionOps.find(getCollection(), Optional.of(filter)).first());
    }

    private MongoCollection<Member> getCollection() {
        return mongoClient.getDatabase(databaseName).getCollection(collectionName, Member.class);
    }
}
