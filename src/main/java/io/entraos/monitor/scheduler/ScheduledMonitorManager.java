package io.entraos.monitor.scheduler;

import io.entraos.monitor.IntegrationMonitor;
import io.entraos.monitor.Status;
import io.entraos.monitor.alerting.Alerter;
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
    private final Alerter[] alerters;
    private Instant lastSuccessfulConnect = null;
    private Instant lastFailedConnect = null;

    private final IntegrationMonitor monitor;
    private Status lastKnownStatus = Status.NOT_RUN_YET;

    public ScheduledMonitorManager(IntegrationMonitor integrationMonitor, Alerter... alerters) {
        this.monitor = integrationMonitor;
        this.alerters = alerters;
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
                        Status currentStatus = monitor.connect();
                        if (currentStatus == Status.OK) {
                            setLastSuccessfulConnect(Instant.now());
                        } else {
                            setLastFailedConnect(Instant.now());
                        }
                        notifyAlerters(currentStatus);
                        setStatus(currentStatus);
                        log.info("Flushed. Now waiting {} seconds for next run.", SECONDS_BETWEEN_SCHEDULED_RUNS);
                        scheduled_monitor_running = false;
                    } catch (Exception e) {
                        log.info("Exception trying to run scheduled monitor on {}. Reason: {}", monitor, e.getMessage());
                        notifyAlerters(Status.FAILED);
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

    synchronized void notifyAlerters(Status currentStatus) {
        if (currentStatus != null) {
            for (Alerter alerter : alerters) {
                if (alerter.isAlertingEnabled()) {
                    String serviceName = getProperty("service_name");
                    String environment = getProperty("environment");
                    if (currentStatus == Status.OK) {
                        if (lastKnownStatus == Status.NOT_RUN_YET || lastKnownStatus == Status.OK) {
                            //no need to notify.
                        } else {
                            String message = "Integration OK on" + serviceName + " in " + environment + ". Status is " + currentStatus.toString();
                            log.trace("Sending revival alert: {}", message);
                            alerter.notifyRevival(message);
                        }
                    } else {
                        if (lastKnownStatus == Status.NOT_RUN_YET || lastKnownStatus == Status.OK) {
                            String message = "Integration Failed on" + serviceName + " in " + environment + ". Status is " + currentStatus.toString();
                            log.trace("Sending failure alert: {}", message);
                            alerter.notifyFailure(message);
                        } else {
                            //repeat alerting is not implemented.
                        }
                    }
                }
            }
        }
    }

    synchronized void setStatus(Status status) {
        this.lastKnownStatus = status;
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

    public Status getStatus() {
        return lastKnownStatus;
    }
}
