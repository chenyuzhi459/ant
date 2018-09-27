package io.sugo.server.http.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.sugo.common.utils.StringUtil;
import io.sugo.services.exception.RemoteException;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;
import io.sugo.services.tag.DataUpdateHelper;
import io.sugo.services.tag.model.DataBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
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
	public Response updateBatchData(DataBean dataBean) {
		Response.ResponseBuilder resBuilder;
		try {
			check(dataBean);
			Map<String, Object> result = this.dataUpdateHelper.update(dataBean);
			resBuilder = Response.ok(result);
		} catch (Throwable e) {
			boolean isRmException = e instanceof RemoteException;
			String errMsg = String.format("UpdateBatchData occurs %s, param:%s",
					isRmException ? "remote exception" : "error", StringUtil.toJson(dataBean));
			log.error(errMsg, e);

			Object originalInfo = isRmException ? ((RemoteException) e).getRemoteMessage() : e.getMessage();
			resBuilder = Response.serverError().entity(ImmutableMap.of("error", originalInfo));
		}

		return resBuilder.build();
	}

	private void check(DataBean dataBean) {
		Preconditions.checkNotNull(dataBean.getHproxyUrl(), "hproxyUrl can not be null.");
		Preconditions.checkNotNull(dataBean.getDataSource(), "dataSource can not be null.");
		Preconditions.checkNotNull(dataBean.getPrimaryColumn(), "primaryColumn can not be null.");
		Preconditions.checkNotNull(dataBean.getUserGroupConfig(), "userGroupConfig can not be null.");
		Preconditions.checkNotNull(dataBean.getDimData(), "dimData can not be null.");
	}
}
