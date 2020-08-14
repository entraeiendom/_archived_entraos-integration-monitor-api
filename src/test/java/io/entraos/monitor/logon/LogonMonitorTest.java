package io.entraos.monitor.logon;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.entraos.monitor.Status;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static no.cantara.config.ServiceConfig.getProperty;
import static org.junit.Assert.assertEquals;
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
        assertNotNull(LogonMonitor.ofFormData(data));
        Status status = logonMonitor.postLogon(data);
        assertNotNull(status);
        assertEquals(Status.OK, status);
    }

    @Test
    public void ofFormDataTest() {
        Map<Object, Object> data = new HashMap<>();
        data.put("grant_type", " ");
        data.put("username", "uname");
        data.put("password", null);
        assertNotNull(LogonMonitor.ofFormData(data));
    }

    public static void main(String[] args) throws URISyntaxException {
        Map<Object, Object> data = new HashMap<>();
        data.put("grant_type", getProperty("logon_grant_type"));
        data.put("username", getProperty("logon_useranme"));
        data.put("password", getProperty("logon_password"));
        String logonUriProperty = getProperty("logon_uri");
        URI logonUri = new URI(logonUriProperty);
        LogonMonitor logonMonitor = new LogonMonitor(logonUri);
        Status status = logonMonitor.postLogon(data);
        log.info("Status: {}", status);
    }


}