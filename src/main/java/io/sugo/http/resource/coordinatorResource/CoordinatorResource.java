package io.sugo.http.resource.coordinatorResource;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import io.sugo.http.resource.Resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/druid/coordinator/v1")
public class CoordinatorResource extends Resource {
    public CoordinatorResource() throws IOException {
        ip = configure.getProperty("druid.properties","coordinator.ip");
        pathPre = "http://" + ip + "/druid/coordinator/v1";
    }

    @GET
    @Path("/leader")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLeader() {
        String url = String.format("%s/leader", pathPre);
        return httpMethod.get(url);
    }

    @GET
    @Path("/loadstatus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLoadStatus(
            @QueryParam("simple") String simple,
            @QueryParam("full") String full
    )
    {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        if(simple != null){
            queryParams.add("simple",simple);
        }
        if(full != null){
            queryParams.add("full",full);
        }

        String url = String.format("%s/loadstatus", pathPre);
        return httpMethod.get(url,queryParams);
    }

    @GET
    @Path("/loadqueue")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLoadQueue(
            @QueryParam("simple") String simple,
            @QueryParam("full") String full
    )
    {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        if(simple != null){
            queryParams.add("simple",simple);
        }
        if(full != null){
            queryParams.add("full",full);
        }
        String url = String.format("%s/loadqueue", pathPre);
        return httpMethod.get(url,queryParams);
    }

}







