package org.mongodb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bson.conversions.Bson;

import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MongoCollectionOps {
    private final MongoTransactionManager transactionManager;

    public MongoCollectionOps(MongoTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public <T> FindIterable<T> find(MongoCollection<T> collection, Optional<Bson> filter) {
        return ifInTransactionThenGetOrElseGet(
            session -> collection.find(session, filter.orElse(Filters.empty())),
            () -> collection.find(filter.orElse(Filters.empty()))
        );
    }

    public <T> InsertOneResult insertOne(MongoCollection<T> collection, T document) {
        return ifInTransactionThenGetOrElseGet(
            session -> collection.insertOne(session, document),
            () -> collection.insertOne(document)
        );
    }

    public <T> DeleteResult deleteOne(MongoCollection<T> collection, Bson filter) {
        return ifInTransactionThenGetOrElseGet(
            session -> collection.deleteOne(session, filter),
            () -> collection.deleteOne(filter)
        );
    }

    public <T> UpdateResult updateOne(MongoCollection<T> collection, Bson filter, T document) {
        Bson update = buildUpdateFromNonNullFields(document);

        return ifInTransactionThenGetOrElseGet(
            session -> collection.updateOne(session, filter, update),
            () -> collection.updateOne(filter, update)
        );
    }

    private <T> T ifInTransactionThenGetOrElseGet(
            Function<ClientSession, T> performInTransaction,
            Supplier<T> performWithoutTransaction) {

        RuntimeException transactionFailedException = new RuntimeException("Transaction failed");

        if (transactionManager.getClientSession().isEmpty()) {
            return performWithoutTransaction.get();
        }

        return transactionManager.execute(performInTransaction)
                .orElseThrow(() -> transactionFailedException);
    }

    public static Bson buildUpdateFromNonNullFields(Object record) {
        List<Bson> updates = new ArrayList<>();
        for (var component : record.getClass().getRecordComponents()) {
            try {
                Object value = component.getAccessor().invoke(record);
                if (value != null) {
                    updates.add(Updates.set(component.getName(), value));
                }
            } catch (Exception e) {
                throw new RuntimeException("error while building bson document to update", e);
            }
        }
        return Updates.combine(updates);
    }
}
