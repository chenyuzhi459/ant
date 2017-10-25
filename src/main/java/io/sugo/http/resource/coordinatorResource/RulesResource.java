package io.sugo.http.resource.coordinatorResource;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import io.sugo.http.audit.AuditManager;
import io.sugo.http.resource.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;


@Path("/druid/coordinator/v1/rules")
public class RulesResource extends Resource {


    public RulesResource() throws IOException {
        ip = configure.getProperty("druid.properties","coordinator.ip");
        pathPre = "http://" + ip + "/druid/coordinator/v1/rules";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRules()
    {
        String url = String.format("%s", pathPre);
        return httpMethod.get(url);
    }

    @GET
    @Path("/{dataSourceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatasourceRules(
            @PathParam("dataSourceName") final String dataSourceName,
            @QueryParam("full") final String full
    )
    {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        if(full != null){
            queryParams.add("full",full);
        }
        String url = String.format("%s/%s", pathPre, dataSourceName);
        return httpMethod.get(url,queryParams);
    }

    @POST
    @Path("/{dataSourceName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setDatasourceRules(
            @PathParam("dataSourceName") final String dataSourceName,
            final String rules,
            @HeaderParam(AuditManager.X_DRUID_AUTHOR) @DefaultValue("") final String author,
            @HeaderParam(AuditManager.X_DRUID_COMMENT) @DefaultValue("") final String comment,
            @Context HttpServletRequest req
    )
    {
        String url = String.format("%s/%s", pathPre, dataSourceName);
        return httpMethod.post(url,rules);
    }


    @GET
    @Path("/{dataSourceName}/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatasourceRuleHistory(
            @PathParam("dataSourceName") final String dataSourceName,
            @QueryParam("interval") final String interval,
            @QueryParam("count") final Integer count
    )
    {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        if(interval != null){
            queryParams.add("full",interval);
        }
        if(count != null) {
            queryParams.add("count",count);
        }
        String url = String.format("%s/%s/history", pathPre, dataSourceName);
        return httpMethod.get(url,queryParams);
    }

    @GET
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatasourceRuleHistory(
            @QueryParam("interval") final String interval,
            @QueryParam("count") final Integer count
    )
    {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        if(interval != null){
            queryParams.add("full",interval);
        }
        if(count != null) {
            queryParams.add("count",count);
        }
        String url = String.format("%s/history", pathPre);
        return httpMethod.get(url,queryParams);
    }
}
