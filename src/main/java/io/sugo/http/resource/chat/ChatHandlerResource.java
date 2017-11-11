package io.sugo.http.resource.chat;

import io.sugo.http.resource.ForwardResource;
import io.sugo.http.util.HttpMethodProxy;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenyuzhi on 17-10-9.
 */
@Path("/druid/worker/v1/task")
public class ChatHandlerResource extends ForwardResource {
	protected static final Logger LOG = Logger.getLogger(ChatHandlerResource.class);
	public ChatHandlerResource(){
		pathPre = "http://{0}/druid/worker/v1";
	}



	@GET
	@Path("/chat/{id}/summary")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSummaryInfo(
			@PathParam("id") String handlerId,
			@QueryParam("location") String location
	)
	{
		String url = MessageFormat.format("{0}/chat/{1}/summary",MessageFormat.format(pathPre,location),handlerId);
		System.out.println("url:" + url);

		return httpMethod.get(url);
	}


}
