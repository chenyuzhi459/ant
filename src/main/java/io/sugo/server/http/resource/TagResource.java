package io.sugo.server.http.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
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
	@Path("/batchUpdate")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public Response updateBatchData(DataBean dataBean) {
		try {
			log.info(String.format("TagResource original param: %s", this.jsonMapper.writeValueAsString(dataBean)));
			Map<String, Object> result = this.dataUpdateHelper.update(dataBean);
			return Response.ok(result).build();
		}catch (Exception e){
			log.error("UpdateBatchData error!",e);
			return Response.serverError().entity(ImmutableMap.of("error", e.getMessage()))
					.build();
		}

	}
}
