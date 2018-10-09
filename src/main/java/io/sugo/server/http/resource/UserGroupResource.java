package io.sugo.server.http.resource;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.sugo.common.utils.StringUtil;
import io.sugo.services.exception.RemoteException;
import io.sugo.services.usergroup.UserGroupHelper;
import io.sugo.services.usergroup.model.UserGroupBean;
import io.sugo.services.usergroup.model.UserGroupQuery;
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
	@Path("/single")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public Response handleSingleUserGroup(UserGroupBean userGroupBean) {
		Response.ResponseBuilder resBuilder;
		try {
			check(userGroupBean, true);
			String broker = userGroupBean.getBroker();
			UserGroupQuery userGroupQuery = userGroupBean.getQuery();

			List<Map> result = userGroupBean.isAppend() ?
					userGroupHelper.doUserGroupQueryIncremental(userGroupQuery, broker) :
					userGroupHelper.getUserGroupQueryResult(userGroupQuery, broker);
			resBuilder = Response.ok(result);
		} catch (Throwable e) {
			boolean isRmException = e instanceof RemoteException;
			String errMsg = String.format("Resource handle singleUserGroup occurs %s, param:%s",
					isRmException ? "remote exception" : "error", StringUtil.toJson(userGroupBean));
			log.error(errMsg, e);

			Object originalInfo = isRmException ? ((RemoteException) e).getRemoteMessage() : e.getMessage();
			resBuilder = Response.serverError().entity(Collections.singletonList(ImmutableMap.of("error", originalInfo)));
		}

		return resBuilder.build();
	}

	@POST
	@Path("/multi")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public Response handleMultiUserGroup(List<UserGroupBean> userGroupList) {
		Response.ResponseBuilder resBuilder;
		try {
			Map<String, List<UserGroupBean>> paramMap = parseMultiUserGroupParam(userGroupList);
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

	public Map<String, List<UserGroupBean>> parseMultiUserGroupParam(List<UserGroupBean> userGroupList) throws IOException {
		Map<String, List<UserGroupBean>> paramMap = new HashMap<>(2);
		for(UserGroupBean userGroupBean : userGroupList){
			String type =  userGroupBean.getType();
			if(Strings.isNullOrEmpty(type)){
				continue;
			}

			check(userGroupBean, UserGroupBean.INDEX_TYPES.contains(type));

			if (type.equals("finalGroup")){
				paramMap.put("finalGroup", ImmutableList.of(userGroupBean));
			}else {
				paramMap.computeIfAbsent("AssistantGroupList", (key -> Lists.newArrayList())).add(userGroupBean);
			}
		}
		return paramMap;
	}

	private void check(UserGroupBean userGroupBean, boolean checkBroker){
		Preconditions.checkNotNull(userGroupBean.getQuery(), "query can not be null.");
		Preconditions.checkNotNull(userGroupBean.getQuery().getDataConfig(), "dataConfig can not be null.");
		if(checkBroker){
			Preconditions.checkNotNull(userGroupBean.getBroker(), "broker can not be null.");
		}
	}

}
