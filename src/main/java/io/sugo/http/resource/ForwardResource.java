package io.sugo.http.resource;

import io.sugo.http.jersey.JerseyClientFactory;
import io.sugo.http.util.HttpMethodProxy;
import javax.ws.rs.client.Client;

public class ForwardResource extends Resource{

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

    public String getIp(String nodeType) {
        String[] ips = configure.getProperty("druid.properties",nodeType + ".ip").split(",");
        if(ips.length < 1) {
            LOG.error(nodeType + " ip is not exist!");
            return null;
        } else if(ips.length == 1) {
            return ips[0];
        } else {
            return getLeaderIp(ips[0], nodeType);
        }
    }


    public String getLeaderIp(String ip, String nodeType) {
        if(nodeType.equals("overlord")) {
            nodeType = "indexer";
        }
        String url = String.format("http://%s/druid/%s/v1/leader", ip, nodeType);
        return (String)httpMethod.get(url).getEntity();
    }
}
