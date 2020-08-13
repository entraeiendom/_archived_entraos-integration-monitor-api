package io.entraos.monitor.logon;

import io.entraos.monitor.IntegrationMonitor;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
    public void connect() {
        Map<Object, Object> data = new HashMap<>();
        data.put("grant_type", getProperty("logon_grant_type"));
        data.put("username", getProperty("logon_username"));
        data.put("password", getProperty("logon_password"));
        HttpResponse response = postLogon(data);
        if (response != null && response.statusCode() == 200) {
            //FIXME need to tell if reason to failure was HostNotFound or username/pw failed.
            log.trace("Connected ok.");
        } else if (response != null) {
            throw new IllegalStateException("Connect to " + logonUri + " failed. HttpCode: " + response.statusCode() + ". Body: " + response.body());
        }
        log.trace("Data: {}, Response: {}");
    }

    /*
        Eg:
        "grant_type=password"
         "username=baardl-test"
         "password=anything"
         */
    public HttpResponse postLogon(Map<Object, Object> body) {
        HttpRequest.BodyPublisher bodyPublisher = ofFormData(body);
        httpRequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(logonUri)
                .POST(bodyPublisher)
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            log.trace("Response Code: {}. Body: {}", response.statusCode(), response.body());
        } catch (IOException e) {
            log.info("IOException: {}", e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            log.info("InteruptedException: {}", e);
            e.printStackTrace();
        }

        return response;
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
}
