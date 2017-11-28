package io.sugo.http.resource.log;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import io.sugo.http.resource.ForwardResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Map;

@Path("/log")
public class LogResource extends ForwardResource {

    String agentPort;

    public LogResource() {
        pathPre = "http://";
        agentPort = configure.getProperty("system.properties","log.agent.port");
    }

    @GET
    @Path("/logConfig")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logConfig() {

        String jsonDir = configure.getProperty("system.properties","json.dir");
        StringBuilder sb = new StringBuilder();
        try {
            File json = new File(jsonDir);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(json)));
            String tempString;

            while ((tempString = br.readLine()) != null) {
                sb.append(tempString);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.ok().entity(sb.toString()).build();
    }


    @GET
    @Path("/logList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logList(
            @QueryParam("service") String service,
            @QueryParam("module") String module,
            @QueryParam("hostname") String hostname
    ) {
        Map<String,Object> queryParams = Maps.newHashMap();
        if(service != null){
            queryParams.put("service",service);
        }
        if(module != null){
            queryParams.put("module",module);
        }
        String url = String.format("%s%s:%s/logList",  pathPre, hostname, agentPort);
        return httpMethod.get(url,queryParams);
    }

    @GET
    @Path("/logging")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logging(
            @QueryParam("name") String name,
            @QueryParam("host") String host,
            @QueryParam("line") String line
    ){
        Map<String,Object> queryParams = Maps.newHashMap();
        if(name != null){
            queryParams.put("name",name);
        }
        if(host != null){
            queryParams.put("host",host);
        }
        if(line != null){
            queryParams.put("line",line);
        }

        String url = String.format("%s%s:%s/logging",  pathPre, host, agentPort);
        return httpMethod.get(url,queryParams);
    }


    @GET
    @Path("/readLog")
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewLog(
            @QueryParam("name") String name,
            @QueryParam("host") String host,
            @QueryParam("lines") String lines
    ){
        Map<String,Object> queryParams = Maps.newHashMap();
        if(name != null){
            queryParams.put("name",name);
        }
        if(host != null){
            queryParams.put("host",host);
        }
        if(lines != null){
            queryParams.put("lines",lines);
        }

        String url = String.format("%s%s:%s/readLog",  pathPre, host, agentPort);
        return httpMethod.get(url,queryParams);
    }

    @GET
    @Path("/logPage")
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewLogPage(
            @QueryParam("name") String name,
            @QueryParam("host") String host,
            @QueryParam("index") String index
    ){
        Map<String,Object> queryParams = Maps.newHashMap();
        if(name != null){
            queryParams.put("name",name);
        }
        if(index != null){
            queryParams.put("index",index);
        }

        String url = String.format("%s%s:%s/logPage",  pathPre, host, agentPort);
        return httpMethod.get(url,queryParams);
    }

}

