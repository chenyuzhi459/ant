package io.sugo.http.resource;

import io.sugo.http.Configure;
import org.apache.log4j.Logger;

public abstract class Resource {
    protected static final Logger LOG = Logger.getLogger(Resource.class);
    protected static Configure configure = Configure.getConfigure();

    public Resource() {
        LOG.info("Resource created");
    }
}
