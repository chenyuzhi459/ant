package io.sugo.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A map to hold the fixed quantity entry, it will   remove eldest map-entry automatically when new mappings
 * are added to the map and size() > maxSize.
 *
 * Created by chenyuzhi on 18-9-21.
 */
public class CapacityMap<K, V> extends LinkedHashMap<K, V>{
	private final int maxSize;

	public CapacityMap(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry eldest) {
		return size() > maxSize;
	}
}
