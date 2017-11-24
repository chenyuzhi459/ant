package io.sugo.http.resource.coordinator;

import com.google.common.collect.Maps;
import io.sugo.http.resource.ForwardResource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;


@Path("/druid/coordinator/v1/metadata")
public class MetadataResource extends CoordinatorForwardResource {

    public MetadataResource() throws IOException {
        pathPre = "http://" + ip + "/druid/coordinator/v1/metadata";
    }

    @GET
    @Path("/datasources")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseDataSources(
            @QueryParam("full") String full,
            @QueryParam("includeDisabled") String includeDisabled
    )
    {
        Map<String,Object> queryParams = Maps.newHashMap();
        if(includeDisabled != null){
            queryParams.put("includeDisabled",includeDisabled);
        }
        if(full != null){
            queryParams.put("full",full);
        }
        String url = String.format("%s/datasources", pathPre);
        return httpMethod.get(url,queryParams);
    }

    @GET
    @Path("/datasources/sortAndSearch")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseDataSources(
            @QueryParam("full") String full,
            @QueryParam("simple") String simple,
            @QueryParam("includeDisabled") String includeDisabled,
            @QueryParam("sortDimension") String sortDimension,
            @QueryParam("isDescending") @DefaultValue("true") boolean isDescending,
            @QueryParam("searchDatasource") String searchDatasource,
            @Context final HttpServletRequest req
    ){
        String url = String.format("%s/datasources/sortAndSearch", pathPre);
        Map<String,Object> queryParams = Maps.newHashMap();
        if(full != null){
            queryParams.put("full",full);
        }
        if(simple != null){
            queryParams.put("simple",simple);
        }
        if(includeDisabled != null){
            queryParams.put("includeDisabled",includeDisabled);
        }
        if(sortDimension != null){
            queryParams.put("sortDimension",sortDimension);
        }
        if(searchDatasource != null){
            queryParams.put("searchDatasource",searchDatasource);
        }
        queryParams.put("isDescending",isDescending);
        return httpMethod.get(url,queryParams);
    }

    @GET
    @Path("/datasources/{dataSourceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseSegmentDataSource(
            @PathParam("dataSourceName") final String dataSourceName
    )
    {
        String url = String.format("%s/datasources/%s", pathPre,dataSourceName);
        return httpMethod.get(url);
    }

    @GET
    @Path("/datasources/{dataSourceName}/segments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseSegmentDataSourceSegments(
            @PathParam("dataSourceName") String dataSourceName,
            @QueryParam("searchSegment") String searchSegment,
            @QueryParam("sortDimension") @DefaultValue("id") String sortDimension,   //可取值:id,size
            @QueryParam("isDescending") @DefaultValue("false") boolean isDescending,
            @QueryParam("full") String full
    )
    {
        Map<String,Object> queryParams = Maps.newHashMap();
        if(full != null){
            queryParams.put("full",full);
        }
        if(searchSegment != null){
            queryParams.put("searchSegment",searchSegment);
        }
        if(sortDimension != null){
            queryParams.put("sortDimension",sortDimension);
        }
        queryParams.put("isDescending",isDescending);
        String url = String.format("%s/datasources/%s/segments", pathPre, dataSourceName);
        return httpMethod.get(url,queryParams);
    }

    @POST
    @Path("/datasources/{dataSourceName}/segments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseSegmentDataSourceSegments(
            @PathParam("dataSourceName") String dataSourceName,
            @QueryParam("searchSegment") String searchSegment,
            @QueryParam("isDescending") @DefaultValue("false") boolean isDescending,
            @QueryParam("full") String full,
            final String intervals
    )
    {
        Map<String,Object> queryParams = Maps.newHashMap();
        if(full != null){
            queryParams.put("full",full);
        }
        if(searchSegment != null){
            queryParams.put("searchSegment",searchSegment);
        }
        queryParams.put("isDescending",isDescending);
        String url = String.format("%s/datasources/%s/segments", pathPre, dataSourceName);
        return httpMethod.post(url, intervals, queryParams);
    }

    @GET
    @Path("/datasources/{dataSourceName}/segments/{segmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseSegmentDataSourceSegment(
            @PathParam("dataSourceName") String dataSourceName,
            @PathParam("segmentId") String segmentId
    )
    {
        String url = String.format("%s/datasources/%s/segments/%s", pathPre, dataSourceName, segmentId);
        return httpMethod.get(url);
    }

    @GET
    @Path("/datasources/{dataSourceName}/intervals")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseSegmentDataSourceIntervals(
            @PathParam("dataSourceName") String dataSourceName,
            @QueryParam("searchInterval") String searchInterval,
            @QueryParam("sortDimension") String sortDimension,
            @QueryParam("isDescending")  boolean isDescending,
            @QueryParam("simple") String simple,
            @QueryParam("full") String full
    )
    {
        Map<String,Object> queryParams = Maps.newHashMap();
        if(full != null){
            queryParams.put("full",full);
        }
        if(simple != null){
            queryParams.put("simple",simple);
        }
        if(searchInterval != null){
            queryParams.put("searchInterval",searchInterval);
        }
        if(sortDimension != null){
            queryParams.put("sortDimension",sortDimension);
        }
        queryParams.put("isDescending",isDescending);
        String url = String.format("%s/datasources/%s/intervals", pathPre, dataSourceName);
        return httpMethod.get(url, queryParams);
    }

    @DELETE
    @Path("/datasources/{dataSourceName}/segments/{segmentId}/disable")
    public Response disableDatasourceSegment(
            @PathParam("dataSourceName") String dataSourceName,
            @PathParam("segmentId") String segmentId
    )
    {
        String url = String.format("%s/datasources/%s/segments/%s/disable", pathPre, dataSourceName, segmentId);
        return httpMethod.delete(url);
    }

    @POST
    @Path("/datasources/{dataSourceName}/segments/{segmentId}/enable")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response enableDatasourceSegment(
        @PathParam("dataSourceName") String dataSourceName,
        @PathParam("segmentId") String segmentId
    )
    {
        String url = String.format("%s/datasources/%s/segments/%s/enable", pathPre, dataSourceName, segmentId);
        return httpMethod.post(url);
    }

    @DELETE
    @Path("/datasources/{dataSourceName}/segments/{segmentId}/delete")
    public Response deleteDatasourceSegment(
            @PathParam("dataSourceName") String dataSourceName,
            @PathParam("segmentId") String segmentId
    )
    {
        String url = String.format("%s/datasources/%s/segments/%s/delete", pathPre, dataSourceName, segmentId);
        return httpMethod.delete(url);
    }

    @GET
    @Path("/datasources/{dataSourceName}/disableSegments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDisableSegmentDataSourceSegments(
            @PathParam("dataSourceName") String dataSourceName,
            @QueryParam("searchSegment") String searchSegment,
            @QueryParam("isDescending") @DefaultValue("false") boolean isDescending
    ){
        Map<String,Object> queryParams = Maps.newHashMap();
        if(null != searchSegment){
            queryParams.put("searchSegment",searchSegment);
        }
        queryParams.put("isDescending",isDescending);
        String url = String.format("%s/datasources/%s/disableSegments", pathPre, dataSourceName);
        return httpMethod.get(url, queryParams);
    }

    @GET
    @Path("/disableDatasources")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDisableDatabaseDataSources(
            @QueryParam("searchDatasource") String searchDatasource,
            @QueryParam("isDescending") @DefaultValue("false") boolean isDescending
    )
    {
        Map<String,Object> queryParams = Maps.newHashMap();
        if(searchDatasource != null){
            queryParams.put("searchDatasource",searchDatasource);
        }
        queryParams.put("isDescending", isDescending);
        String url = String.format("%s/disableDatasources", pathPre);
        return httpMethod.get(url, queryParams);
    }

    @GET
    @Path("/datasources/{dataSourceName}/disableIntervals")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseSegmentDataSourceDisableIntervals(
            @PathParam("dataSourceName") String dataSourceName,
            @QueryParam("searchInterval") String searchInterval,
            @QueryParam("isDescending") @DefaultValue("true")  boolean isDescending
    )
    {
        Map<String,Object> queryParams = Maps.newHashMap();
        if(searchInterval != null){
            queryParams.put("searchInterval",searchInterval);
        }
        queryParams.put("isDescending", isDescending);
        String url = String.format("%s/datasources/%s/disableIntervals", pathPre, dataSourceName);
        return httpMethod.get(url, queryParams);
    }

    @POST
    @Path("/datasources/{dataSourceName}/disableSegments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDisableSegmentsByInterval(
            @PathParam("dataSourceName") String dataSourceName,
            @QueryParam("full") String full,
            @QueryParam("searchSegment") String searchSegment,
            @QueryParam("isDescending") @DefaultValue("false") boolean isDescending,
            final String intervals
    )
    {
        Map<String,Object> queryParams = Maps.newHashMap();
        if(full != null){
            queryParams.put("full",full);
        }
        if(searchSegment != null){
            queryParams.put("searchSegment",searchSegment);
        }
        queryParams.put("isDescending",isDescending);
        String url = String.format("%s/datasources/%s/disableSegments", pathPre, dataSourceName);
        return httpMethod.post(url, intervals, queryParams);
    }

    @DELETE
    @Path("/datasources/{dataSourceName}/disable")
    public Response disableDatasourceSource(
            @PathParam("dataSourceName") String dataSourceName
    )
    {
        String url = String.format("%s/datasources/%s/disable", pathPre, dataSourceName);
        return httpMethod.delete(url);
    }

    @DELETE
    @Path("/datasources/{dataSourceName}/delete")
    public Response deleteDatasourceSource(
            @PathParam("dataSourceName") String dataSourceName
    )
    {
        String url = String.format("%s/datasources/%s/delete", pathPre, dataSourceName);
        return httpMethod.delete(url);
    }


    @POST
    @Path("/datasources/{dataSourceName}/enable")
    public Response enableDatasourceSource(
            @PathParam("dataSourceName") String dataSourceName
    )
    {
        String url = String.format("%s/datasources/%s/enable", pathPre, dataSourceName);
        return httpMethod.post(url);
    }


    @DELETE
    @Path("/datasources/{dataSourceName}/intervals/{interval}/disable")
    public Response disableSegmentByInterval(
            @PathParam("dataSourceName") String dataSourceName,
            @PathParam("interval") String interval
    )
    {
        String url = String.format("%s/datasources/%s/intervals/%s/disable", pathPre, dataSourceName, interval);
        return httpMethod.delete(url);
    }

    @DELETE
    @Path("/datasources/{dataSourceName}/intervals/{interval}/delete")
    public Response deleteSegmentByInterval(
            @PathParam("dataSourceName") String dataSourceName,
            @PathParam("interval") String interval
    )
    {
        String url = String.format("%s/datasources/%s/intervals/%s/delete", pathPre, dataSourceName, interval);
        return httpMethod.delete(url);
    }

    @POST
    @Path("/datasources/{dataSourceName}/intervals/{interval}/enable")
    public Response enableSegmentByInterval(
            @PathParam("dataSourceName") String dataSourceName,
            @PathParam("interval") String interval
    )
    {
        String url = String.format("%s/datasources/%s/intervals/%s/enable", pathPre, dataSourceName, interval);
        return httpMethod.post(url);
    }

    @Path("/druid/coordinator/v1/intervals")
    public static class IntervalsResource extends ForwardResource {

        public IntervalsResource() throws IOException {
            ip = configure.getProperty("druid.properties","coordinator_ip");
            pathPre = "http://" + ip + "/druid/coordinator/v1/intervals";
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response getIntervals(
                @Context final HttpServletRequest req,
                @QueryParam("isAscending") @DefaultValue("false") boolean isAscending

        )
        {
            Map<String,Object> queryParams = Maps.newHashMap();
            queryParams.put("isAscending",isAscending);
            String url = String.format("%s");
            return httpMethod.get(url,queryParams);
        }

        @GET
        @Path("/{interval}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response getSpecificIntervals(
                @PathParam("interval") String interval,
                @QueryParam("simple") String simple,
                @QueryParam("full") String full,
                @Context final HttpServletRequest req
        )
        {
            Map<String,Object> queryParams = Maps.newHashMap();
            if(simple != null){
                queryParams.put("simple",simple);
            }
            if(full != null){
                queryParams.put("full",full);
            }
            String url = String.format("%s/%s", pathPre, interval);
            return httpMethod.get(url,queryParams);
        }
    }
}


