package org.risney.inauth.geochallenge.dc;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.risney.inauth.geochallenge.model.LatLong;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class GeoToolsDataControl {
	
	private GeometryFactory geomFctory;
	private FilterFactory2 filterFactory;
	private FileDataStore store;
	private SimpleFeatureCollection features;
	private ReferencedEnvelope env;
	private File file;
	private static final Logger logger = Logger.getLogger(GeoToolsDataControl.class.getName());



	public GeoToolsDataControl(String fileURL) {

		file = new File(fileURL);
		if (!file.exists()) {
			logger.severe(file + " doesn't exist");
			return;
		}
		try {
			store = FileDataStoreFinder.getDataStore(file);
			filterFactory = CommonFactoryFinder.getFilterFactory2(GeoTools
					.getDefaultHints());

			SimpleFeatureSource featureSource = store.getFeatureSource();
			this.setFeatures(featureSource.getFeatures());
			geomFctory = new GeometryFactory();

		} catch (Exception e) {
			logger.severe(e + " unable to connect to " + fileURL
					+ " as data source");
		}
	}

	public boolean containedInShapeFiles(LatLong latLon) {
		Point point = geomFctory.createPoint(new Coordinate(latLon.longitude,
				latLon.latitude));
		if (!env.contains(point.getCoordinate())) {
			return false;
		}
		Expression propertyName = filterFactory.property(features.getSchema()
				.getGeometryDescriptor().getName());
		Filter filter = filterFactory.contains(propertyName,
				filterFactory.literal(point));
		SimpleFeatureCollection sub = features.subCollection(filter);
		if (sub.size() > 0) {
			return true;
		}
		return false;
	}

	private void setFeatures(SimpleFeatureCollection features) {
		this.features = features;
		env = features.getBounds();
	}

	public static void main(String[] args) throws IOException {

		String shapeFile = "/Users/marcrisney/Downloads/gz_2010_us_040_00_500k/gz_2010_us_040_00_500k.shp";
		GeoToolsDataControl geotoolsDC = new GeoToolsDataControl(shapeFile);
		RedisDataControl redisDC = new RedisDataControl();

		Set<LatLong> latLongs = redisDC.getAllLatLongs();
		Set<LatLong> unitedStateslatLongs = new HashSet<LatLong>();
		int i = 0;
		for (LatLong latlong : latLongs) {
			System.out.println(i);
			if (geotoolsDC.containedInShapeFiles(latlong)) {
				System.out.println(latlong);
				i++;
				unitedStateslatLongs.add(latlong);
			}
		}

		Gson gson = new GsonBuilder().serializeNulls().create();
		String output = gson.toJson(unitedStateslatLongs);
		System.out.println(output);
	}
}
