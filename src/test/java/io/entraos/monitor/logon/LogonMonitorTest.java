package io.entraos.monitor.logon;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.entraos.monitor.Status;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.slf4j.Logger;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static no.cantara.config.ServiceConfig.getProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;


public class LogonMonitorTest {
    private static final Logger log = getLogger(LogonMonitorTest.class);
    private int port;
    private URI logonUri;
    private Map<Object,Object> bodyData = null;
    private HttpClient mockClient = null;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .dynamicPort()
    );

    @Before
    public void setUp() throws Exception {
        bodyData = new HashMap<>();
        bodyData.put("grant_type", getProperty("logon_grant_type"));
        bodyData.put("username", getProperty("logon_username"));
        bodyData.put("password", getProperty("logon_password"));

        mockClient = mock(HttpClient.class);

        port = wireMockRule.port();
        logonUri = URI.create("http://localhost:" + port + "/logon");
    }

    @Test
    public void postLogon() {
//        port = wireMockRule.port();
//        logonUri = URI.create("http://localhost:" + port + "/logon");

        wireMockRule.stubFor(post(urlEqualTo("/logon"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("<response>Some content</response>")));
        LogonMonitor logonMonitor = new LogonMonitor(logonUri);
        assertNotNull(logonMonitor);
        assertNotNull(LogonMonitor.ofFormData(bodyData));
        Status status = logonMonitor.postLogon(bodyData);
        assertNotNull(status);
        assertEquals(Status.OK, status);
    }

    @Test
    public void badrequest() {
//        port = wireMockRule.port();
//        logonUri = URI.create("http://localhost:" + port + "/logon");

        wireMockRule.stubFor(post(urlEqualTo("/logon"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Bad Request")));
        LogonMonitor logonMonitor = new LogonMonitor(logonUri);
        assertNotNull(logonMonitor);
        assertNotNull(LogonMonitor.ofFormData(bodyData));
        Status status = logonMonitor.postLogon(bodyData);
        assertNotNull(status);
        assertEquals(Status.BAD_REQUEST, status);
    }

    @Test
    public void wrongUsernamePassword() {
        port = wireMockRule.port();
        logonUri = URI.create("http://localhost:" + port + "/logon");

        wireMockRule.stubFor(post(urlEqualTo("/logon"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Illegal username password")));
        LogonMonitor logonMonitor = new LogonMonitor(logonUri);
        assertNotNull(logonMonitor);
        assertNotNull(LogonMonitor.ofFormData(bodyData));
        Status status = logonMonitor.postLogon(bodyData);
        assertNotNull(status);
        assertEquals(Status.UNAUTHORIZED, status);
    }

    @Test
    public void unauthenticated() {
        port = wireMockRule.port();
        logonUri = URI.create("http://localhost:" + port + "/logon");

        wireMockRule.stubFor(post(urlEqualTo("/logon"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Illegal username password")));
        LogonMonitor logonMonitor = new LogonMonitor(logonUri);
        assertNotNull(logonMonitor);
        assertNotNull(LogonMonitor.ofFormData(bodyData));
        Status status = logonMonitor.postLogon(bodyData);
        assertNotNull(status);
        assertEquals(Status.UNAUTHORIZED, status);
    }

    @Test
    public void unauthorized() {
        port = wireMockRule.port();
        logonUri = URI.create("http://localhost:" + port + "/logon");

        wireMockRule.stubFor(post(urlEqualTo("/logon"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Unauthorized")));
        LogonMonitor logonMonitor = new LogonMonitor(logonUri);
        assertNotNull(logonMonitor);
        assertNotNull(LogonMonitor.ofFormData(bodyData));
        Status status = logonMonitor.postLogon(bodyData);
        assertNotNull(status);
        assertEquals(Status.UNAUTHORIZED, status);
    }

    @Test
    public void hostNotFound() throws Exception {
        port = wireMockRule.port();
        logonUri = URI.create("http://localhost:" + port + "/logon");

        UnresolvedAddressException uae = new UnresolvedAddressException();
        Throwable unresolvedAddressException = new ConnectException("missing dns");
        unresolvedAddressException.initCause(uae);
        when(mockClient.send(ArgumentMatchers.any(),ArgumentMatchers.any())).thenThrow(unresolvedAddressException);
        LogonMonitor logonMonitor = new LogonMonitor(logonUri, mockClient);
        Status status = logonMonitor.postLogon(bodyData);
        assertNotNull(status);
        assertEquals(Status.UNKNOWN_HOST, status);
    }

    @Test
    public void hostNotResponding() throws Exception {

        HttpConnectTimeoutException hostNotResponding = new HttpConnectTimeoutException("host not responding");
        when(mockClient.send(ArgumentMatchers.any(),ArgumentMatchers.any())).thenThrow(hostNotResponding);
        LogonMonitor logonMonitor = new LogonMonitor(logonUri, mockClient);
        Status status = logonMonitor.postLogon(bodyData);
        assertNotNull(status);
        assertEquals(Status.HOST_UNREACHABLE, status);
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