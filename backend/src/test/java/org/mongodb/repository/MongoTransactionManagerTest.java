package org.mongodb.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.function.Function;
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MongoTransactionManagerTest {

    private MongoClient mockMongoClient;
    private ClientSession mockSession;
    private MongoTransactionManager txManager;

    @BeforeEach
    void setUp() {
        mockMongoClient = mock(MongoClient.class);
        mockSession = mock(ClientSession.class);
        when(mockMongoClient.startSession()).thenReturn(mockSession);
        txManager = new MongoTransactionManager(mockMongoClient);
    }

    @Test
    void testExecute_CommitsTransactionAndReturnsResult() {
        when(mockSession.hasActiveTransaction()).thenReturn(false);
        doNothing().when(mockSession).startTransaction(any(TransactionOptions.class));
        doNothing().when(mockSession).commitTransaction();
        doNothing().when(mockSession).close();

        Function<ClientSession, String> callback = session -> "success";
        Optional<String> result = txManager.execute(callback);

        assertTrue(result.isPresent());
        assertEquals("success", result.get());
        verify(mockSession).startTransaction(any(TransactionOptions.class));
        verify(mockSession).commitTransaction();
    }

    @Test
    void testExecute_WithActiveTransaction_DoesNotStartOrCommit() {
        txManager = new MongoTransactionManager(mockMongoClient);

        Function<ClientSession, Integer> callback = session -> {
            when(mockSession.hasActiveTransaction()).thenReturn(true);

            Optional<String> callback2 = txManager.execute(s -> "test");
            assertTrue(callback2.isPresent());
            assertEquals("test", callback2.get());

            return 42;
        };
        Optional<Integer> result = txManager.execute(callback);

        assertTrue(result.isPresent());
        assertEquals(42, result.get());
        verify(mockSession, times(1)).startTransaction(any(TransactionOptions.class));
        verify(mockSession, times(1)).commitTransaction();
    }

    @Test
    void testExecute_WhenCallbackThrows_AbortsTransactionAndThrows() {
        when(mockSession.hasActiveTransaction()).thenReturn(false);
        doNothing().when(mockSession).startTransaction(any(TransactionOptions.class));
        doNothing().when(mockSession).abortTransaction();
        doNothing().when(mockSession).close();

        Function<ClientSession, String> callback = session -> {
            throw new RuntimeException("fail");
        };

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> txManager.execute(callback));
        assertTrue(thrown.getMessage().contains("Transaction failed"));
        verify(mockSession).abortTransaction();
    }
}