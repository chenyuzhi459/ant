package io.sugo.services.usergroup.model;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.Preconditions;
import io.sugo.common.redis.RedisInfo;
import io.sugo.services.usergroup.model.query.Query;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl=UserGroupBean.class)
@JsonSubTypes(value = {
		@JsonSubTypes.Type(name = "usergroup", value = UserGroupBean.class),
		@JsonSubTypes.Type(name = "finalGroup", value = FinalGroupBean.class),
		@JsonSubTypes.Type(name = "uindex", value = UindexGroupBean.class),
		@JsonSubTypes.Type(name = "tindex", value = TindexGroupBean.class)
})
public abstract class GroupBean implements Closeable{
	protected  String type;
	protected Query query;      //maybe userGroupQuery or groupByQuery


	@JsonCreator
	public GroupBean(
			@JsonProperty("type") String type,
			@JsonProperty("query") Query query
	) {
		Preconditions.checkNotNull(query, "query can not be null.");
		this.type = type;
		this.query = query;
	}

	public abstract String getType();

	@JsonProperty
	public Query getQuery() {
		return query;
	}

	public abstract Set<String>  getData(Map<RedisInfo, Set<String>> tempGroups);

	public void close(){

	}
}
