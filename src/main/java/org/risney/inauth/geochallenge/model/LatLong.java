package org.risney.inauth.geochallenge.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LatLong {

	public double latitude;
	public double longitude;

	public LatLong() {

	}

	public LatLong(double lat, double lon) {
		latitude = lat;
		longitude = lon;
	}

	public LatLong(String json) {
		Gson gson = new GsonBuilder().serializeNulls().create();
		LatLong latlon = gson.fromJson(json, LatLong.class);
		this.latitude = latlon.latitude;
		this.longitude = latlon.longitude;
	}

	public String toJSON() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.toJson(this);
	}

}