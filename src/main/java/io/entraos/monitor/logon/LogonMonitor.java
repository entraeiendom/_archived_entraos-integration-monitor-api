package io.entraos.monitor.logon;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class LogonMonitor {
    private static final Logger log = getLogger(LogonMonitor.class);
    protected HttpClient client;
    private HttpRequest httpRequest;
    private final URI logonUri;

    public LogonMonitor(URI logonUri) {
        this.logonUri = logonUri;
        client = HttpClient.newBuilder().build();
    }

    /*
    Eg:
    "grant_type=password"
     "username=baardl-test"
     "password=anything"
     */
    public HttpResponse postLogon(Map<Object, Object> body) {
log.info("****jenkins trail");
        httpRequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(logonUri)
                .POST(ofFormData(body))
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            // print status code
            System.out.println(response.statusCode());

            // print response body
            System.out.println(response.body());
        } catch (IOException e) {
            log.info("IOException: {}", e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            log.info("InteruptedException: {}", e);
            e.printStackTrace();
        }

        log.info("***jenkins response: {}", response);
        return response;
    }

    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}
