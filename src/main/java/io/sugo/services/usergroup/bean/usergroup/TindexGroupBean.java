package io.sugo.services.usergroup.bean.usergroup;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.utils.QueryUtil;
import io.sugo.services.cache.Caches;
import io.sugo.services.tag.DataUpdateHelper;
import io.sugo.services.usergroup.UpdateSpec;
import io.sugo.services.usergroup.UserGroupHelper;
import io.sugo.services.query.GroupByQuery;
import io.sugo.services.query.Query;
import io.sugo.services.query.UserGroupQuery;
import io.sugo.services.usergroup.parser.Parser;

import java.util.*;

/**
 * Created by chenyuzhi on 19-8-5.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class TindexGroupBean extends UindexGroupBean {
	private static final String TYPE = "tindex";
	protected UpdateSpec to;   //用于说明把计算结果发往哪个地方
	protected  String broker;
	private DataUpdateHelper dataUpdateHelper;
	private String groupByDim;

	private List<Map> queryResult;
	@JsonCreator
	public TindexGroupBean(
			@JsonProperty("type") String type,
			@JsonProperty("broker") String broker,
			@JsonProperty("query") Query query,
			@JsonProperty("op") String op,
			@JsonProperty("groupByDim") String groupByDim,
			@JsonProperty("to") UpdateSpec to,
			@JacksonInject DataUpdateHelper dataUpdateHelper,
			@JacksonInject Caches.RedisClientCache redisClientCache

	) {
		super(type,broker,query, op, redisClientCache);
		//兼容旧接口,暂不做检查
//		Preconditions.checkNotNull(groupByDim, "groupByDim can not be null.");
		this.broker = broker;
		this.dataUpdateHelper = dataUpdateHelper;
		this.groupByDim = groupByDim;
		this.to = to;
	}

	@JsonProperty
	public String getType() {
		return TYPE;
	}

	@Override
	public  Set<String>  getData() {
		if(query instanceof UserGroupQuery){
			//兼容旧接口
			return super.getData();
		}

		queryResult = QueryUtil.getGroupByQueryResult(getBroker(),(GroupByQuery)query);
		Set<String> userIds = new HashSet<>();
		for(Map resultMap : queryResult){
			Map<String, Object> eventData = (Map<String, Object>)resultMap.get("event");
			userIds.add(eventData.get(groupByDim).toString());
		}

		return userIds;
	}

	@JsonProperty
	public String getBroker() {
		return broker;
	}

	@JsonProperty
	public String getGroupByDim() {
		return groupByDim;
	}


	@JsonProperty
	public UpdateSpec getTo() {
		return to;
	}

	public void updateParsedData(Set<String> distinct_ids, String operationId, UserGroupHelper userGroupHelper) throws  Exception{
		if(to == null) {return;}

		List<Map> updateRecords = new LinkedList<>();
		List<Parser> parsers = to.getParsers();

		//转换数据
		for(Map rawData : queryResult){
			Map<String, Object> eventData = (Map<String,Object>)rawData.get("event");
			String groupVal = eventData.get(groupByDim).toString();
			if(!distinct_ids.contains(groupVal)){continue;}

			Map<String, Object> parsedRecord = new HashMap<>(parsers.size());
			for(Parser parser : parsers){
				parsedRecord.put(parser.getName(), parser.getParsedVal(rawData));
			}
			parsedRecord.put(to.getUindexKey(), groupVal);
			updateRecords.add(parsedRecord);
		}
		DataUpdateHelper.updateQueryData(updateRecords, to, operationId, userGroupHelper);
	}

	@Override
	public void close() {
		if(queryResult != null){
			queryResult.clear();
		}


		if(query instanceof UserGroupQuery){
			super.close();
		}

	}
}
