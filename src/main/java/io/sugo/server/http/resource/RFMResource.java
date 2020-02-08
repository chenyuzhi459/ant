package io.sugo.server.http.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.sugo.common.guice.annotations.Json;
import io.sugo.services.usergroup.bean.rfm.CustomRFMParams;
import io.sugo.services.usergroup.bean.rfm.DefaultRFMParams;
import io.sugo.services.usergroup.bean.rfm.RFMParams;
import io.sugo.services.usergroup.model.bean.CustomizedRFMDto;
import io.sugo.services.usergroup.bean.rfm.RFMRequestBean;
import io.sugo.services.usergroup.model.rfm.QuantileModel;
import io.sugo.services.usergroup.model.rfm.RFMManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/ant/model/rfm")
public class RFMResource {

    private final RFMManager rfmManager;

    private final ObjectMapper jsonMapper;

    @Inject
    public RFMResource(@Json ObjectMapper jsonMapper, RFMManager rfmManager) {
        this.jsonMapper = jsonMapper;
        this.rfmManager = rfmManager;
    }

    @POST
    @Path("/default")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getDefaultQuantileModel(final RFMRequestBean requestBean) {
        try {
            RFMParams params = requestBean.getParams();
            QuantileModel quantileModel = null;
            if (params instanceof DefaultRFMParams){
                DefaultRFMParams defaultRFMParams = (DefaultRFMParams)params;
                quantileModel = rfmManager.getDefaultQuantileModel(requestBean, defaultRFMParams.getR(), defaultRFMParams.getF(), defaultRFMParams.getM());
            }else {
                CustomRFMParams customRFMParams = (CustomRFMParams)params;
                quantileModel =  rfmManager.getCustomizedQuantileModel(requestBean, customRFMParams.getR(), customRFMParams.getF(), customRFMParams.getM());
            }

            return Response.ok(quantileModel).build();
        } catch (Throwable e) {
            return Response.serverError().entity(ImmutableMap.of("error", e.getMessage())).build();
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


    private void check(CustomizedRFMDto rfmDto) {
        Preconditions.checkNotNull(rfmDto.getDatasource(), "Data source can not be null.");
        if (rfmDto.getRq().length <= 0 || rfmDto.getRq().length > 3) {
            throw new IllegalArgumentException("'RQ' must be at least contains one element and at most 3 elements.");
        }
        if (rfmDto.getFq().length <= 0 || rfmDto.getFq().length > 3) {
            throw new IllegalArgumentException("'FQ' must be at least contains one element and at most 3 elements.");
        }
        if (rfmDto.getMq().length <= 0 || rfmDto.getMq().length > 3) {
            throw new IllegalArgumentException("'MQ' must be at least contains one element and at most 3 elements.");
        }
    }

}
