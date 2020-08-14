package io.entraos.monitor.logon;

import io.entraos.monitor.IntegrationMonitor;
import io.entraos.monitor.Status;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.UnresolvedAddressException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static no.cantara.config.ServiceConfig.getProperty;
import static org.slf4j.LoggerFactory.getLogger;

public class LogonMonitor implements IntegrationMonitor {
    private static final Logger log = getLogger(LogonMonitor.class);
    protected HttpClient client;
    private HttpRequest httpRequest;
    private final URI logonUri;

    public LogonMonitor(URI logonUri) {
        this.logonUri = logonUri;
        client = HttpClient.newBuilder().build();
    }

    @Override
    public Status connect() {
        Status status = Status.FAILED;
        Map<Object, Object> data = new HashMap<>();
        data.put("grant_type", getProperty("logon_grant_type"));
        data.put("username", getProperty("logon_username"));
        data.put("password", getProperty("logon_password"));
        status = postLogon(data);

        log.trace("Data: {}, Status: {}", data, status);
        return status;
    }

    /*
        Eg:
        "grant_type=password"
         "username=baardl-test"
         "password=anything"
         */
    public Status postLogon(Map<Object, Object> body) {
        Status status = Status.NOT_RUN_YET;
        HttpRequest.BodyPublisher bodyPublisher = ofFormData(body);
        httpRequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(logonUri)
                .POST(bodyPublisher)
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response != null) {
                log.trace("Response Code: {}. Body: {}", response.statusCode(), response.body());
                int httpStatus = response.statusCode();
                switch (httpStatus) {
                    case 200:
                        status = Status.OK;
                        break;
                    default:
                        status = Status.FAILED;
                }
            } else {
                log.trace("No response from {}", logonUri);
                status = Status.FAILED;
            }

        } catch (ConnectException ce) {
            Throwable rootCause = findRootCause(ce);
            if (rootCause != null && rootCause instanceof UnresolvedAddressException) {
                log.trace("Missing DNS for {}", logonUri);
                status = Status.UNKNOWN_HOST;
            } else {
                log.trace("ConnectEception {}", ce);
                status = Status.FAILED;
            }
        } catch (IOException e) {
            log.info("IOException: {}", e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            log.info("InteruptedException: {}", e);
            e.printStackTrace();
        }

        return status;
    }

    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        log.trace("Build body string from {}", data);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            if (data.get(entry.getKey()) != null) {
                builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
                builder.append("=");
                builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
            }
        }
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(builder.toString());
        log.trace("Built publisher: {}", bodyPublisher);
        return bodyPublisher;
    }

    public static Throwable findRootCause(Throwable throwable) {
        Objects.requireNonNull(throwable);
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}
