package io.sugo.services.tag.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by chenyuzhi on 18-9-25.
 */
public class UpdateBatch {
	private final Map<String, Object> values;
	private final Map<String, Boolean> appendFlags;

	@JsonCreator
	public UpdateBatch(
			@JsonProperty("values") Map<String, Object> values,
			@JsonProperty("appendFlags") Map<String, Boolean> appendFlags
	) {
		this.values = values;
		this.appendFlags = appendFlags;
	}

	@JsonProperty
	public Map<String, Object> getValues() {
		return values;
	}

	@JsonProperty
	public Map<String, Boolean> getAppendFlags() {
		return appendFlags;
	}
}
