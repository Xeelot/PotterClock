package com.app.xeelot.potterclock;


import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class Current {
    private String userId;
    private String position;
    private int month;
    private int day;
    private int year;
    private int hour;
    private int minute;
    private int second;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public RequestParams getParams() {
        RequestParams request = new RequestParams();
        request.put("userid", userId);
        request.put("position", position);
        request.put("month", month);
        request.put("day", day);
        request.put("year", year);
        request.put("hour", hour);
        request.put("minute", minute);
        request.put("second", second);
        return request;
    }
}
