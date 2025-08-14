package org.mongodb.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.mongodb.model.Member;
import org.mongodb.repository.MemberRepo;
import org.mongodb.repository.MongoTransactionManager;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RunOnVirtualThread
public class MongoMemberRepo implements MemberRepo {
    private final MongoClient mongoClient;
    private final MongoTransactionManager transactionManager;

    @ConfigProperty(name = "quarkus.mongodb.database")
    private String databaseName;

    @ConfigProperty(name = "quarkus.mongodb.collection.members")
    private String collectionName;

    public MongoMemberRepo(MongoClient mongoClient, MongoTransactionManager transactionManager) {
        this.mongoClient = mongoClient;
        this.transactionManager = transactionManager;
    }
    
    @Override
    public Optional<Member> findById(String id) {
        if (transactionManager.getClientSession().isEmpty()) {
            return Optional.ofNullable(getCollection().find(Filters.eq("_id", id)).first());
        }

        Optional<Member> member = transactionManager.execute(session -> {
            return getCollection().find(session, Filters.eq("_id", id)).first();
        });

        return member;
    }

    @Override
    public List<Member> findAll() {
        if (transactionManager.getClientSession().isEmpty()) {
            return getCollection().find().into(new ArrayList<>());
        }
        Optional<List<Member>> members = transactionManager.execute(session -> {
            return getCollection().find(session).into(new ArrayList<>());
        });

        return members.orElse(new ArrayList<>());
    }

    @Override
    public void save(Member member) {
        if (transactionManager.getClientSession().isEmpty()) {
            getCollection().insertOne(member);
            return;
        }

        transactionManager.execute(session -> {
            getCollection().insertOne(session, member);
            return null; // Void return type
        });
    }

    @Override
    public void deleteById(String id) {
        if (transactionManager.getClientSession().isEmpty()) {
            getCollection().deleteOne(Filters.eq("_id", id));
            return;
        }

        transactionManager.execute(session -> {
            getCollection().deleteOne(session, Filters.eq("_id", id));
            return null; // Void return type
        });
    }
    
    private MongoCollection<Member> getCollection() {
        return mongoClient.getDatabase(databaseName).getCollection(collectionName, Member.class);
    }
}
