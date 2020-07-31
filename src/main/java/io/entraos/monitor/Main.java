package io.entraos.monitor;

import io.entraos.monitor.status.StatusResource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings("restriction")
public class Main {
    private static final Logger log = getLogger(Main.class);
    public static void main(String[] args) throws Exception {

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        int port = 8080;
        Server jettyServer = new Server(port);
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);


        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServlet.setInitParameter(
                "jersey.config.server.provider.classnames",
                StatusResource.class.getCanonicalName());


        try {
            jettyServer.start();
            String serverUrl = "http://localhost:" + port + "/";
            log.info("Server started on {}", serverUrl );
            log.info("Status available on {}status", serverUrl);
            jettyServer.join();

        } finally {
            log.info("Shutting down.");
            jettyServer.destroy();
        }

    }
}
