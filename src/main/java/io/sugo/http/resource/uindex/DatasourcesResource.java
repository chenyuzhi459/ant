package io.sugo.http.resource.uindex;

import com.google.common.collect.Maps;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;


/**
 * Created by chenyuzhi on 18-5-23.
 */
@Path("/druid/hmaster/v1/datasources")
public class DatasourcesResource extends HMasterForwardResource {
	public DatasourcesResource() {
		pathPre = "http://" + ip + "/druid/hmaster/v1/datasources";
	}
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQueryableDataSources(
			@QueryParam("full") String full,
			@QueryParam("simple") String simple
	){
		Map<String,Object> queryParams = Maps.newHashMap();

		if(full != null){
			queryParams.put("full",full);
		}
		if(simple != null){
			queryParams.put("simple",simple);
		}
		return  httpMethod.get(pathPre,queryParams);
	}

	@GET
	@Path("/spec/{dataSourceName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDataSourceSpec(
			@PathParam("dataSourceName") String dataSourceName
	){
		String url = String.format("%s/spec/%s",pathPre,dataSourceName);
		return httpMethod.get(url);
	}

	//TODO
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createDatasource(String spec) {
		String url = String.format("%s", pathPre);
		return httpMethod.post(url,spec);
	}

	@GET
	@Path("/serverview")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllDataSourceServers()
	{
		String url = String.format("%s/serverview",pathPre);
		return httpMethod.get(url);
	}

	@GET
	@Path("/serverview/{dataSourceName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDataSourceServers(
			@PathParam("dataSourceName") String dataSourceName
	)
	{
		String url = String.format("%s/serverview/%s",pathPre,dataSourceName);
		return httpMethod.get(url);
	}

	@GET
	@Path("/serverview/alternative")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllAlternativeDataSourceServers()
	{
		String url = String.format("%s/serverview/alternative",pathPre);
		return httpMethod.get(url);
	}

	@GET
	@Path("/serverview/alternative/{dataSourceName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAlternativeDataSourceServers(
			@PathParam("dataSourceName") String dataSourceName
	)
	{
		String url = String.format("%s/serverview/alternative/%s",pathPre,dataSourceName);
		return httpMethod.get(url);
	}

	@GET
	@Path("/segments/{dataSourceName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDSSegmentServers(
			@PathParam("dataSourceName") String dataSourceName
	){
		String url = String.format("%s/segments/%s",pathPre,dataSourceName);
		return httpMethod.get(url);
	}

	@GET
	@Path("/serverview/readonly/{dataSourceName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDataSourceReadOnlyServers(
			@PathParam("dataSourceName") String dataSourceName
	){
		String url = String.format("%s/serverview/readonly/%s",pathPre,dataSourceName);
		return httpMethod.get(url);
	}
	@GET
	@Path("/serverview/writable/{dataSourceName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDataSourceWritableServers(
			@PathParam("dataSourceName") String dataSourceName
	){
		String url = String.format("%s/serverview/writable/%s",pathPre,dataSourceName);
		return httpMethod.get(url);
	}

	@GET
	@Path("/serverview/alternative/readonly/{dataSourceName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAlternativeDataSourceReadOnlyServers(
			@PathParam("dataSourceName") String dataSourceName
	){
		String url = String.format("%s/serverview/alternative/readonly/%s",pathPre,dataSourceName);
		return httpMethod.get(url);
	}
	@GET
	@Path("/serverview/alternative/writable/{dataSourceName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAlternativeDataSourceWritableServers(
			@PathParam("dataSourceName") String dataSourceName
	){
		String url = String.format("%s/serverview/alternative/writable/%s",pathPre,dataSourceName);
		return httpMethod.get(url);
	}

	@DELETE
	@Path("/{dataSourceName}")
	public Response deleteDatasource(
			@PathParam("dataSourceName") String dataSourceName
	)
	{
		String url = String.format("%s/%s",pathPre,dataSourceName);
		return httpMethod.delete(url);
	}

	@DELETE
	@Path("/force/{dataSourceName}")
	public Response deleteDatasourceCompletely(
			@PathParam("dataSourceName") String dataSourceName
	)
	{
		String url = String.format("%s/force/%s",pathPre,dataSourceName);
		return httpMethod.delete(url);
	}

	@GET
	@Path("/dimensions/{dataSourceName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDimensions(
			@PathParam("dataSourceName") String dataSourceName
	) {
		String url = String.format("%s/dimensions/%s",pathPre,dataSourceName);
		return httpMethod.get(url);
	}


	@POST
	@Path("/dimensions/add/{dataSourceName}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addDimensions(@PathParam("dataSourceName") String dataSourceName,
								  String dimensions
	) {
		String url = String.format("%s/dimensions/add/%s",pathPre,dataSourceName);
		return httpMethod.post(url,dimensions);
	}



	@POST
	@Path("/dimensions/delete/{dataSourceName}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteDimensions(@PathParam("dataSourceName") String dataSourceName,
									 String dimensions
	) {
		String url = String.format("%s/dimensions/delete/%s",pathPre,dataSourceName);
		return httpMethod.post(url,dimensions);
	}
}
