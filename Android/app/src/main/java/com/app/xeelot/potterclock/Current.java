package com.app.xeelot.potterclock;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "Current")
public class Current {
    private String userId;
    private String position;
    private int month;
    private int day;
    private int year;
    private int hour;
    private int minute;
    private int second;

    @DynamoDBHashKey(attributeName = "userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @DynamoDBAttribute(attributeName = "position")
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @DynamoDBAttribute(attributeName = "month")
    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    @DynamoDBAttribute(attributeName = "day")
    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    @DynamoDBAttribute(attributeName = "year")
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @DynamoDBAttribute(attributeName = "hour")
    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    @DynamoDBAttribute(attributeName = "minute")
    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    @DynamoDBAttribute(attributeName = "second")
    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }
}
