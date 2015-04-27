package org.risney.inauth.geochallenge.rest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.server.ChunkedOutput;
import org.risney.inauth.geochallenge.dc.GeoNamesDataControl;
import org.risney.inauth.geochallenge.dc.GeoToolsDataControl;
import org.risney.inauth.geochallenge.dc.RedisDataControl;
import org.risney.inauth.geochallenge.model.LatLong;
import org.risney.inauth.geochallenge.model.Location;
import org.risney.inauth.geochallenge.model.ProximateLocation;
import org.risney.inauth.geochallenge.utils.SpatialUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Path("/")
public class RESTServiceControl {
	

	private static final Logger logger = Logger
			.getLogger(RESTServiceControl.class.getName());
	
	/*
	 * GET Method of adding a coordinate pair to Redis Database, through Redis DataControl
	 * path parameters are : addData/{latitude}/{longitude
	 * eg. addData/1.0/-1.0
	 */
	@GET
	@Path("addData/{latitude}/{longitude}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addData(@PathParam("latitude") double latitude,
			@PathParam("longitude") double longitude) {
		RedisDataControl redisDC = new RedisDataControl();
		String output = "{coordinates not added}";
		try {
			LatLong latLong = new LatLong(latitude, longitude);
			redisDC.addLatLong(latLong);

			Gson gson = new GsonBuilder().serializeNulls().create();
			output = gson.toJson(latLong);
		} catch (Exception ex) {
			logger.warning(ex.getMessage());
		}

		return Response.status(200).entity(output).build();
	}
	/*
	 * POST Method of adding a coordinate pair to Redis Database, through Redis DataControl
	 * POST parameters are Strings, converted to double :
	 * parameters = (String) latitude and (String) longitude
	 */
	
	@POST
	@Path("postData/{latitude}/{longitude}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response post(String latitude, String longitude) {
		RedisDataControl redisDC = new RedisDataControl();
		String output = "{coordinates not added}";
		try {
			LatLong latLong = new LatLong( Double.parseDouble(latitude), Double.parseDouble(longitude));
			redisDC.addLatLong(latLong);

			Gson gson = new GsonBuilder().serializeNulls().create();
			output = gson.toJson(latLong);
		} catch (Exception ex) {
			logger.warning(ex.getMessage());
		}

		return Response.status(200).entity(output).build();
	}
	
	

	
	@Path("getData/{latitude}/{longitude}")
	public void getData(@PathParam("latitude") double latitude,
			@PathParam("longitude") double longitude) {
		RedisDataControl redisDC = new RedisDataControl();
		try {
			redisDC.addLatLong(new LatLong(latitude,longitude));
		} catch (Exception ex) {
			logger.warning(ex.getMessage());
		}
		
	}

	@GET
	@Path("generateRandomData")
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateRandomData() {

		final RedisDataControl redisDC = new RedisDataControl();
		// remove all pre-existing data first
		redisDC.removeAll();

		final Gson gson = new GsonBuilder().serializeNulls().create();
		StreamingOutput stream = new StreamingOutput() {
			public void write(OutputStream os) throws IOException,
					WebApplicationException {
				Writer writer = new BufferedWriter(new OutputStreamWriter(os));
				for (int i = 0; i < 10000; i++) {
					LatLong latLong = SpatialUtils.random();
					redisDC.addLatLong(latLong);
					writer.write(gson.toJson(latLong) + "\n");
				}
				writer.flush();
			}
		};
		return Response.ok(stream).build();
	}

	@GET
	@Path("findInUSA")
	@Produces(MediaType.TEXT_PLAIN)
	public ChunkedOutput<String> findInUSA() {

		URL resource = getClass().getResource("/");
		String path = resource.getPath();
		String shapeFile = path
				.replace("WEB-INF/classes/",
						"WEB-INF/shape.files/gz_2010_us_040_00_500k/gz_2010_us_040_00_500k.shp");
		final ChunkedOutput<String> output = new ChunkedOutput<String>(
				Location.class);

		final RedisDataControl redisDC = new RedisDataControl();
		final GeoToolsDataControl geotoolsDC = new GeoToolsDataControl(
				shapeFile);
		final GeoNamesDataControl geoNamesDC = new GeoNamesDataControl();
		final Gson gson = new GsonBuilder().serializeNulls().create();

		new Thread() {
			@Override
			public void run() {
				try {

					for (LatLong latlong : redisDC.getAllLatLongs()) {
						if (geotoolsDC.containedInShapeFiles(latlong)) {
							Location location = geoNamesDC
									.getLocationWithCoordinates(
											latlong.latitude, latlong.longitude);
							output.write(location.latitude + ","
									+ location.longitude + ","
									+ location.location + "\n");
						} else {
							redisDC.addOutsideUSLatLong(latlong);
						}

					}
				} catch (IOException ex) {
					logger.severe(ex.getMessage());
				} finally {
					try {
						output.close();
						// simplified: IOException thrown from
						// this close() should be handled here...
					} catch (IOException iex) {
						logger.severe(iex.getMessage());
					}
				}

			}
		}.start();

		return output;
	}

	@GET
	@Path("coordinatesWithin500Miles")
	@Produces(MediaType.TEXT_PLAIN)
	public ChunkedOutput<String> coordinatesWithin500Miles() {

		final GeoNamesDataControl geoNamesDC = new GeoNamesDataControl();
		final RedisDataControl redisDC = new RedisDataControl();
		final Gson gson = new GsonBuilder().serializeNulls().create();
		final ChunkedOutput<String> output = new ChunkedOutput<String>(
				ProximateLocation.class);
		new Thread() {
			@Override
			public void run() {
				try {

					// 1. Lookup the latitude/longitude for each City
					// use the GeoNames webservice to find the Lat Long for the
					// cities
					// Google Map API 2,500 a day vs Geonames 3,000, Geonames
					// trumps Google
					// Geocoding
					// #FUKYOUMountainView
					Set<Location> cityLocations = new HashSet<Location>();

					cityLocations.add(geoNamesDC.fuzzySearch("Tokyo,Japan"));
					cityLocations.add(geoNamesDC
							.fuzzySearch("Sydney, Australia"));
					cityLocations.add(geoNamesDC
							.fuzzySearch("Riyadh, Saudi Arabia"));
					cityLocations.add(geoNamesDC
							.fuzzySearch("Zurich, Switzerland"));
					cityLocations.add(geoNamesDC
							.fuzzySearch("Reykjavik, Iceland"));
					cityLocations.add(geoNamesDC
							.fuzzySearch("Mexico City, Mexico"));
					cityLocations.add(geoNamesDC.fuzzySearch("Lima, Peru"));

					// 2. Loop through all the random generated data,
					// that was not in the U.S

					for (LatLong latlong : redisDC.getAllLatLongsOutsideUS()) {

						// 3. Loop through each city coordinates in
						// cityLocations Set,
						// compare the distance, if < 500 miles, add to
						// proximateLocations
						// collection
						for (Location cityLocation : cityLocations) {

							double distance = SpatialUtils.distance(
									latlong.latitude, latlong.longitude,
									cityLocation.latitude,
									cityLocation.longitude, "M");

							if (distance < 500.0) {
								ProximateLocation proximateLocation = new ProximateLocation();
								proximateLocation.latitude = latlong.latitude;
								proximateLocation.longitude = latlong.longitude;
								proximateLocation.proximateFromCity = cityLocation.location;
								
								// round distance up, to 2 decimal places
								proximateLocation.distance = Math
										.round(distance * 100.0) / 100.0;
								output.write(proximateLocation.latitude + ","
										+ proximateLocation.longitude + " - "
										+ proximateLocation.distance + " miles  from "
							
										+ proximateLocation.proximateFromCity
										+ "\n");
							}
						}
					}
				} catch (IOException iex) {
					logger.severe(iex.getMessage());
				} finally {
					try {
						output.close();
						// simplified: IOException thrown from
						// this close() should be handled here...
					} catch (IOException iex) {
						logger.severe(iex.getMessage());
					}
				}
			}
		}.start();
		return output;
	}

	@GET
	@Path("getAllDataSets")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllDataSets() {

		final RedisDataControl redisDC = new RedisDataControl();
		final Gson gson = new GsonBuilder().serializeNulls().create();
		StreamingOutput stream = new StreamingOutput() {
			public void write(OutputStream os) throws IOException,
					WebApplicationException {
				Writer writer = new BufferedWriter(new OutputStreamWriter(os));
				for (LatLong latlong : redisDC.getAllLatLongs()) {
					writer.write(gson.toJson(latlong) + "\n");
				}
				writer.flush();
			}
		};
		return Response.ok(stream).build();
	}

	@GET
	@Path("removeAllDataSets")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeAllDataSets() {
		RedisDataControl redisDC = new RedisDataControl();
		redisDC.removeAll();
		return Response.status(200).entity("{OK}").build();
	}
	
	
	public class CityNameComparator implements Comparator<ProximateLocation> {
	    public int compare(ProximateLocation o1, ProximateLocation o2) {
	        return o1.proximateFromCity.compareTo(o2.proximateFromCity);
	    }
	}

}