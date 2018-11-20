package io.sugo.server.http.resource;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.sugo.dgraph.bean.ChainQuery;
import io.sugo.dgraph.client.ClientProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by chenyuzhi on 18-11-16.
 */
@Path("/dgraph/query")
public class DgraphResource {
	private static final Logger log = LogManager.getLogger(DgraphResource.class);
	private final ClientProxy client;
	@Inject
	public DgraphResource(ClientProxy client) {
		this.client = client;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response query(ChainQuery query) {
		Response.ResponseBuilder builder ;
		try {
			String queryStr = query.getSQL();
			log.info(String.format("query====>\n%s", queryStr));
			builder = Response.ok(client.query(queryStr));
		} catch (Exception e){
			log.error("Dgraph query error!", e);
			builder = Response.serverError().entity(ImmutableMap.of("error", e.getMessage()));
		}

		return builder.build();
	}
}
