package io.sugo.server.http.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.sugo.common.utils.ParamChecker;
import io.sugo.common.utils.StringUtil;
import io.sugo.common.utils.UserGroupUtil;
import io.sugo.services.exception.RemoteException;
import io.sugo.services.usergroup.UserGroupHelper;
import io.sugo.services.usergroup.OperationResult;
import io.sugo.services.usergroup.model.GroupBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
			Map<String, List<GroupBean>> paramMap = UserGroupUtil.parseMultiUserGroupParam(userGroupList);
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
			Map<String, List<GroupBean>> paramMap = UserGroupUtil.parseMultiUserGroupParam(userGroupList);
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
	public Response handleMultiOperation(MutliOperationBody body) {
		Response.ResponseBuilder resBuilder;
		try {
//			Map<String, List<GroupBean>> paramMap = parseMultiUserGroupParam(body.getGroups());
//			List<Map> result =  userGroupHelper.doMultiUserGroupOperationV2(paramMap);
			OperationResult result = userGroupHelper.addOperation(body);

			resBuilder = Response.ok(ImmutableMap.of("result",result));
		} catch (Throwable e) {
			boolean isRmException = e instanceof RemoteException;
			String errMsg = String.format("Resource handle multiUserGroup occurs %s, param:%s",
					isRmException ? "remote exception" : "error", StringUtil.toJson(body));
			log.error(errMsg, e);

			Object originalInfo = isRmException ? ((RemoteException) e).getRemoteMessage() : e.getMessage();
			resBuilder = Response.serverError().entity(Collections.singletonList(ImmutableMap.of("error", originalInfo == null ? "null" : originalInfo)));
		}

		return resBuilder.build();
	}

	@GET
	@Path("/multi/result")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	//此接口除了具有"/multi"的功能外, 还添加了tindex结果输出到uindex的功能
	public Response handleGetMultiOperationRes(@QueryParam("ids") String ids) {
		Response.ResponseBuilder resBuilder;
		try {
//			Map<String, List<GroupBean>> paramMap = parseMultiUserGroupParam(body.getGroups());
//			List<Map> result =  userGroupHelper.doMultiUserGroupOperationV2(paramMap);
			log.info("ids===>" + ids);
			String[] idArr = ids.split(",");

			resBuilder = Response.ok(ImmutableMap.of("result",userGroupHelper.fetchOperationResultByIds(idArr)));
		} catch (Throwable e) {
//			boolean isRmException = e instanceof RemoteException;
//			String errMsg = String.format("Resource handle multiUserGroup occurs %s, param:%s",
//					isRmException ? "remote exception" : "error", StringUtil.toJson(body));
			log.error(e.getMessage(), e);

//			Object originalInfo = isRmException ? ((RemoteException) e).getRemoteMessage() : e.getMessage();
			resBuilder = Response.serverError().entity(Collections.singletonList(ImmutableMap.of("error", e.getMessage())));
		}

		return resBuilder.build();
	}


	public static class MutliOperationBody {
		String id;
		List<GroupBean> groups;

		@JsonCreator
		public MutliOperationBody(
				@JsonProperty("id") String id,
				@JsonProperty("groups") List<GroupBean> groups) {
			ParamChecker.checkEmpty(id,id);
			ParamChecker.checkEmpty(groups,id);
			this.id = id;
			this.groups = groups;
		}

		@JsonProperty
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public String getId() {
			return id;
		}

		@JsonProperty
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public List<GroupBean> getGroups() {
			return groups;
		}
	}

}
