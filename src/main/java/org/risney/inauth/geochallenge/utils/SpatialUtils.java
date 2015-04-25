package org.risney.inauth.geochallenge.utils;

import java.util.Random;

import org.risney.inauth.geochallenge.model.LatLong;

public class SpatialUtils {

	public static double distance(double lat1, double lon1, double lat2, double lon2, String sr) {

		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (sr.equals("K")) {
			dist = dist * 1.609344;
		} else if (sr.equals("N")) {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	public static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	public static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	/**
	 * Creates a random latitude and longitude. (Not inclusive of (-90, 0))
	 */
	public static LatLong random() {
		return random(new Random());
	}

	public static LatLong random(Random r) {
		return new LatLong((r.nextDouble() * -180.0) + 90.0,
				(r.nextDouble() * -360.0) + 180.0);
	}

	public static void main(String[] args) {
		SpatialUtils spatialUtils = new SpatialUtils();
		// Tokyo, japan
		double[] latlong1 = { 35.709026f, 139.731992f };

		// Hiroshima, japan
		double[] latlong2 = { 42.9758f, 141.5672f };

		double distance = SpatialUtils.distance(latlong1[0], latlong1[1],latlong2[0], latlong2[1], "M");
		System.out.println("distance = " + distance);

	}

}
