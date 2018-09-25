package io.sugo.server.http.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.sugo.services.usergroup.UserGroupHelper;
import io.sugo.services.usergroup.model.UserGroupQuery;
import io.sugo.common.guice.annotations.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

@Path("/ant/usergroup/")
public class UserGroupResource {
	private static final Logger log = LogManager.getLogger(UserGroupResource.class);
	private UserGroupHelper userGroupHelper;
	private final ObjectMapper jsonMapper;

	@Inject
	public UserGroupResource(@Json ObjectMapper jsonMapper, UserGroupHelper userGroupHelper) {
		this.userGroupHelper = userGroupHelper;
		this.jsonMapper = jsonMapper;
	}

	@POST
	@Path("/single")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public Response handleSingleUserGroup(
			Map<String, Object> singleUserGroupParam) {
		try {
			checkParam(singleUserGroupParam);
			String brokerUrl = (String) singleUserGroupParam.get("brokerUrl");
			Boolean append = (Boolean) singleUserGroupParam.get("append");
			Map userGroupQueryMap = (Map)singleUserGroupParam.get("query");
			UserGroupQuery userGroupQuery = jsonMapper.readValue(
					jsonMapper.writeValueAsString(userGroupQueryMap), UserGroupQuery.class);
			checkUrl(brokerUrl);
			checkQuery(userGroupQuery);
			List<Map> result;
			if(append){
				result = userGroupHelper.doUserGroupQueryIncremental(userGroupQuery, brokerUrl);
			}else {
				result = userGroupHelper.getUserGroupQueryResult(userGroupQuery, brokerUrl);
			}
			return Response.ok(result == null ? Collections.emptyList(): result).build();
		} catch (Throwable e) {
			log.error("Resource handle singleUserGroup error!",e);
			return Response.serverError().entity(Collections.
					singletonList(ImmutableMap.of("error", e.getMessage())))
					.build();
		}
	}

	@POST
	@Path("/multi")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public Response handleMultiUserGroup(
			List<Map<String, Object>> userGroupList) {
		try {
			checkParam(userGroupList);
			Map<String, Object> paramMap = parseMultiUserGroupParam(userGroupList);
			List<Map> result =  userGroupHelper.doMultiUserGroupOperation(paramMap);
			return Response.ok(result == null ? Collections.emptyList(): result).build();
		} catch (Throwable e) {
			log.error("Resource handle multiUserGroup error!",e);
			return Response.serverError().entity(Collections.
					singletonList(ImmutableMap.of("error", e.getMessage())))
					.build();
		}
	}

	public Map<String, Object> parseMultiUserGroupParam(List<Map<String, Object>> userGroupList) throws IOException {
		Map<String, Object> paramMap = new HashMap<>(2);
		List<Map<String, Object>> realUserGroupList = new ArrayList<>();
		for(Map<String, Object> map : userGroupList){
			String type = (String) map.get("type");
			if(type == null || type.isEmpty()){
				continue;
			}
			if(type.equals("tindex")|| type.equals("uindex")){
				String brokerUrl = (String) map.get("brokerUrl");
				checkUrl(brokerUrl);
			}

			Map queryMap = (Map) map.get("query");
			Preconditions.checkNotNull(queryMap, "query field can not be null.");
			UserGroupQuery query = null;
			try {
				query = jsonMapper.readValue(jsonMapper.writeValueAsString(queryMap), UserGroupQuery.class);
			}catch (Exception e){
				log.error("MultiUserGroup param can not  deserialize instance of UserGroupQuery", e);
				Throwables.propagate(e);
			}
			checkQuery(query);
			map.put("query", query);

			if (type.equals("finalGroup")){
				paramMap.put("finalGroup", map);
			}else {
				realUserGroupList.add(map);
			}
		}
		paramMap.put("userGroupList", realUserGroupList);
		return paramMap;
	}

	private void checkParam(Object param){
		try {
			log.info(String.format("UserGroupResource original param: %s", jsonMapper.writeValueAsString(param)));
		}catch (Exception e){
		}
	}

 	private void checkUrl(String url){
		Preconditions.checkNotNull(url, "Url can not be null.");
	}

	private void checkQuery(UserGroupQuery userGroupQuery) {
		Preconditions.checkNotNull(userGroupQuery.getDataConfig(), "Data config can not be null.");
	}

}
