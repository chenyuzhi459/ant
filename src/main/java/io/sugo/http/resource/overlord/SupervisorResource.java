package io.sugo.http.resource.overlord;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.sugo.http.resource.overlord.condition.SupervisorSearchCondition;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

@Path("/druid/indexer/v1/supervisor")
public class SupervisorResource extends OverlordForwardResource {

    public SupervisorResource() throws IOException {
        pathPre = "http://" + ip + "/druid/indexer/v1/supervisor";
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response specPost(final String spec)
    {
        String url = String.format("%s", pathPre);
        return httpMethod.post(url,spec);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response specGetAll(
            @QueryParam("full") String full,
            @QueryParam("simple") String simple
    )
    {
        Map<String,Object> queryParams = Maps.newHashMap();
        if(null != full){
            queryParams.put("full",full);
        }
        if(null != simple){
            queryParams.put("simple",simple);
        }
        String url = String.format("%s", pathPre);
        return httpMethod.get(url,queryParams);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response specGet(@PathParam("id") final String id)
    {
        String url = String.format("%s/%s", pathPre , id);
        return httpMethod.get(url);
    }

    @GET
    @Path("/{id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response specGetStatus(@PathParam("id") final String id)
    {
        String url = String.format("%s/%s/status", pathPre , id);
        return httpMethod.get(url);
    }

    @POST
    @Path("/{id}/shutdown")
    @Produces(MediaType.APPLICATION_JSON)
    public Response shutdown(@PathParam("id") final String id)
    {
        String url = String.format("%s/%s/shutdown", pathPre , id);
        return httpMethod.post(url);
    }

    @GET
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response specGetAllHistory()
    {
        String url = String.format("%s/history", pathPre);
        return httpMethod.get(url);
    }


    @POST
    @Path("/history/part")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSupervisorHistoryPart(SupervisorSearchCondition supervisorSearchCondition){
        if(null == supervisorSearchCondition){
            supervisorSearchCondition = new SupervisorSearchCondition();
        }
        if(null == supervisorSearchCondition.getSupervisorSortItem()){
            supervisorSearchCondition.setSupervisorSortItem(
                    ImmutableMap.of("sortDimension","created_date", "sortDirection","DESC")
            );
        }
        if(null == supervisorSearchCondition.getSupervisorPageItem()){
            supervisorSearchCondition.setSupervisorPageItem(
                    ImmutableMap.of("size",10,"offset",0)
            );
        }
        String url = String.format("%s/history/part", pathPre);

        return httpMethod.postWithObjectParam(url,supervisorSearchCondition);
    }

    @POST
    @Path("/history/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSupervisorHistoryNum(
            SupervisorSearchCondition supervisorSearchCondition
    )
    {
        if(null == supervisorSearchCondition){
            supervisorSearchCondition = new SupervisorSearchCondition();
        }
        String url = String.format("%s/history/count", pathPre);
        return httpMethod.postWithObjectParam(url,supervisorSearchCondition);
    }

    @GET
    @Path("/{id}/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response specGetHistory(@PathParam("id") final String id)
    {
        String url = String.format("%s/%s/history", pathPre , id);
        return httpMethod.get(url);
    }

    @POST
    @Path("/{id}/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reset(@PathParam("id") final String id)
    {
        String url = String.format("%s/%s/reset", pathPre , id);
        return httpMethod.post(url);
    }

    @POST
    @Path("/topic/{id}/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteTopic(@PathParam("id") final String id)
    {
        String url = String.format("%s/topic/%s/delete", pathPre , id);
        return httpMethod.post(url);
    }
}

