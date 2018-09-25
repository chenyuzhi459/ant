package io.sugo.server.http.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import io.sugo.common.guice.annotations.Json;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;
import io.sugo.services.pathanalysis.PathAnalyzer;
import io.sugo.services.pathanalysis.model.AccessTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

@Path("/ant/process/pa/")
public class PathAnalysisResource {

    private static final Logger log = LogManager.getLogger(PathAnalysisResource.class);

    private final PathAnalyzer pathAnalyzer;

    private final ObjectMapper jsonMapper;

    @Inject
    public PathAnalysisResource(@Json ObjectMapper jsonMapper, PathAnalyzer pathAnalyzer) {
        this.jsonMapper = jsonMapper;
        this.pathAnalyzer = pathAnalyzer;
    }

    @POST
//    @Path("/normal")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response normalPath(PathAnalysisDto pathAnalysisDto) {
        check(pathAnalysisDto);
        try {
            String queryStr = pathAnalysisDto.buildScanQuery();
            boolean isReverseDirection = PathAnalysisDto.REVERSE_DIRECTION.equals(pathAnalysisDto.getDirection());
            AccessTree tree = pathAnalyzer.getAccessTree(queryStr,
                    pathAnalysisDto.getHomePage(), isReverseDirection, pathAnalysisDto.getBrokerUrl());
            return Response.ok(tree == null ? Collections.EMPTY_LIST : tree).build();
        } catch (Throwable e) {
            log.error("Resource handle pathAnalysis error!",e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

//    @POST
//    @Path("/reverse")
//    @Produces({MediaType.APPLICATION_JSON})
//    @Consumes({MediaType.APPLICATION_JSON})
//    public Response reversePath(PathAnalysisDto pathAnalysisDto) {
//        check(pathAnalysisDto);
//        try {
//            String queryStr = pathAnalysisDto.buildScanQuery();
//            AccessTree tree = pathAnalyzer.getAccessTree(queryStr,
//                    pathAnalysisDto.getHomePage(), true, pathAnalysisDto.getBrokerUrl());
//
//            return Response.ok(tree == null ? Collections.EMPTY_LIST : tree).build();
//        } catch (Throwable e) {
//            return Response.serverError().entity(e.getMessage()).build();
//        }
//    }

    private void check(PathAnalysisDto pathAnalysisDto) {
        try {
            log.info(String.format("PathAnalysisResource original param: %s", jsonMapper.writeValueAsString(pathAnalysisDto)));
        } catch (JsonProcessingException ignore) {
        }

        Preconditions.checkNotNull(pathAnalysisDto.getDataSource(), "Data source can not be null.");
        Preconditions.checkNotNull(pathAnalysisDto.getHomePage(), "Home page can not be null.");
        Preconditions.checkNotNull(pathAnalysisDto.getBrokerUrl(), "BrokerUrl can not be null.");
        Preconditions.checkNotNull(pathAnalysisDto.getDimension().getSessionId(), "SessionId can not be null.");
        Preconditions.checkNotNull(pathAnalysisDto.getDimension().getUserId(), "UserId can not be null.");
        Preconditions.checkNotNull(pathAnalysisDto.getDimension().getPageName(), "PageName can not be null.");
    }

}
