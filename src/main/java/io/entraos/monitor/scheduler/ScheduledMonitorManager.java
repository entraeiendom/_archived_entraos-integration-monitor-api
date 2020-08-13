package io.entraos.monitor.scheduler;

import io.entraos.monitor.IntegrationMonitor;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static no.cantara.config.ServiceConfig.getProperty;
import static org.slf4j.LoggerFactory.getLogger;

public class ScheduledMonitorManager {
    private static final Logger log = getLogger(ScheduledMonitorManager.class);
    private final int DEFAULT_SECONDS_BETWEEN_SCHEDULED_RUNS = 60*10;
    public static final String IMPORT_SCHEDULE_MINUTES_KEY = "schedule_minutes";
    private static boolean scheduled_monitor_started = false;
    private static boolean scheduled_monitor_running = false;
    private final int SECONDS_BETWEEN_SCHEDULED_RUNS;
    private Instant lastSuccessfulConnect = null;
    private Instant lastFailedConnect = null;

    private final IntegrationMonitor monitor;

    public ScheduledMonitorManager(IntegrationMonitor integrationMonitor) {
        this.monitor = integrationMonitor;
        Integer scheduleMinutes = findScheduledMinutes();
        if (scheduleMinutes != null) {
            SECONDS_BETWEEN_SCHEDULED_RUNS = scheduleMinutes * 60;
        } else {
            SECONDS_BETWEEN_SCHEDULED_RUNS = DEFAULT_SECONDS_BETWEEN_SCHEDULED_RUNS;
        }
    }

    private Integer findScheduledMinutes() {
        Integer scheduleMinutes = null;
        String scheduleMinutesValue = getProperty(IMPORT_SCHEDULE_MINUTES_KEY);
        if (scheduleMinutesValue != null) {
            try {
                scheduleMinutes = Integer.valueOf(scheduleMinutesValue);
            } catch (NumberFormatException nfe) {
                log.debug("Failed to create scheduledMinutes from [{}]", scheduleMinutesValue);
            }
        }
        return scheduleMinutes;
    }

    public void startScheduledMonitor() {
        if (!scheduled_monitor_started) {
            log.info("Starting ScheduledMonitorManager");

            scheduled_monitor_started = true;
            ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

            Runnable task1 = () -> {
                if (scheduled_monitor_running == false) {
                    log.info("Running an new import round.");

                    try {
                        scheduled_monitor_running = true;
                        monitor.connect();
                        setLastSuccessfulConnect(Instant.now());
                        log.info("Flushed. Now waiting {} seconds for next run.", SECONDS_BETWEEN_SCHEDULED_RUNS);
                        scheduled_monitor_running = false;
                    } catch (Exception e) {
                        log.info("Exception trying to run scheduled monitor on {}. Reason: {}", monitor, e.getMessage());
                        setLastFailedConnect(Instant.now());
                        scheduled_monitor_running = false;
                    }
                } else {
                    log.info("Last round of imports has not finished yet. ");
                }
            };

            // init Delay = 5, repeat the task every 60 second
            ScheduledFuture<?> scheduledFuture = ses.scheduleAtFixedRate(task1, 5, SECONDS_BETWEEN_SCHEDULED_RUNS, TimeUnit.SECONDS);
        } else {
            log.info("ScheduledImportManager is is already started");
        }
    }

    public Instant getLastSuccessfulConnect() {
        return lastSuccessfulConnect;
    }

    private synchronized void setLastSuccessfulConnect(Instant lastSuccessfulConnect) {
        this.lastSuccessfulConnect = lastSuccessfulConnect;
    }

    public Instant getLastFailedConnect() {
        return lastFailedConnect;
    }

    private synchronized void setLastFailedConnect(Instant lastFailedConnect) {
        this.lastFailedConnect = lastFailedConnect;
    }
}
