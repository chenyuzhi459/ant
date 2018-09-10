package io.sugo.server.http.util;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public class HttpMethod {

    private Client client;

    public HttpMethod(Client client) {
        this.client = client;
    }

    public Response get(WebTarget target) {
        Response rep = getShortConnRequest(target).get(Response.class);
        return rep;
    }

    public Response get(WebTarget target, Map<String,Object> queryParams) {
        WebTarget withQueryParamTaget = target;
        for(Map.Entry<String,Object> param: queryParams.entrySet()){
            withQueryParamTaget = withQueryParamTaget.queryParam(param.getKey(),param.getValue());
        }
        Response rep = getShortConnRequest(withQueryParamTaget).get(Response.class);
        return rep;
    }

    public Response getWithHeader(WebTarget target,Map<String,Object> header) {
        Invocation.Builder builder = getShortConnRequest(target);
        for(String key : header.keySet()){
            builder.header(key,header.get(key));
        }
        Response rep = builder.get(Response.class);
        return rep;
    }

    public Response post(WebTarget target, String data ,Map<String,Object> queryParams) {
        WebTarget withQueryParamTaget = target;
        for(Map.Entry<String,Object> param: queryParams.entrySet()){
            withQueryParamTaget = withQueryParamTaget.queryParam(param.getKey(),param.getValue());
        }
        Response rep = getShortConnRequest(withQueryParamTaget)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(data,MediaType.APPLICATION_JSON),Response.class);
        return rep;
    }

    public Response post(WebTarget target, Map<String,Object> queryParams) {
        WebTarget withQueryParamTaget = target;
        for(Map.Entry<String,Object> param: queryParams.entrySet()){
            withQueryParamTaget = withQueryParamTaget.queryParam(param.getKey(),param.getValue());
        }
        Response rep = getShortConnRequest(withQueryParamTaget)
                .accept(MediaType.APPLICATION_JSON)
                .build("POST").invoke(Response.class);
        return rep;
    }

    public Response post(WebTarget target, String data) {
        Response rep = getShortConnRequest(target)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(data,MediaType.APPLICATION_JSON),Response.class);
        return rep;
    }

    public Response post(WebTarget target) {
        Response rep = getShortConnRequest(target)
                .accept(MediaType.APPLICATION_JSON)
                .build("POST").invoke(Response.class);
        return rep;
    }

    public Response postWithObjectParam(WebTarget target, Object objParam){
        Response rep = getShortConnRequest(target)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(objParam,MediaType.APPLICATION_JSON),Response.class);
        return rep;
    }

    public Response delete(WebTarget target) {
        Response rep = getShortConnRequest(target).delete(Response.class);
        return rep;
    }

    public Response delete(WebTarget target, Map<String,Object> queryParams) {
        WebTarget withQueryParamTaget = target;
        for(Map.Entry<String,Object> param: queryParams.entrySet()){
            withQueryParamTaget = withQueryParamTaget.queryParam(param.getKey(),param.getValue());
        }
        Response rep = getShortConnRequest(withQueryParamTaget).delete(Response.class);
        return rep;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private Invocation.Builder getShortConnRequest(WebTarget target){
        return target.request().header("Connection","close");
    }
}
