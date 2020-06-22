# Description 

This project is meant to demonstrate a failover issue with Cosmos DB using mongo api. There is a class FailoverTest.java that contains a main method. This class creates a collection and inserts a simple document into it. It then proceeds to read that document and update it in an infinite loop. The intent is to run this loop and initiate a manual failover in the azure portal and observe this programs output to see how it behaves during the failover.

## Steps to reproduce issue

### Test 1:

1. Provision a cosmos db instance using the mongo Api on version 3.2. Ensure Geo-Redundancy is enabled.
2. Edit the file FailoverTest.java and update the value of the connection string variable to use your instances connection string.
3. Run FailoverTest.java and ensure the reads and writes are succeeding. You should see output like this if reads and writes are succeeding...
`
Read document: {"_id": {"$oid": "5ef11cafb7f5590927350f33"}, "id": "wQMdgUpUbH", "pk": "pk", "key": "someValue"}
Update Result: [AcknowledgedUpdateResult{matchedCount=1, modifiedCount=1, upsertedId=null}]
`
4. Go to the Azure portal for your database instance, click the Replicate data globally tab.
5. Click the manual failover button. On the next page click the read region, check the checkbox that says "I understand and agree to trigger a failover on my current Write Region." and then click ok.
6. Oberserve the program output.

Results:

The above test results in write failures once the failover is initiated and it never recovers even once the failover is complete. 

### Test 2:
1. Edit the file FailoverTest.java and remove replicaSet=globaldb from the end of your connection string
2. Repeat steps above starting with (3.)

Results:

The above test results in write failures once the failover is initiated and eventuaally will recover once the failover is complete.

## Conclusion:

The addition of replicaSet=globaldb in the connection string causes writes to indefintely fail in the event of a failover. If this were in the context of a server, this would mean the server would have to be restarted in order for writes to resume. Removing replicaSet=globaldb from the connection string makes that mongo client behave as expected, and writes recover after the failover is complete.
