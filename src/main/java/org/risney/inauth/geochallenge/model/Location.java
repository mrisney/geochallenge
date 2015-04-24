package org.risney.inauth.geochallenge.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Location extends LatLong {

	public String location;

	public Location() {
		super();
	}

	public Location(String location) {
		super();
		this.location = location;
	}

	public Location(double latitude, double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Location(double latitude, double longitude, String location) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.location = location;
	}

	public String toJSON() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.toJson(this);
	}

}
