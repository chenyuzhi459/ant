package io.sugo.http.resource.broker;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/status")
public class StatusResource extends BrokerForwardResource {

    public StatusResource() throws IOException {
        pathPre = "http://" + ip + "/status";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response status() {

        String url = String.format("%s", pathPre);
        return httpMethod.get(url);
    }


}

