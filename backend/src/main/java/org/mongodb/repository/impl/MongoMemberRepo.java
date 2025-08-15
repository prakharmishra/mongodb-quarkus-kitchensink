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
        Objects.requireNonNull(id, "Member ID cannot be null");
        if (id.isBlank()) {
            return Optional.empty();
        }

        Bson filter = Filters.eq("_id", new ObjectId(id));

        return Optional.of(collectionOps.find(getCollection(), Optional.of(filter)).first());
    }

    @Override
    public CursorPage<Member> listMembersPage(int size, String cursor) {
        if (size <= 0) {
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

        return new CursorPage<>(members, nextCursor);
    }

    @Override
    public void save(Member member) {
        Objects.requireNonNull(member, "Member cannot be null");
        collectionOps.insertOne(getCollection(), member);
    }

    @Override
    public void deleteById(String id) {
        Objects.requireNonNull(id, "Member ID cannot be null");
        if (id.isBlank()) {
            return;
        }

        Bson filter = Filters.eq("_id", new ObjectId(id));
        collectionOps.deleteOne(getCollection(), filter);
    }
    
    @Override
    public void update(Member member) {
        Objects.requireNonNull(member.id());

        Bson filter = Filters.eq("_id", member.id());
        collectionOps.updateOne(getCollection(), filter, member);
    }

    private MongoCollection<Member> getCollection() {
        return mongoClient.getDatabase(databaseName).getCollection(collectionName, Member.class);
    }
}
