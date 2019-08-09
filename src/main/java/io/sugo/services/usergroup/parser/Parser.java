package io.sugo.services.usergroup.parser;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Map;

/**
 * Created by chenyuzhi on 19-8-5.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl=MappingParser.class)
@JsonSubTypes(value = {
		@JsonSubTypes.Type(name = MappingParser.TYPE, value = MappingParser.class),
		@JsonSubTypes.Type(name = FixedParser.TYPE, value = FixedParser.class)
})
public interface Parser {
	public String getType();
	public String getName();
	public Object getParsedVal(Map<String,Object> data);
}
