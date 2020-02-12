package io.sugo.common.redis;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = RedisDataIOFetcher.class)
@JsonSubTypes(value = {
		@JsonSubTypes.Type(name = DataIOFetcher.REDIS, value = RedisDataIOFetcher.class)

})
public interface DataIOFetcher
{
	String REDIS = "redis";

	byte[] readBytes();

	void writeBytes(byte[] buf);

	String getGroupId();

	void delete();

	void close();
}