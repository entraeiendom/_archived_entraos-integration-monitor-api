package io.entraos.monitor.scheduler;

import io.entraos.monitor.IntegrationMonitor;
import io.entraos.monitor.Status;
import io.entraos.monitor.alerting.Alerter;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class ScheduledMonitorManagerTest {

    private ScheduledMonitorManager manager = null;
    private Alerter alerter;
    private IntegrationMonitor integrationMonitor;

    @Before
    public void setUp() throws Exception {
        alerter = mock(Alerter.class);
        integrationMonitor = mock(IntegrationMonitor.class);
        manager = new ScheduledMonitorManager(integrationMonitor, alerter);
    }

    @Test
    public void notifyAlertersFromUnknownToFailure() {
        when(alerter.isAlertingEnabled()).thenReturn(true);
        manager.notifyAlerters(Status.FAILED);

        verify(alerter, times(1)).isAlertingEnabled();
    }

    @Test
    public void multipleFailedStatuses() {

        when(alerter.isAlertingEnabled()).thenReturn(true);
        manager.notifyAlerters(Status.FAILED);
        manager.notifyAlerters(Status.FAILED);
        verify(alerter, times(1)).isAlertingEnabled();
    }
}