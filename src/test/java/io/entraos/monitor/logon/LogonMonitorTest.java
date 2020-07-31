package io.entraos.monitor.logon;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static no.cantara.config.ServiceConfig.getProperty;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;


class LogonMonitorTest {
    private static final Logger log = getLogger(LogonMonitorTest.class);
    private static WireMockServer server;
    private static int port;
    private static URI logonUri;

    @BeforeAll
    static void setUp() {
        server = new WireMockServer(wireMockConfig().dynamicPort()); //No-args constructor will start on port 8080, no HTTPS
        server.start();
        port = server.port();
        logonUri = URI.create("http://localhost:" + port + "/logon");
    }

    @Test
    void postLogon() {
        Map<Object, Object> data = new HashMap<>();
        data.put("grant_type", getProperty("logon_grant_type"));
        data.put("username", getProperty("logon_username"));
        data.put("password", getProperty("logon_password"));
        WireMock.configureFor("localhost", port);
        stubFor(post(urlEqualTo("/logon"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("<response>Some content</response>")));
        LogonMonitor logonMonitor = new LogonMonitor(logonUri);
        assertNotNull(logonMonitor);
        HttpResponse response = logonMonitor.postLogon(data);
        assertNotNull(response);
    }

    public static void main(String[] args) throws URISyntaxException {
        Map<Object, Object> data = new HashMap<>();
        data.put("grant_type", getProperty("logon_grant_type"));
        data.put("username", getProperty("logon_useranme"));
        data.put("password", getProperty("logon_password"));
        String logonUriProperty = getProperty("logon_uri");
        URI logonUri = new URI(logonUriProperty);
        LogonMonitor logonMonitor = new LogonMonitor(logonUri);
        HttpResponse response = logonMonitor.postLogon(data);
        log.info("Response: {}", response);
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }
}