package io.sugo.services.usergroup.model;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.sugo.common.redis.RedisInfo;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.common.utils.QueryUtil;
import io.sugo.services.cache.Caches;
import io.sugo.services.usergroup.model.query.Query;
import io.sugo.services.usergroup.model.query.UserGroupQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenyuzhi on 19-8-5.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class UindexGroupBean extends UserGroupBean {
	private static final Logger log = LogManager.getLogger(UserGroupBean.class);
	private static final String TYPE="uindex";
	protected final Caches.RedisClientCache redisClientCache;
	protected  String broker;
	protected String op;
	@JsonCreator
	public UindexGroupBean(
			@JsonProperty("type") String type,
			@JsonProperty("broker") String broker,
			@JsonProperty("query") Query query,
			@JsonProperty("op") String op,
			@JacksonInject Caches.RedisClientCache redisClientCache

	) {
		super(type,query,op, null);
		Preconditions.checkNotNull(broker, "broker can not be null.");
		this.broker = broker;
		this.op = op;
		this.redisClientCache = redisClientCache;
	}


	@JsonProperty
	public String getType() {
		return TYPE;
	}

	@Override
	public  Set<String>  getData() {
		QueryUtil.getUserGroupQueryResult(broker, (UserGroupQuery) query);
		return super.getData();
	}

	@JsonProperty
	public String getBroker() {
		return broker;
	}

	@Override
	public void close() {
		//删除临时分群
		RedisInfo redisInfo = query.getDataConfig().getRedisInfo();
		String userGroupKeys = query.getDataConfig().getGroupId();
		redisClientCache.delete(redisInfo, userGroupKeys);
		log.info(String.format("Delete userGroups %s with config: %s", userGroupKeys, redisInfo));
	}
}
