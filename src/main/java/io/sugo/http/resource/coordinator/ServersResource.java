package io.sugo.http.resource.coordinator;

import com.google.common.collect.Maps;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

@Path("/druid/coordinator/v1/servers")
public class ServersResource extends CoordinatorForwardResource {

    public ServersResource() throws IOException {
        pathPre = "http://" + ip + "/druid/coordinator/v1/servers";
    }

    @GET
    @Path("/datasources/{dataSourceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTheDataSourceServers(
            @PathParam("dataSourceName") final String dataSourceName
    )
    {
        String url = String.format("%s/datasources/%s", pathPre, dataSourceName);
        return httpMethod.get(url);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClusterServers(
            @QueryParam("full") String full,
            @QueryParam("simple") String simple
    )
    {
        Map<String,Object> queryParams = Maps.newHashMap();
        if(simple != null){
            queryParams.put("simple",simple);
        }
        if(full != null){
            queryParams.put("full",full);
        }
        String url = String.format("%s", pathPre);
        return httpMethod.get(url,queryParams);
    }

    @GET
    @Path("/{serverName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServer(
            @PathParam("serverName") String serverName,
            @QueryParam("simple") String simple
    )
    {
        Map<String,Object> queryParams = Maps.newHashMap();
        if(simple != null){
            queryParams.put("simple",simple);
        }
        String url = String.format("%s/%s", pathPre, serverName);
        return httpMethod.get(url,queryParams);
    }

    @GET
    @Path("/{serverName}/segments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServerSegments(
            @PathParam("serverName") String serverName,
            @QueryParam("full") String full
    )
    {
        Map<String,Object> queryParams = Maps.newHashMap();
        if(full != null){
            queryParams.put("full",full);
        }
        String url = String.format("%s/%s/segments", pathPre, serverName);
        return httpMethod.get(url,queryParams);
    }

    @GET
    @Path("/{serverName}/segments/sortAndSearch")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServerSegments(
            @PathParam("serverName") String serverName,
            @QueryParam("full") String full,
            @QueryParam("isDescending") @DefaultValue("false") boolean isDscending,
            @QueryParam("searchSegment") String searchSegment
    ){
        Map<String,Object> queryParams = Maps.newHashMap();
        if(full != null){
            queryParams.put("full",full);
        }
        if(searchSegment != null){
            queryParams.put("searchSegment",searchSegment);
        }
        queryParams.put("isDescending",isDscending);
        String url = String.format("%s/%s/segments/sortAndSearch", pathPre, serverName);
        return httpMethod.get(url,queryParams);
    }

    @GET
    @Path("/{serverName}/segments/{segmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServerSegment(
            @PathParam("serverName") String serverName,
            @PathParam("segmentId") String segmentId
    )
    {
        String url = String.format("%s/%s/segments/%s", pathPre, serverName, segmentId);
        return httpMethod.get(url);
    }
}
