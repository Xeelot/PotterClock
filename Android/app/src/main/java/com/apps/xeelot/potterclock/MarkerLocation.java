package com.apps.xeelot.potterclock;

import android.content.res.Resources;
import android.graphics.Color;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIgnore;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

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

    @DynamoDBIgnore
    public String getIndexName(Resources res) { return res.getStringArray(R.array.index_names)[index]; }

    @DynamoDBIgnore
    public BitmapDescriptor getIconDescriptor() {
        float f;
        switch(index) {
            case 1:  f = BitmapDescriptorFactory.HUE_RED; break;
            case 2:  f = 150.0F; break;
            case 3:  f = BitmapDescriptorFactory.HUE_ROSE; break;
            case 4:  f = BitmapDescriptorFactory.HUE_MAGENTA; break;
            case 5:  f = BitmapDescriptorFactory.HUE_YELLOW; break;
            case 6:  f = 90.0F; break;
            case 7:  f = BitmapDescriptorFactory.HUE_GREEN; break;
            case 8:  f = BitmapDescriptorFactory.HUE_CYAN; break;
            case 9:  f = BitmapDescriptorFactory.HUE_AZURE; break;
            case 10: f = BitmapDescriptorFactory.HUE_VIOLET; break;
            case 11: f = BitmapDescriptorFactory.HUE_BLUE; break;
            case 0:
            default: f = BitmapDescriptorFactory.HUE_ORANGE;
        }
        return BitmapDescriptorFactory.defaultMarker(f);
    }

    @DynamoDBIgnore
    public int getCircleColor() {
        int i;
        switch(index) {
            case 1:  i = Color.RED; break;
            case 2:  i = 0xFF00FF80; break;
            case 3:  i = 0xFFFF0080; break;
            case 4:  i = Color.MAGENTA; break;
            case 5:  i = Color.YELLOW; break;
            case 6:  i = 0xFF80FF00; break;
            case 7:  i = Color.GREEN; break;
            case 8:  i = Color.CYAN; break;
            case 9:  i = 0xFF0080FF; break;
            case 10: i = 0xFF8000FF; break;
            case 11: i = Color.BLUE; break;
            case 0:
            default: i = 0xFFFF8000; break;
        }
        return i;
    }
}
