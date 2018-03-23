package io.sugo.http.resource.system;

import io.sugo.http.resource.Resource;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/link")
public class QuickLinksResource extends Resource {

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLink(
            @QueryParam("target") String target
    ) {

        if(StringUtils.isNotBlank(target)) {
            String linkString = configure.getProperty("link.properties",target, null);
            if(StringUtils.isNotBlank(linkString)) {
                return Response.ok().entity(linkString).build();
            }
        }
        return Response.status(400).build();
    }
}
