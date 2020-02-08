package io.sugo.services.usergroup.query;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.sugo.common.redis.RedisDataIOFetcher;

/**
 * Created by chenyuzhi on 19-8-5.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "queryType", defaultImpl=UserGroupQuery.class)
@JsonSubTypes(value = {
		@JsonSubTypes.Type(name = "user_group", value = UserGroupQuery.class),
		@JsonSubTypes.Type(name = "lucene_groupBy", value = GroupByQuery.class)
})
public interface Query {
	RedisDataIOFetcher getDataConfig();
}
