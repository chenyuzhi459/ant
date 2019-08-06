package io.sugo.services.usergroup.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.redis.RedisInfo;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.services.usergroup.model.query.Query;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenyuzhi on 19-8-5.
 */
public class FinalGroupBean extends GroupBean {
	private static final String TYPE="finalGroup";
	protected  Boolean append;   // used for finalGroup

	@JsonCreator
	public FinalGroupBean(
			@JsonProperty("type") String type,
			@JsonProperty("query") Query query,
			@JsonProperty("append") Boolean append

	) {
		super(type,query);
		this.append = append == null ? false : append;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getType() {
		return TYPE;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Boolean isAppend() {
		return append;
	}

	@Override
	public  Set<String>  getData(Map<RedisInfo, Set<String>> tempGroups) {
		UserGroupSerDeserializer serDeserializer = new UserGroupSerDeserializer(query.getDataConfig());
		Set<String> userIds = new HashSet<>();
		serDeserializer.deserialize(userIds);
		return userIds;
	}
}
