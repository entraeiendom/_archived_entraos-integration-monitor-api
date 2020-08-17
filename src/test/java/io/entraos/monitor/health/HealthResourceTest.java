package io.entraos.monitor.health;

import io.entraos.monitor.MonitorService;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HealthResourceTest {

    private MonitorService monitorService = null;
    private HealthResource healthResource;

    @Before
    public void setUp() throws Exception {
        monitorService = mock(MonitorService.class);
        healthResource = new HealthResource(monitorService);
    }

    @Test
    public void getMessageHealthy() {
        when(monitorService.isStatusOk()).thenReturn(true);
        String healthyJson = "{\"status\": \"ok\"";
        when(monitorService.toJson()).thenReturn(healthyJson);
        Response response = healthResource.getMessage();
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        String body = (String) response.getEntity();
        assertEquals(healthyJson, body);
    }

    @Test
    public void getMessageUnHealthy() {
        when(monitorService.isStatusOk()).thenReturn(false);
        String unHealthyBody = "see http://<host>:<port>/monitor/status for reason to failure.";
        when(monitorService.toJson()).thenReturn(unHealthyBody);
        Response response = healthResource.getMessage();
        assertNotNull(response);
        assertEquals(412, response.getStatus());
        String body = response.getStatusInfo().getReasonPhrase();
        assertEquals(unHealthyBody, body);
    }
}