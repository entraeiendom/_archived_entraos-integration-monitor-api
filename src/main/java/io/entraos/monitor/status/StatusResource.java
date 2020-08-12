package io.entraos.monitor.status;

import io.entraos.monitor.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Singleton
@Path("status")
public class StatusResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StatusResource.class);

    private final MonitorService monitorService;

    @Autowired
    public StatusResource() {
        monitorService = new MonitorService();
    }

    public StatusResource(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getMessage() {

        return monitorService.toJson();
    }
}
