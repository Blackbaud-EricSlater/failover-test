package com.mongo.failover;

import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.Success;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;

import static com.mongodb.client.model.Filters.*;
import static com.mongo.failover.SubscriberHelpers.*;

public class FailoverTest {
    public static void main(String[] args) {
        String connectionString = "put connection string here";

        MongoClient client = MongoClients.create(connectionString);
        MongoDatabase database = client.getDatabase("failover-test-db");
        MongoCollection<Document> collection = database.getCollection("failover-test");

        String generatedId = RandomStringUtils.randomAlphabetic(10);

        Document doc = new Document("id", generatedId)
            .append("pk", "pk")
            .append("key", "someValue");

        //create
        ObservableSubscriber<Success> createSubscriber = new OperationSubscriber<>();
        collection.insertOne(doc).subscribe(createSubscriber);

        try {
            createSubscriber.await();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        while (true) {

            //read
            ObservableSubscriber<Document> readSubscriber = new PrintDocumentSubscriber();
            collection.find(eq("id", generatedId)).first().subscribe(readSubscriber);

            try {
                readSubscriber.await();
            } catch (Throwable e) {
                System.out.println("read failed: " + e.getMessage());
            }

            //update
            String generatedValue = RandomStringUtils.randomAlphabetic(10);
            ObservableSubscriber<UpdateResult> updateSubscriber = new PrintSubscriber<>("Update Result: %s");
            collection.updateOne(eq("id", generatedId), new Document("$set", new Document("key", generatedValue)))
                .subscribe(updateSubscriber);

            try {
                updateSubscriber.await();
            } catch (Throwable e) {
                System.out.println("update failed: " + e.getMessage());
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

//        client.close();
    }
}
