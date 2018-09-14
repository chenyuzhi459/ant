package io.sugo.server.http.resource;

import io.sugo.server.http.jersey.JerseyClientFactory;
import io.sugo.server.http.util.HttpMethodProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.ws.rs.client.Client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ForwardResource extends Resource{
    private static final Logger LOG = LogManager.getLogger(ForwardResource.class);
    protected  Client client;
    protected  HttpMethodProxy httpMethod;
    protected  String ip;
    protected  String pathPre;

    public ForwardResource() {
        init();
    }

    private void init() {
        client = JerseyClientFactory.create();
        httpMethod = new HttpMethodProxy(client);
    }

    public String getIp(String properityFileName,String nodeType) {
        String[] ips = configure.getProperty(properityFileName,nodeType + ".ip").split(",");
        if(ips.length < 1) {
            LOG.error(nodeType + " ip is not exist!");
            return null;
        } else {
            String leaderIp;
            for(String ip:ips){
                try {
                    leaderIp = getLeaderIp(ip, nodeType);
                    return leaderIp;
                }catch (Exception e){
//                    e.printStackTrace();
                    LOG.error(String.format("can not get leader from ip(%s) for [%s]",ip,nodeType));
                }

            }
            return null;
        }
    }


    public String getLeaderIp(String ip, String nodeType) throws IOException {
        String result = null;
        if(nodeType.equals("overlord")) {
            nodeType = "indexer";
        }
        String url = String.format("http://%s/druid/%s/v1/leader", ip, nodeType);
        Object entity = httpMethod.get(url).getEntity();
        if(entity instanceof InputStream){
            result= getStrFromInputSteam((InputStream)entity);
        }else {
            result = (String)httpMethod.get(url).getEntity();
        }

        result = result.replace("http://" ,"");
        return result;
    }

    private String getStrFromInputSteam(InputStream in) throws IOException {
        BufferedReader bf=new BufferedReader(new InputStreamReader(in,"UTF-8"));
        //最好在将字节流转换为字符流的时候 进行转码
        StringBuilder buffer=new StringBuilder();
        String line="";
        while((line=bf.readLine())!=null){
            buffer.append(line);
        }

        return buffer.toString();
    }
}