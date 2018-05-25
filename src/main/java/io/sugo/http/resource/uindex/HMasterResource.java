package io.sugo.http.resource.uindex;

import com.google.common.collect.Maps;
import io.sugo.http.resource.ForwardResource;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created by chenyuzhi on 18-5-22.
 */
@Path("/druid/hmaster/v1")
public class HMasterResource extends HMasterForwardResource{
	private static final Logger LOG = Logger.getLogger(HMasterResource.class);
	public HMasterResource() {
		pathPre = "http://" + ip + "/druid/hmaster/v1";
	}

	@GET
	@Path("/loadstatus")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLoadStatus(
			@QueryParam("simple") String simple,
			@QueryParam("full") String full
	){
		String url = String.format("%s/loadstatus",pathPre);
		Map<String,Object> queryParams = Maps.newHashMap();

		if(full != null){
			queryParams.put("full",full);
		}
		if(simple != null){
			queryParams.put("simple",simple);
		}

		return  httpMethod.get(url,queryParams);
	}

	@GET
	@Path("/loadqueue")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLoadQueue(
			@QueryParam("simple") String simple,
			@QueryParam("full") String full
	){
		String url = String.format("%s/loadqueue",pathPre);
		Map<String,Object> queryParams = Maps.newHashMap();

		if(full != null){
			queryParams.put("full",full);
		}
		if(simple != null){
			queryParams.put("simple",simple);
		}

		return  httpMethod.get(url,queryParams);
	}
}
