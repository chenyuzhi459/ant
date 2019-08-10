package io.sugo.services.usergroup.model;

import com.fasterxml.jackson.annotation.*;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.services.tag.DataUpdateHelper;
import io.sugo.services.usergroup.UpdateSpec;
import io.sugo.services.usergroup.UserGroupHelper;
import io.sugo.services.usergroup.model.query.Query;
import io.sugo.services.usergroup.parser.Parser;

import java.util.*;


@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class UserGroupBean extends GroupBean{
	public static final Set<String> INDEX_TYPES = new HashSet<>(Arrays.asList("tindex","uindex"));
	public static final String TYPE="usergroup";
	protected  String op;        // used for AssistantGroup
	protected UpdateSpec to;


	@JsonCreator
	public UserGroupBean(
			@JsonProperty("type") String type,
			@JsonProperty("query") Query query,
			@JsonProperty("op") String op,
			@JsonProperty("to") UpdateSpec to
	) {

		super(type, query);
		this.op = op == null ? "" : op;
		this.to = to;

	}

	@JsonProperty
	public String getType() {
		return TYPE;
	}


	@JsonProperty
	public String getOp() {
		return op;
	}

	@JsonProperty
	public UpdateSpec getTo() {
		return to;
	}

	public  Set<String>  getData(){
		UserGroupSerDeserializer serDeserializer = new UserGroupSerDeserializer(query.getDataConfig());
		Set<String> userIds = new HashSet<>();
		serDeserializer.deserialize(userIds);
		return userIds;
	}

	public void updateParsedData(Set<String> distinct_ids, String operationId, UserGroupHelper userGroupHelper) throws  Exception{
		if(to == null) {return;}

		List<Map> updateRecords = new LinkedList<>();
		List<Parser> parsers = to.getParsers();

		//转换数据
		for(String distinct_id : distinct_ids){
			Map<String, Object> parsedRecord = new HashMap<>(parsers.size());
			for(Parser parser : parsers){
				//默认都是FixedParser
				parsedRecord.put(parser.getName(), parser.getParsedVal(null));
			}
			parsedRecord.put(to.getUindexKey(), distinct_id);
			updateRecords.add(parsedRecord);
		}
		DataUpdateHelper.updateQueryData(updateRecords, to, operationId, userGroupHelper);
	}

}
