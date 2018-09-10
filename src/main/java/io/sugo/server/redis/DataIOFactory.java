package io.sugo.server.redis;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DataRedisIOFactory.class)
@JsonSubTypes(value = {
		@JsonSubTypes.Type(name = DataIOFactory.REDIS, value = DataRedisIOFactory.class)

})
public interface DataIOFactory
{
	String REDIS = "redis";

	byte[] readBytes();

	void writeBytes(byte[] buf);

	String getGroupId();

	void close();
}