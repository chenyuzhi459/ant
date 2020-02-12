package io.sugo.services.usergroup.bean.usergroup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.services.query.Query;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenyuzhi on 19-8-5.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
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
	public String getType() {
		return TYPE;
	}

	@JsonProperty
	public Boolean isAppend() {
		return append;
	}

	@Override
	public  Set<String>  getData() {
		UserGroupSerDeserializer serDeserializer = new UserGroupSerDeserializer(query.getDataConfig());
		Set<String> userIds = new HashSet<>();
		serDeserializer.deserialize(userIds);
		return userIds;
	}
}
