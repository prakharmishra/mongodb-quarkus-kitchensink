package org.mongodb.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongodb.model.Member;
import org.mongodb.model.CursorPage;
import org.mongodb.repository.MongoCollectionOps;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;

public class MongoMemberRepoTest {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Member> collection;
    private MongoCollectionOps collectionOps;
    private MongoMemberRepo repo;
    private FindIterable<Member> findIterable;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mongoClient = mock(MongoClient.class);
        mongoDatabase = mock(MongoDatabase.class);
        collection = mock(MongoCollection.class);
        collectionOps = mock(MongoCollectionOps.class);
        findIterable = mock(FindIterable.class);

        when(mongoClient.getDatabase(anyString())).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection(anyString(), eq(Member.class))).thenReturn(collection);
        when(findIterable.sort(any())).thenReturn(findIterable);
        when(findIterable.limit(anyInt())).thenReturn(findIterable);
        // Using doReturn for generic return type
        doReturn(findIterable).when(collectionOps).find(any(), any());

        repo = new MongoMemberRepo(mongoClient, collectionOps);

        // Set private fields via reflection for testing
        setField(repo, "databaseName", "testdb");
        setField(repo, "collectionName", "members");
    }

    @Test
    void testFindById() {
        // Given
        ObjectId objectId = new ObjectId();
        Member expectedMember = new Member(objectId, "user-id", "johndoe", "John", "Doe", "john@example.com", "1234567890");
        
        when(findIterable.first()).thenReturn(expectedMember);

        // When
        Optional<Member> result = repo.findById(objectId.toString());

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedMember, result.get());
        verify(collectionOps).find(eq(collection), any());
    }

    @Test
    void testFindById_InvalidId() {
        // When
        Optional<Member> result = repo.findById("");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testListMembersPage() {
        // Given
        int pageSize = 2;
        ObjectId cursor = new ObjectId();
        ObjectId id1 = new ObjectId();
        ObjectId id2 = new ObjectId();
        
        List<Member> members = Arrays.asList(
            new Member(id1, "user1", "johndoe", "John", "Doe", "john@example.com", "1234567890"),
            new Member(id2, "user2", "janedoe", "Jane", "Doe", "jane@example.com", "0987654321")
        );

        // findIterable setup is done in setUp()
        when(findIterable.into(any())).thenReturn(members);

        // When
        CursorPage<Member> result = repo.listMembersPage(pageSize, cursor.toString());

        // Then
        assertNotNull(result);
        assertEquals(2, result.data().size());
        assertEquals(id2.toString(), result.nextCursor());
        verify(collectionOps).find(eq(collection), any());
    }

    @Test
    void testListMembersPage_InvalidSize() {
        // When
        CursorPage<Member> result = repo.listMembersPage(0, null);

        // Then
        assertTrue(result.data().isEmpty());
        assertNull(result.nextCursor());
    }

    @Test
    void testSave() {
        // Given
        ObjectId id = new ObjectId();
        Member member = new Member(id, "user-id", "johndoe", "John", "Doe", "john@example.com", "1234567890");

        // When
        repo.register(member);

        // Then
        verify(collectionOps).insertOne(eq(collection), eq(member));
    }

    @Test
    void testDeleteById() {
        // Given
        String id = new ObjectId().toString();

        // When
        repo.deleteById(id);

        // Then
        verify(collectionOps).deleteOne(eq(collection), any(Bson.class));
    }

    @Test
    void testDeleteById_InvalidId() {
        // When
        repo.deleteById("");

        // Then
        verify(collectionOps, never()).deleteOne(any(), any());
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
