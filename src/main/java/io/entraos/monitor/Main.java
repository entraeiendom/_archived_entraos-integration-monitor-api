package io.entraos.monitor;

import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.web.context.ContextLoaderListener;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static no.cantara.config.ServiceConfig.getProperty;
import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings("restriction")
public class Main {
    private static final Logger log = getLogger(Main.class);

    public static final String CONTEXT_PATH = "/monitor";
    private static final String LOGS_NAME = "logs";
    public static final int DEFAULT_PORT = 8080;


    private Integer webappPort;
    private Server server;


    public Main() {
        this.server = new Server();
    }

    public Main withPort(Integer webappPort) {
        this.webappPort = webappPort;
        return this;
    }

    public static void main(String[] args) {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().getLogger("").setLevel(Level.INFO);

        Integer webappPort = DEFAULT_PORT;
        String port = getProperty("server_port");
        if (port != null) {
            try {
               webappPort = Integer.valueOf(port);
            } catch (NumberFormatException nfe) {
                log.warn("Failed to create port as integer from {}. Using default port: {}", port, webappPort);
            }
        }

        try {

            final Main main = new Main().withPort(webappPort);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    log.debug("ShutdownHook triggered. Exiting basicauthapplication");
                    main.stop();
                }
            });

            main.start();
            log.debug("Finished waiting for Thread.currentThread().join()");
            main.stop();
        } catch (RuntimeException e) {
            log.error("Error during startup. Shutting down ConfigService.", e);
            System.exit(1);
        }
    }

    // https://github.com/psamsotha/jersey-spring-jetty/blob/master/src/main/java/com/underdog/jersey/spring/jetty/JettyServerMain.java
    public void start() {
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath(CONTEXT_PATH);

        ResourceConfig jerseyResourceConfig = new ResourceConfig();
        jerseyResourceConfig.packages("io.entraos.monitor");
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(jerseyResourceConfig));
        context.addServlet(jerseyServlet, "/*");

        context.addEventListener(new ContextLoaderListener());

        context.setInitParameter("contextConfigLocation", "classpath:context.xml");

        ServerConnector connector = new ServerConnector(server);
        if (webappPort != null) {
            connector.setPort(webappPort);
        }
        NCSARequestLog requestLog = buildRequestLog();
        server.setRequestLog(requestLog);
        server.addConnector(connector);
        server.setHandler(context);

        try {
            ensureLogsDirExist();
            server.start();
        } catch (Exception e) {
            log.error("Error during Jetty startup. Exiting", e);
            // "System. exit(2);"
        }
        webappPort = connector.getLocalPort();
        String baseUri = "http://localhost:" + webappPort + "/" + CONTEXT_PATH + "/";
        log.info("integration monitor started on {}", baseUri);
        log.info("    Status on {}status", baseUri);
        log.info("    Health on {}health", baseUri);

        try {
            server.join();
        } catch (InterruptedException e) {
            log.error("Jetty server thread when join. Pretend everything is OK.", e);
        }
    }

    void ensureLogsDirExist() {
        File directory = new File(LOGS_NAME);
        if (! directory.exists()){
            directory.mkdir();
        }
    }

    private NCSARequestLog buildRequestLog() {
        NCSARequestLog requestLog = new NCSARequestLog("logs/jetty-yyyy_mm_dd.request.log");
        requestLog.setAppend(true);
        requestLog.setExtended(true);
        requestLog.setLogTimeZone("GMT");

        return requestLog;
    }




    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            log.warn("Error when stopping Jetty server", e);
        }
    }

    public int getPort() {
        return webappPort;
    }

    public boolean isStarted() {
        return server.isStarted();
    }
}
