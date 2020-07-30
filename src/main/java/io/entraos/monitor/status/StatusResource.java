package io.entraos.monitor.status;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("status")
public class StatusResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StatusResource.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessage() {

        return "My message\n";
    }
}
