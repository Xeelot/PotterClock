package com.app.xeelot.potterclock;


import com.loopj.android.http.RequestParams;

public class Location {
    private String userId;
    private String uniqueId;
    private String category;
    private double latNw;
    private double longNw;
    private double latSe;
    private double longSe;
    private String name;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLatNw() {
        return latNw;
    }

    public void setLatNw(double latNw) {
        this.latNw = latNw;
    }

    public double getLongNw() {
        return longNw;
    }

    public void setLongNw(double longNw) {
        this.longNw = longNw;
    }

    public double getLatSe() {
        return latSe;
    }

    public void setLatSe(double latSe) {
        this.latSe = latSe;
    }

    public double getLongSe() {
        return longSe;
    }

    public void setLongSe(double longSe) {
        this.longSe = longSe;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RequestParams getParams() {
        RequestParams request = new RequestParams();
        request.put("userid", userId);
        request.put("category", category);
        request.put("latNW", latNw);
        request.put("longNW", longNw);
        request.put("latSE", latSe);
        request.put("longSE", longSe);
        request.put("name", name);
        return request;
    }
}
