package io.sugo.services.usergroup.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by chenyuzhi on 18-9-26.
 */
public class UserGroupBean {
	public static final Set<String> INDEX_TYPES = new HashSet<>(Arrays.asList("tindex","uindex"));
	public static final String FINAL_GROUP_TYPE = "finalGroup";
	private  String type;
	private  String brokerUrl;
	private  UserGroupQuery query;
	private  String op;        // for AssistantGroup
	private  Boolean append;   // for finalGroup

	@JsonCreator
	public UserGroupBean(
			@JsonProperty("type") String type,
			@JsonProperty("brokerUrl") String brokerUrl,
			@JsonProperty(value = "query", required = true) UserGroupQuery query,
			@JsonProperty("op") String op,
			@JsonProperty("append") Boolean append) {

		this.type = type;
		this.brokerUrl = brokerUrl;
		this.query = query;

		if(this.type.equals(FINAL_GROUP_TYPE)){
			this.append = append == null ? false : append;
		}else {
			this.op = op == null ? "" : op;
		}

	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getType() {
		return type;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getBrokerUrl() {
		return brokerUrl;
	}

	@JsonProperty
	public UserGroupQuery getQuery() {
		return query;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getOp() {
		return op;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Boolean isAppend() {
		return append;
	}
}
