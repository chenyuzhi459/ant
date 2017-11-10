package io.sugo.http.resource;


import io.sugo.http.jersey.JerseyClientFactory;
import io.sugo.http.util.HttpMethodProxy;
import org.apache.log4j.Logger;

import javax.ws.rs.client.Client;

public class ForwardResource extends Resource{

    private static final Logger LOG = Logger.getLogger(ForwardResource.class);
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
}
