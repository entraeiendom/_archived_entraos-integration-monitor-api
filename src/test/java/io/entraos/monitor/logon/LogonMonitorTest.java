package io.entraos.monitor.logon;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static no.cantara.config.ServiceConfig.getProperty;
import static org.junit.Assert.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;


public class LogonMonitorTest {
    private static final Logger log = getLogger(LogonMonitorTest.class);
    private int port;
    private URI logonUri;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .dynamicPort()
    );


    @Test
    public void postLogon() {
        port = wireMockRule.port();
        logonUri = URI.create("http://localhost:" + port + "/logon");
        Map<Object, Object> data = new HashMap<>();
        data.put("grant_type", getProperty("logon_grant_type"));
        data.put("username", getProperty("logon_username"));
        data.put("password", getProperty("logon_password"));
        wireMockRule.stubFor(post(urlEqualTo("/logon"))
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


}