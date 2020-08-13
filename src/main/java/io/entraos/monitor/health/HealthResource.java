package io.entraos.monitor.health;

import io.entraos.monitor.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Singleton
@Path("health")
public class HealthResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HealthResource.class);

    private final MonitorService monitorService;


    public HealthResource() {
        monitorService = new MonitorService();
    }

    @Autowired
    public HealthResource(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    /**
     * The Monitoring system of our service provider may only detect Ok as http 200.
     * @return http 200 if all well. http 412 if any of the integrations has failed.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessage() {
        Response response = null;
        if (monitorService.isStatusOk()) {
            String status = monitorService.toJson();
            response = Response.ok(status).build();
        } else {
            response = Response.status(412, "see http://<host>:<port>/monitor/status for reason to failure.").build();
        }
        return response;
    }
}
