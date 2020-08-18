package io.entraos.monitor.alerting;

public interface Alerter {
    void notifyFailure( String message);

    void notifyRevival(String message);

    boolean isAlertingEnabled();
}
