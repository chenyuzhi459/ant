package io.sugo.server.http.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.sugo.common.guice.annotations.Json;
import io.sugo.services.usergroup.bean.rfm.CustomRFMParams;
import io.sugo.services.usergroup.bean.rfm.DefaultRFMParams;
import io.sugo.services.usergroup.bean.rfm.RFMParams;
import io.sugo.services.usergroup.bean.rfm.RFMRequestBean;
import io.sugo.services.usergroup.model.ModelManager;
import io.sugo.services.usergroup.model.rfm.QuantileModel;
import io.sugo.services.usergroup.model.rfm.RFMManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/ant/model/rfm")
public class RFMResource {

    private final ModelManager modelManager;

    @Inject
    public RFMResource(ModelManager modelManager) {
        this.modelManager = modelManager;
    }

    @POST
//    @Path("/default")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getDefaultQuantileModel(final RFMRequestBean requestBean) {
        try {
            modelManager.addToRedisQueue(requestBean);

            return Response.ok(ImmutableMap.of("requestId", requestBean.getRequestId(),
                    "status", "success")).build();
        } catch (Throwable e) {
            return Response.serverError().entity(ImmutableMap.of("ok","success","msg", e.getMessage())).build();
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
//
//    @GET
//    @Path("/slice/customized/{param}")
//    @Produces({MediaType.APPLICATION_JSON})
//    @Consumes({MediaType.APPLICATION_JSON})
//    public Response sliceCustomized(@PathParam("param") final String param) {
//        String newParam = new String(Base64.getDecoder().decode(param));
//        CustomizedRFMDto rfmDto = null;
//        try {
//            rfmDto = jsonMapper.readValue(newParam, CustomizedRFMDto.class);
//        } catch (IOException ignore) {
//        }
//
//        check(rfmDto);
//        try {
//            String queryStr = rfmDto.buildQuery();
//            QuantileModel quantileModel = rfmManager.getCustomizedQuantileModel(queryStr,
//                    rfmDto.getRq(), rfmDto.getFq(), rfmDto.getMq());
//
//            return Response.ok(quantileModel).header("Access-Control-Allow-Origin", "*").build();
//        } catch (Throwable e) {
//            return Response.serverError().entity(e.getMessage()).header("Access-Control-Allow-Origin", "*").build();
//        }
//    }



}
