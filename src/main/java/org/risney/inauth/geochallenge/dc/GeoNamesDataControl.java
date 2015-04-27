package org.risney.inauth.geochallenge.dc;

import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.risney.inauth.geochallenge.model.Location;

public class GeoNamesDataControl {
	private static final Logger logger = Logger
			.getLogger(GeoNamesDataControl.class.getName());
	private static final int MAX_ROWS = 1;
	private static final String USER_NAME = "mrisney";
	private Map<String, Location> suggestedLocations;

	public GeoNamesDataControl() {
		super();

		logger.setLevel(Level.ALL);
	}

	/**
	 * 
	 * @param searchString
	 * @return org.risney.inauth.geochallenge.model.Location fuzzySearch
	 *         constructs a org.geonames.ToponymSearchCriteria object and uses
	 *         the Geonames method WebService.search(searchCriteria) returning a
	 *         list of Toponyms. For the purpose of this example application,
	 *         the last - or only Toponym is used, more sophisticated
	 *         applications could fine tune the results.
	 * 
	 */

	public Location fuzzySearch(String searchString) {
		Location location = new Location();
		WebService.setUserName(USER_NAME);

		suggestedLocations = new HashMap<String, Location>();
		try {
			ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
			searchCriteria.setStyle(Style.FULL);
			searchCriteria.setMaxRows(MAX_ROWS);
			searchCriteria.setQ(searchString);

			ToponymSearchResult searchResult = WebService
					.search(searchCriteria);
			for (Toponym toponym : searchResult.getToponyms()) {
				location = getLocation(toponym, false);
			}
		} catch (Exception ex) {
			logger.severe(ex.getMessage());
		}
		return location;
	}

	/**
	 *
	 * @param toponym
	 * @param reQueryWebServie
	 * @return org.risney.inauth.geochallenge.model.Location
	 *
	 *         Creates a comma separated string, by concatenating the Place
	 *         name, followed the successive Admin codes that the adminName1,
	 *         adminName2 and adminName3 provide - usually the city, followed by
	 *         the state or province followed by the district or in the US - the
	 *         county, followed by the country name. returns a Location object
	 *
	 *         There seems to be a bug in WebServices.findNearbyPlaceName - that
	 *         returns a Toponym without a full feature, so using
	 *         WebService.get(Toponym.getGeoNameId()..) seems to solve that
	 *         problem. Passing a Boolean lets this method know whether to call
	 *         it or not.
	 *
	 */
	private Location getLocation(Toponym toponym, Boolean reQueryWebServie) {
		Location location = new Location();
		StringBuffer sb = new StringBuffer(toponym.getName());

		try {
			// Workaround as the Toponym from WebServices.findNearbyPlaceName
			// does not contain
			// AdminName1, however the resulting Toponym from WebService.get
			// does.
			// so if this Boolean is true, re-query the toponym, using the
			// geoname identifier.
			if (reQueryWebServie) {
				WebService.setUserName(USER_NAME);
				toponym = WebService.get(toponym.getGeoNameId(), null, null);
			}
			if (toponym.getAdminName1().length() > 0) {
				sb.append(", " + toponym.getAdminName1());
			}

		} catch (Exception ex) {
			logger.warning(ex.getMessage());
		}
		sb.append(", " + toponym.getCountryName());
		location.location = sb.toString();
		location.latitude = toponym.getLatitude();
		location.longitude = toponym.getLongitude();
		return location;
	}

	/**
	 * 
	 * @param latitude
	 * @param longitude
	 * @return org.risney.inauth.geochallenge.mode.Location
	 * 
	 *         getLocationAndWeatherWithCoordinates uses
	 *         org.geonames.ToponymSearchCriteria object and uses the Geonames
	 *         method WebService.search(searchCriteria) returning a list of
	 *         Toponyms. For the purpose of this example application, the last -
	 *         or only Toponym is used, more sophisticated applications could
	 *         fine tune the results.
	 */

	public Location getLocationWithCoordinates(Double latitude, Double longitude) {
		WebService.setUserName(USER_NAME);
		Location location = new Location(latitude, longitude);
		try {

			List<Toponym> toponyms = WebService.findNearbyPlaceName(latitude,
					longitude);
			for (Toponym toponym : toponyms) {
				location = getLocation(latitude, longitude, toponym, true);
			}

		} catch (Exception ex) {
			logger.severe(ex.getMessage());
		}
		return location;
	}

	/**
	 * 
	 * @param toponym
	 * @param reQueryWebServie
	 * @return org.risney.inauth.geochallenge.mode..Location
	 * 
	 *         Creates a comma separated string, by concatenating the Place
	 *         name, followed the successive Admin codes that the adminName1,
	 *         adminName2 and adminName3 provide - usually the city, followed by
	 *         the state or province followed by the district or in the US - the
	 *         county, followed by the country name. returns a Location object
	 * 
	 *         There seems to be a bug in WebServices.findNearbyPlaceName - that
	 *         returns a Toponym without a full feature, so using
	 *         WebService.get(Toponym.getGeoNameId()..) seems to solve that
	 *         problem. Passing a Boolean lets this method know whether to call
	 *         it or not.
	 * 
	 */
	private Location getLocation(Double latitude, Double longitude,
			Toponym toponym, Boolean reQueryWebServie) {

		StringBuffer sb = new StringBuffer(toponym.getName());

		try {
			// Workaround as the Toponym from WebServices.findNearbyPlaceName
			// does not contain
			// AdminName1, however the resulting Toponym from WebService.get
			// does.
			// so if this Boolean is true, re-query the toponym, using the
			// geoname identifier.
			if (reQueryWebServie) {
				WebService.setUserName(USER_NAME);
				toponym = WebService.get(toponym.getGeoNameId(), null, null);
			}
			if (toponym.getAdminName1().length() > 0) {
				sb.append(", " + toponym.getAdminName1());
			}

		} catch (Exception ex) {
			logger.warning(ex.getMessage());
		}
		sb.append(", " + toponym.getCountryName());

		Location location = new Location(latitude, longitude, sb.toString());

		return location;
	}

	public static void main(String[] args) {

		GeoNamesDataControl dataControl = new GeoNamesDataControl();

		// Double latitude = new Double(37.77493);
		// Double longitude = new Double(-122.41942);
		Double latitude = new Double(42.9758);
		Double longitude = new Double(141.5672);

		Location location = dataControl.fuzzySearch("Tokyo");
		logger.log(Level.INFO, location.toJSON());
		/*
		 * for (Locations.Location location : locations.getLocations()) {
		 * logger.log(Level.INFO, location.toJSON()); }
		 * 
		 * Location location01 =
		 * dataControl.getLocationWithCoordinates(latitude, longitude);
		 * logger.log(Level.INFO,
		 * "location from 'getLocationWithCoordinates' : " +
		 * location01.toJSON());
		 */
	}
}