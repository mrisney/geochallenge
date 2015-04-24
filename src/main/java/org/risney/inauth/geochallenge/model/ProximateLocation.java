package org.risney.inauth.geochallenge.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ProximateLocation extends LatLong {

	public String proximateFromCity;
	public double distance;

	public ProximateLocation() {
		super();
	}
	
	public String toJSON() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.toJson(this);
	}


}
