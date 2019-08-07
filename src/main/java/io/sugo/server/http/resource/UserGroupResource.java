package io.sugo.server.http.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.sugo.common.utils.StringUtil;
import io.sugo.services.exception.RemoteException;
import io.sugo.services.usergroup.UserGroupHelper;
import io.sugo.services.usergroup.model.GroupBean;
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

	@Inject
	public UserGroupResource(UserGroupHelper userGroupHelper) {
		this.userGroupHelper = userGroupHelper;
	}


	@POST
	@Path("/multi")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public Response handleMultiUserGroup(List<GroupBean> userGroupList) {
		Response.ResponseBuilder resBuilder;
		try {
			Map<String, List<GroupBean>> paramMap = parseMultiUserGroupParam(userGroupList);
			List<Map> result =  userGroupHelper.doMultiUserGroupOperation(paramMap);
			resBuilder = Response.ok(result);
		} catch (Throwable e) {
			boolean isRmException = e instanceof RemoteException;
			String errMsg = String.format("Resource handle multiUserGroup occurs %s, param:%s",
					isRmException ? "remote exception" : "error", StringUtil.toJson(userGroupList));
			log.error(errMsg, e);

			Object originalInfo = isRmException ? ((RemoteException) e).getRemoteMessage() : e.getMessage();
			resBuilder = Response.serverError().entity(Collections.singletonList(ImmutableMap.of("error", originalInfo)));
		}

		return resBuilder.build();
	}

	@POST
	@Path("/checkMutex")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public Response checkMutexUserGroup(List<GroupBean> userGroupList) {
		Response.ResponseBuilder resBuilder;
		try {
			Map<String, List<GroupBean>> paramMap = parseMultiUserGroupParam(userGroupList);
			List<Map> result =  userGroupHelper.checkMutex(paramMap);
			resBuilder = Response.ok(result);
		} catch (Throwable e) {
			boolean isRmException = e instanceof RemoteException;
			String errMsg = String.format("Resource handle checkMutex occurs %s, param:%s",
					isRmException ? "remote exception" : "error", StringUtil.toJson(userGroupList));
			log.error(errMsg, e);

			Object originalInfo = isRmException ? ((RemoteException) e).getRemoteMessage() : e.getMessage();
			resBuilder = Response.serverError().entity(Collections.singletonList(ImmutableMap.of("error", originalInfo)));
		}

		return resBuilder.build();
	}


	@POST
	@Path("/multi/v2")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	//此接口除了具有"/multi"的功能外, 还添加了tindex结果输出到uindex的功能
	public Response handleMultiOperation(OperationItem operationItem) {
		Response.ResponseBuilder resBuilder;
		try {
			Map<String, List<GroupBean>> paramMap = parseMultiUserGroupParam(operationItem.getGroups());
			List<Map> result =  userGroupHelper.doMultiUserGroupOperationV2(paramMap);
			resBuilder = Response.ok(result);
		} catch (Throwable e) {
			boolean isRmException = e instanceof RemoteException;
			String errMsg = String.format("Resource handle multiUserGroup occurs %s, param:%s",
					isRmException ? "remote exception" : "error", StringUtil.toJson(operationItem));
			log.error(errMsg, e);

			Object originalInfo = isRmException ? ((RemoteException) e).getRemoteMessage() : e.getMessage();
			resBuilder = Response.serverError().entity(Collections.singletonList(ImmutableMap.of("error", originalInfo)));
		}

		return resBuilder.build();
	}


	public static class OperationItem{
		String id;
		String containerKey;
		List<GroupBean> groups;

		@JsonCreator
		public OperationItem(
				@JsonProperty("id") String id,
				@JsonProperty("containerKey") String containerKey,
				@JsonProperty("groups") List<GroupBean> groups) {
			this.id = id;
			this.containerKey = containerKey;
			this.groups = groups;
		}

		@JsonProperty
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public String getId() {
			return id;
		}

		@JsonProperty
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public String getContainerKey() {
			return containerKey;
		}

		@JsonProperty
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public List<GroupBean> getGroups() {
			return groups;
		}
	}



	public Map<String, List<GroupBean>> parseMultiUserGroupParam(List<GroupBean> userGroupList) throws IOException {
		Map<String, List<GroupBean>> paramMap = new HashMap<>(2);
		for(GroupBean userGroupBean : userGroupList){
			String type =  userGroupBean.getType();
			if(Strings.isNullOrEmpty(type)){
				continue;
			}

			if (type.equals("finalGroup")){
				paramMap.put("finalGroup", ImmutableList.of(userGroupBean));
			}else {
				paramMap.computeIfAbsent("AssistantGroupList", (key -> Lists.newArrayList())).add(userGroupBean);
			}
		}
		return paramMap;
	}


}
