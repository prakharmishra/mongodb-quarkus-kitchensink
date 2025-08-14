package org.mongodb.repository;

import java.util.Optional;
import java.util.function.Function;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class MongoTransactionManager {
    private final MongoClient mongoClient;
    private Optional<ClientSession> clientSession;

    // Transaction options for all transactions
    private static final TransactionOptions TXN_OPTIONS = TransactionOptions.builder()
            .readPreference(ReadPreference.primary())
            .readConcern(ReadConcern.MAJORITY)
            .writeConcern(WriteConcern.MAJORITY)
            .build();

    public MongoTransactionManager(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        this.clientSession = Optional.empty();
    }

    public Optional<ClientSession> getClientSession() {
        return clientSession;
    }

    public <T> Optional<T> execute(Function<ClientSession, T> callback) {
        Optional<T> result = Optional.empty();
        boolean transactionActive = clientSession.isPresent() && clientSession.get().hasActiveTransaction();
        if (!clientSession.isPresent()) {
            // Start a new session
            this.clientSession = Optional.of(mongoClient.startSession());
        }
        ClientSession session = this.clientSession.get();
        if (!transactionActive) {
            // If no active transaction, start a new one
            session.startTransaction(TXN_OPTIONS);
        }
        try {
            result = Optional.of(callback.apply(session));
            if (!transactionActive) {
                // If we started a new transaction, commit it
                session.commitTransaction();
            }
        } catch (Exception e) {
            session.abortTransaction();
            throw new RuntimeException("Transaction failed, rolled back", e);
        }

        return result;
    }

    @PreDestroy
    void cleanup() {
        // Ensure the session is always closed when the request ends
        if (clientSession.isPresent() && clientSession.get().hasActiveTransaction()) {
            // Rollback any uncommitted transaction
            clientSession.get().abortTransaction();
        }
        if (clientSession.isPresent()) {
            clientSession.get().close();
        }
    }
}
