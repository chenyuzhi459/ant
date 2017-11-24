package io.sugo.http.resource.coordinator;

import io.sugo.http.resource.ForwardResource;

public class CoordinatorForwardResource extends ForwardResource{

    public CoordinatorForwardResource() {
        ip = getIp("coordinator");
    }

}
