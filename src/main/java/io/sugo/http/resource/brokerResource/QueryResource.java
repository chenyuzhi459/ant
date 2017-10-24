package io.sugo.http.resource.brokerResource;

import com.fasterxml.jackson.jaxrs.smile.SmileMediaTypes;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import io.sugo.http.resource.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;


//@Path("/druid/v2/")
public class QueryResource extends Resource {


    public QueryResource() throws IOException {
        ip = configure.getProperty("druid.properties","broker.ip");
        pathPre = "http://" + ip + "/druid/broker/v1";
    }

    @GET
    @Path("/queryCount")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQueryCount(@Context final HttpServletRequest req) {
        String url = String.format("%s/queryCount", pathPre);
        return httpMethod.get(url);
    }

    @GET
    @Path("/queue")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllQueries(@Context final HttpServletRequest req) {
        String url = String.format("%s/queue", pathPre);
        return httpMethod.get(url);
    }

    @GET
    @Path("/queue/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQuery(@PathParam("id") String queryId, @Context final HttpServletRequest req) {
        String url = String.format("%s/queue/%s", pathPre, queryId);
        return httpMethod.get(url);
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelQuery(@PathParam("id") String queryId, @Context final HttpServletRequest req)
    {
        String url = String.format("%s/%s", pathPre, queryId);
        return httpMethod.delete(url);
    }

    //not finish
    @POST
    @Produces({MediaType.APPLICATION_JSON, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Consumes({MediaType.APPLICATION_JSON, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    public Response doPost(
            final String in,
            @QueryParam("pretty") String pretty,
            @Context final HttpServletRequest req // used to get request content-type, remote address and AuthorizationInfo
    ) throws IOException
    {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        if(pretty != null){
            queryParams.add("pretty",pretty);
        }
        String url = String.format("%s", pathPre);
        return httpMethod.post(url, in, queryParams);
    }

}

