package com.apps.xeelot.potterclock;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIgnore;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "CurrentLocationTable")
public class CurrentLocation {
    private String userid;
    private int index;
    private double latitude;
    private double longitude;
    private String timestamp;

    @DynamoDBHashKey(attributeName = "userid")
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    @DynamoDBAttribute(attributeName = "index")
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @DynamoDBAttribute(attributeName = "latitude")
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @DynamoDBAttribute(attributeName = "longitude")
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @DynamoDBAttribute(attributeName = "timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @DynamoDBIgnore
    public Drawable getHandDrawable(Resources res) {
        Drawable temp;
        //TODO: pass in drawable array?
        if(userid.equals(res.getString(R.string.user_joe))) {
            switch (index) {
                case 1:  temp = ResourcesCompat.getDrawable(res, R.drawable.joe1, null); break;
                case 2:  temp = ResourcesCompat.getDrawable(res, R.drawable.joe2, null); break;
                case 3:  temp = ResourcesCompat.getDrawable(res, R.drawable.joe3, null); break;
                case 4:  temp = ResourcesCompat.getDrawable(res, R.drawable.joe4, null); break;
                case 5:  temp = ResourcesCompat.getDrawable(res, R.drawable.joe5, null); break;
                case 6:  temp = ResourcesCompat.getDrawable(res, R.drawable.joe6, null); break;
                case 7:  temp = ResourcesCompat.getDrawable(res, R.drawable.joe7, null); break;
                case 8:  temp = ResourcesCompat.getDrawable(res, R.drawable.joe8, null); break;
                case 9:  temp = ResourcesCompat.getDrawable(res, R.drawable.joe9, null); break;
                case 10: temp = ResourcesCompat.getDrawable(res, R.drawable.joe10, null); break;
                case 11: temp = ResourcesCompat.getDrawable(res, R.drawable.joe11, null); break;
                case 0:
                default: temp = ResourcesCompat.getDrawable(res, R.drawable.joe0, null); break;
            }
        }
        else {
            switch (index) {
                case 1:  temp = ResourcesCompat.getDrawable(res, R.drawable.hannah1, null); break;
                case 2:  temp = ResourcesCompat.getDrawable(res, R.drawable.hannah2, null); break;
                case 3:  temp = ResourcesCompat.getDrawable(res, R.drawable.hannah3, null); break;
                case 4:  temp = ResourcesCompat.getDrawable(res, R.drawable.hannah4, null); break;
                case 5:  temp = ResourcesCompat.getDrawable(res, R.drawable.hannah5, null); break;
                case 6:  temp = ResourcesCompat.getDrawable(res, R.drawable.hannah6, null); break;
                case 7:  temp = ResourcesCompat.getDrawable(res, R.drawable.hannah7, null); break;
                case 8:  temp = ResourcesCompat.getDrawable(res, R.drawable.hannah8, null); break;
                case 9:  temp = ResourcesCompat.getDrawable(res, R.drawable.hannah9, null); break;
                case 10: temp = ResourcesCompat.getDrawable(res, R.drawable.hannah10, null); break;
                case 11: temp = ResourcesCompat.getDrawable(res, R.drawable.hannah11, null); break;
                case 0:
                default: temp = ResourcesCompat.getDrawable(res, R.drawable.hannah0, null); break;
            }
        }
        return temp;
    }
}
