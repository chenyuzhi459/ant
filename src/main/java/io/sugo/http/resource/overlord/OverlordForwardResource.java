package io.sugo.http.resource.overlord;

import io.sugo.http.resource.ForwardResource;

public class OverlordForwardResource extends ForwardResource {

    public OverlordForwardResource() {
        ip = getIp("druid.properties","overlord");
    }
}
