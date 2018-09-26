package io.sugo.server.http.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.sugo.services.usergroup.UserGroupHelper;
import io.sugo.services.usergroup.model.UserGroupBean;
import io.sugo.services.usergroup.model.UserGroupQuery;
import io.sugo.common.guice.annotations.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.parquet.Strings;

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
			UserGroupBean userGroupBean) {
		try {
			checkParam(userGroupBean);
			String brokerUrl = userGroupBean.getBrokerUrl();
			checkUrl(brokerUrl);
			UserGroupQuery userGroupQuery = userGroupBean.getQuery();

			List<Map> result;
			if(userGroupBean.isAppend()){
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
			List<UserGroupBean> userGroupList) {
		try {
			checkParam(userGroupList);
			Map<String, List<UserGroupBean>> paramMap = parseMultiUserGroupParam(userGroupList);
			List<Map> result =  userGroupHelper.doMultiUserGroupOperation(paramMap);
			return Response.ok(result == null ? Collections.emptyList(): result).build();
		} catch (Throwable e) {
			log.error("Resource handle multiUserGroup error!",e);
			return Response.serverError().entity(Collections.
					singletonList(ImmutableMap.of("error", e.getMessage())))
					.build();
		}
	}

	public Map<String, List<UserGroupBean>> parseMultiUserGroupParam(List<UserGroupBean> userGroupList) throws IOException {
		Map<String, List<UserGroupBean>> paramMap = new HashMap<>(2);
		for(UserGroupBean userGroupBean : userGroupList){
			String type =  userGroupBean.getType();
			if(Strings.isNullOrEmpty(type)){
				continue;
			}
			if(UserGroupBean.INDEX_TYPES.contains(type)){
				checkUrl(userGroupBean.getBrokerUrl());
			}

			if (type.equals("finalGroup")){
				paramMap.put("finalGroup", ImmutableList.of(userGroupBean));
			}else {
				paramMap.computeIfAbsent("AssistantGroupList", (key -> Lists.newArrayList())).add(userGroupBean);
			}
		}
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

}
