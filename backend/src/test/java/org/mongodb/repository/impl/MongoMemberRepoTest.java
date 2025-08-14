package org.mongodb.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongodb.model.Member;
import org.mongodb.repository.MongoTransactionManager;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;

public class MongoMemberRepoTest {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Member> collection;
    private MongoTransactionManager transactionManager;
    private MongoMemberRepo repo;

    @BeforeEach
    void setUp() {
        mongoClient = mock(MongoClient.class);
        mongoDatabase = mock(MongoDatabase.class);
        collection = mock(MongoCollection.class);
        transactionManager = mock(MongoTransactionManager.class);
        when(mongoClient.getDatabase(anyString())).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection(anyString(), eq(Member.class))).thenReturn(collection);

        repo = new MongoMemberRepo(mongoClient, transactionManager);

        // Set private fields via reflection for testing
        setField(repo, "databaseName", "testdb");
        setField(repo, "collectionName", "members");
    }

    @Test
    void testFindById_NoSession() {
        String id = "123";
        Member member = new Member("123", "John Doe", "x@y.com", "1234567890");
        FindIterable<Member> iterable = mock(FindIterable.class);

        when(transactionManager.getClientSession()).thenReturn(Optional.empty());
        when(collection.find(any(Bson.class))).thenReturn(iterable);
        when(iterable.first()).thenReturn(member);

        Optional<Member> result = repo.findById(id);

        assertTrue(result.isPresent());
        assertEquals(member, result.get());
        verify(collection).find(any(Bson.class));
    }

    @Test
    void testFindById_WithSession() {
        String id = "456";
        Member member = new Member("123", "John Doe", "x@y.com", "1234567890");
        ClientSession session = mock(ClientSession.class);

        when(transactionManager.getClientSession()).thenReturn(Optional.of(session));
        when(transactionManager.execute(any())).thenAnswer(invocation -> {
            return Optional.of(member);
        });

        Optional<Member> result = repo.findById(id);

        assertTrue(result.isPresent());
        assertEquals(member, result.get());
        verify(transactionManager).execute(any());
    }

    @Test
    void testFindAll_NoSession() {
        Member member1 = new Member("123", "John Doe", "x@y.com", "1234567890");
        Member member2 = new Member("234", "John Doe 2", "x2@y.com", "1234567890");
        List<Member> members = Arrays.asList(member1, member2);
        FindIterable<Member> iterable = mock(FindIterable.class);

        when(transactionManager.getClientSession()).thenReturn(Optional.empty());
        when(collection.find()).thenReturn(iterable);
        when(iterable.into(any(List.class))).thenAnswer(invocation -> {
            List<Member> list = invocation.getArgument(0);
            list.addAll(members);
            return list;
        });

        List<Member> result = repo.findAll();

        assertEquals(2, result.size());
        verify(collection).find();
    }

    @Test
    void testFindAll_WithSession() {
        Member member1 = new Member("123", "John Doe", "x@y.com", "1234567890");
        Member member2 = new Member("234", "John Doe 2", "x2@y.com", "1234567890");
        List<Member> members = Arrays.asList(member1, member2);
        ClientSession session = mock(ClientSession.class);

        when(transactionManager.getClientSession()).thenReturn(Optional.of(session));
        when(transactionManager.execute(any())).thenAnswer(invocation -> {
            return Optional.of(members);
        });

        List<Member> result = repo.findAll();

        assertEquals(2, result.size());
        verify(transactionManager).execute(any());
    }

    @Test
    void testSave_NoSession() {
        Member member = new Member("123", "John Doe", "x@y.com", "1234567890");

        when(transactionManager.getClientSession()).thenReturn(Optional.empty());

        repo.save(member);

        verify(collection).insertOne(member);
    }

    @Test
    void testSave_WithSession() {
        Member member = new Member("123", "John Doe", "x@y.com", "1234567890");
        ClientSession session = mock(ClientSession.class);

        when(transactionManager.getClientSession()).thenReturn(Optional.of(session));

        repo.save(member);

        verify(transactionManager).execute(any());
    }

    @Test
    void testDeleteById_NoSession() {
        String id = "789";

        when(transactionManager.getClientSession()).thenReturn(Optional.empty());

        repo.deleteById(id);

        verify(collection).deleteOne(any(Bson.class));
    }

    @Test
    void testDeleteById_WithSession() {
        String id = "101";
        ClientSession session = mock(ClientSession.class);

        when(transactionManager.getClientSession()).thenReturn(Optional.of(session));

        repo.deleteById(id);

        verify(transactionManager).execute(any());
    }

    // Helper to set private fields via reflection
    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
