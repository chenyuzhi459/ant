package io.sugo.server.http.resource;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.sugo.services.usergroup.bean.rfm.RFMRequestBean;
import io.sugo.services.usergroup.model.ModelManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/ant/model")
public class RFMResource {
    private static final Logger log = LogManager.getLogger(RFMResource.class);
    private final ModelManager modelManager;

    @Inject
    public RFMResource(ModelManager modelManager) {
        this.modelManager = modelManager;
    }

    @POST
    @Path("/rfm")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getDefaultQuantileModel(final RFMRequestBean requestBean) {
        try {
            modelManager.addToRedisQueue(requestBean);

            return Response.ok(ImmutableMap.of("requestId", requestBean.getRequestId(),
                    "status", "success")).build();
        } catch (Throwable e) {
            log.error("add model request to queue error", e);
            return Response.serverError().entity(ImmutableMap.of("status","error","msg", e.getMessage())).build();
        }
    }

    @GET
    @Path("/result/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response slice(@PathParam("id") final String id) {
        try {

            return Response.ok(modelManager.fetchResult(id)).build();
        } catch (Throwable e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
//
//    @GET
//    @Path("/slice/default/{param}")
//    @Produces({MediaType.APPLICATION_JSON})
//    @Consumes({MediaType.APPLICATION_JSON})
//    public Response slice(@PathParam("param") final String param) {
//        String newParam = new String(Base64.getDecoder().decode(param));
//        DefaultRFMDto rfmDto = null;
//        try {
//            rfmDto = jsonMapper.readValue(newParam, DefaultRFMDto.class);
//        } catch (IOException ignore) {
//        }
//
//        check(rfmDto);
//        try {
//            String queryStr = rfmDto.buildQuery();
//            QuantileModel quantileModel = rfmManager.getDefaultQuantileModel(queryStr,
//                    rfmDto.getR(), rfmDto.getF(), rfmDto.getM());
//            return Response.ok(quantileModel).header("Access-Control-Allow-Origin", "*").build();
//        } catch (Throwable e) {
//            return Response.serverError().entity(e.getMessage()).header("Access-Control-Allow-Origin", "*").build();
//        }
//    }



}
