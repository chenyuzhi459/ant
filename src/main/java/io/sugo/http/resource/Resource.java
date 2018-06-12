package io.sugo.http.resource;

import io.sugo.http.Configure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public abstract class Resource {
    private static final Logger LOG = LogManager.getLogger(Resource.class);
    protected static Configure configure = Configure.getConfigure();

    public Resource() {
        LOG.info("Resource created");
    }
}
