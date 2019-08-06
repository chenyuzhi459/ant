package io.sugo.server.http.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.sugo.common.utils.StringUtil;
import io.sugo.services.exception.RemoteException;
import io.sugo.services.tag.DataUpdateHelper;
import io.sugo.services.tag.model.QueryUpdateBean;
import io.sugo.services.tag.model.UserGroupUpdateBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created by chenyuzhi on 18-9-25.
 */
@Path("/ant/tag/")
public class TagResource {
	private static final Logger log = LogManager.getLogger(TagResource.class);
	private final DataUpdateHelper dataUpdateHelper;
	private final ObjectMapper jsonMapper;

	@Inject
	public TagResource(DataUpdateHelper dataUpdateHelper, ObjectMapper jsonMapper) {
		this.dataUpdateHelper = dataUpdateHelper;
		this.jsonMapper = jsonMapper;
	}

	@POST
	@Path("/usergroup/batchUpdate")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public Response updateUserGroupData(UserGroupUpdateBean userGroupUpdateBean) {
		Response.ResponseBuilder resBuilder;
		try {
			check(userGroupUpdateBean);
			Map<String, Object> result = this.dataUpdateHelper.updateUserGroup(userGroupUpdateBean);
			resBuilder = Response.ok(result);
		} catch (Throwable e) {
			boolean isRmException = e instanceof RemoteException;
			String errMsg = String.format("UpdateUserGroupData occurs %s, param:%s",
					isRmException ? "remote exception" : "error", StringUtil.toJson(userGroupUpdateBean));
			log.error(errMsg, e);

			Object originalInfo = isRmException ? ((RemoteException) e).getRemoteMessage() : e.getMessage();
			resBuilder = Response.serverError().entity(ImmutableMap.of("error", originalInfo));
		}

		return resBuilder.build();
	}


	private void check(UserGroupUpdateBean userGroupUpdateBean) {
		Preconditions.checkNotNull(userGroupUpdateBean.getHproxy(), "hproxy can not be null.");
		Preconditions.checkNotNull(userGroupUpdateBean.getDataSource(), "dataSource can not be null.");
		Preconditions.checkNotNull(userGroupUpdateBean.getPrimaryColumn(), "primaryColumn can not be null.");
		Preconditions.checkNotNull(userGroupUpdateBean.getUserGroupConfig(), "userGroupConfig can not be null.");
		Preconditions.checkNotNull(userGroupUpdateBean.getDimData(), "dimData can not be null.");
	}
}
