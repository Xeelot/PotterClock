package com.apps.xeelot.potterclock;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIgnore;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "MarkerLocationTable")
public class MarkerLocation {
    private double latitude;
    private double longitude;
    private int index;
    private int radius;
    private String name;
    private String userid;

    @DynamoDBHashKey(attributeName = "latitude")
    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    @DynamoDBRangeKey(attributeName = "longitude")
    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    @DynamoDBAttribute(attributeName = "index")
    public int getIndex() { return index; }

    public void setIndex(int index) { this.index = index; }

    @DynamoDBAttribute(attributeName = "radius")
    public int getRadius() { return radius; }

    public void setRadius(int radius) { this.radius = radius; }

    @DynamoDBAttribute(attributeName = "name")
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    @DynamoDBAttribute(attributeName = "userid")
    public String getUserid() { return userid; }

    public void setUserid(String userid) { this.userid = userid; }
}
