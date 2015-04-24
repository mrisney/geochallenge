package org.risney.inauth.geochallenge.dc;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.risney.inauth.geochallenge.model.LatLong;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RedisDataControl {
	private static final Logger logger = Logger.getLogger(RedisDataControl.class.getName());
	
	public Jedis jedis;

	public RedisDataControl() {
		this.jedis = new Jedis("localhost");
	}

	public void addLatLong(LatLong latlong) {
		jedis.hset("latlongs", latlong.latitude + ":" + latlong.longitude,
				latlong.toJSON());
	}

	public LatLong getLatLong(String key) {
		String json = jedis.hget("latlongs", key);
		return new LatLong(json);
	}

	public void addOutsideUSLatLong(LatLong latlong) {
		jedis.hset("outsideUS", latlong.latitude + ":" + latlong.longitude,
				latlong.toJSON());
	}

	public Set<LatLong> getAllLatLongs() {
		// HGETALL key returns a hash table in key, all fields and values.
		// just return the values, not the keys
		Set<LatLong> latLongs = new HashSet<LatLong>();
		Map<String, String> allmap = jedis.hgetAll("latlongs");
		for (Map.Entry entry : allmap.entrySet()) {
			latLongs.add(new LatLong(entry.getValue().toString()));
		}
		return latLongs;
	}

	public Set<LatLong> getAllLatLongsOutsideUS() {
		// HGETALL key returns a hash table in key, all fields and values.
		// just return the values, not the keys
		Set<LatLong> latLongs = new HashSet<LatLong>();
		Map<String, String> allmap = jedis.hgetAll("outsideUS");
		for (Map.Entry entry : allmap.entrySet()) {
			latLongs.add(new LatLong(entry.getValue().toString()));
		}
		return latLongs;
	}

	public void removeAll() {
		jedis.flushDB();
	}

	public static void main(String[] args) {
		RedisDataControl redisDC = new RedisDataControl();

		for (int i = 0; i < 50; i++) {
			double lat = (Math.random() * 180.0) - 90.0;
			double lon = (Math.random() * 360.0) - 180.0;
			redisDC.addLatLong(new LatLong(lat, lon));
		}

		Set<LatLong> latLongs = redisDC.getAllLatLongs();
		Gson gson = new GsonBuilder().serializeNulls().create();
		System.out.println(gson.toJson(latLongs));
		// finally flush the data, we just created
		redisDC.removeAll();
	}
}
