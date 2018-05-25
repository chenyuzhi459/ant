package io.sugo.http.resource.uindex;

import io.sugo.http.resource.ForwardResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by chenyuzhi on 18-5-25.
 */
@Path("uindex/druid/v2")
public class UindexQueryResource extends ForwardResource{
	public UindexQueryResource() {
		ip = getIp("uindex.properties","broker");
		pathPre = "http://" + ip + "/druid/v2";
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doQuery(String spec) {
		String url = String.format("%s", pathPre);
		return httpMethod.post(url,spec);
	}
}
