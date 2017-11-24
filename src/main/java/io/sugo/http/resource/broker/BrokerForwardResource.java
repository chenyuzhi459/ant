package io.sugo.http.resource.broker;

import io.sugo.http.resource.ForwardResource;

public class BrokerForwardResource extends ForwardResource {

    public BrokerForwardResource() {
        ip = configure.getProperty("druid.properties","broker.ip");
    }
}
