package com.apps.xeelot.potterclock;


import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mContents;

    CustomInfoWindowAdapter(LayoutInflater layoutInflater) {
        mContents = layoutInflater.inflate(R.layout.map_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        render(marker, mContents);
        return mContents;
    }

    private void render(Marker marker, View view) {
        TextView name = (TextView)view.findViewById(R.id.textName);
        TextView category = (TextView)view.findViewById(R.id.textCategory);
        name.setText(marker.getTitle());
        category.setText(marker.getSnippet());
    }
}
