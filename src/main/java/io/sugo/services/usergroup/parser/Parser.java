package io.sugo.services.usergroup.parser;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.sugo.services.usergroup.model.FinalGroupBean;
import io.sugo.services.usergroup.model.TindexGroupBean;
import io.sugo.services.usergroup.model.UindexGroupBean;
import io.sugo.services.usergroup.model.UserGroupBean;

import java.util.Map;

/**
 * Created by chenyuzhi on 19-8-5.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl=DefaultParser.class)
@JsonSubTypes(value = {
		@JsonSubTypes.Type(name = "default", value = DefaultParser.class),
		@JsonSubTypes.Type(name = "fixed", value = FixedParser.class)
})
public interface Parser {
	public String getType();
	public Map<String, Object> parse(Map<String, Object> data, String groupByDim);
}
