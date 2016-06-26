package com.app.xeelot.potterclock;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class AwsDb {

    private CognitoCachingCredentialsProvider credProvider;
    private AmazonDynamoDBClient ddbClient;
    private DynamoDBMapper mapper;

    public void initDb(CognitoCachingCredentialsProvider credProvider) {
        this.credProvider = credProvider;
        ddbClient = new AmazonDynamoDBClient(credProvider);
        mapper = new DynamoDBMapper(ddbClient);
    }

    public void saveCurrent(Current item) {
        mapper.save(item);
    }

    //public void saveLocation(Location item) {
    //    mapper.save(Location);
    //}

}
