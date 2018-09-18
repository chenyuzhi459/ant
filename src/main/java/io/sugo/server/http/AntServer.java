package io.sugo.server.http;

import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
//import com.sun.jersey.spi.container.servlet.ServletContainer;
import io.sugo.common.guice.GuiceManager;
import io.sugo.common.module.CommonModule;
import io.sugo.common.module.JacksonModule;
import io.sugo.common.module.JettyServerModule;
import io.sugo.server.http.jetty.JettyMonitoringConnectionFactory;
import io.sugo.server.http.jetty.listener.GuiceServletListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
//import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.DispatcherType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class AntServer {

    private static final Logger LOG = LogManager.getLogger(AntServer.class);
    public static int port;

    private static final AtomicInteger activeConnections = new AtomicInteger();
    public static final CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        ServerRunnable serverRunnable = new ServerRunnable(args.length > 0 ? args[0] : "src/main/resources/config/");
        serverRunnable.run();
//        GuiceManager guiceManager = new GuiceManager.Builder()
//                .addModule(new CommonModule(args.length > 0 ? args[0] : "src/main/resources/config/"))
//                .addModule(new JettyServerModule())
//                .addModule(new JacksonModule())
//                .build();
//        Configure configure = guiceManager.getInstance(Configure.class);
//        port = configure.getInt("system.properties","http.port");
//
//        LOG.info("initializing server");
//        Server server = null;
//        try {
//            server = makeJettyServer(configure);
//            initialize(server, guiceManager);
//            server.start();
//            LOG.info("start...in " + port);
//            latch.await();
//        }catch (Exception e){
//            LOG.error(e);
//        }finally {
//            if (server != null) {
//                server.stop();
//            }
//            LOG.info("server stopped!");
//        }

    }

    private static Server makeJettyServer(Configure configure ) {
        int maxConn = configure.getInt("system.properties","server.max.conn", 200);
        final Server server = new Server(new QueuedThreadPool(maxConn));

        // Without this bean set, the default ScheduledExecutorScheduler runs as non-daemon, causing lifecycle hooks to fail
        // to fire on main exit. Related bug: https://github.com/druid-io/druid/pull/1627
        server.addBean(new ScheduledExecutorScheduler("JettyScheduler", true), true);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        connector.setIdleTimeout(600000);
        // workaround suggested in -
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=435322#c66 for jetty half open connection issues during failovers
        connector.setAcceptorPriorityDelta(-1);

        List<ConnectionFactory> monitoredConnFactories = new ArrayList<>();
        for (ConnectionFactory cf : connector.getConnectionFactories()) {
            monitoredConnFactories.add(new JettyMonitoringConnectionFactory(cf, activeConnections));
        }
        connector.setConnectionFactories(monitoredConnFactories);

        server.setConnectors(new Connector[]{connector});

        return server;
    }

    private static void initialize(Server server, GuiceManager guiceManager) {

        final ServletContextHandler apiHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        apiHandler.setContextPath("/");

        ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
        apiHandler.addServlet(servletHolder, "/*");

        // add GuiceFilter to support GuiceServletListener
        apiHandler.addFilter(GuiceFilter.class,"/*", EnumSet.of(DispatcherType.REQUEST));
        apiHandler.addEventListener(new GuiceServletListener(guiceManager.getInjector()));
        HandlerList handlerList = new HandlerList();
        handlerList.addHandler(apiHandler);

        server.setHandler(handlerList);

    }

}
