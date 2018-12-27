package io.sugo.server.http;


import io.sugo.common.guice.GuiceManager;
import io.sugo.common.modules.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AntServer {

    private static final Logger log = LogManager.getLogger(AntServer.class);
    public static int port;

    public static void main(String[] args) throws Exception {
        GuiceManager guiceManager = new GuiceManager.Builder()
                .addModule(new CommonModule(args.length > 0 ? args[0] : "src/main/resources/config/"))
                .addModule(new CacheModule())
                .addModule(new JacksonModule())
                .addModule(new LifecycleModule())
                .addModule(new JettyServerModule())
                .addModule(new HiveModule())
                .addModule(new PathAnalysisModule())
                .build();
        log.info(String.format("Initialized [%d] Guice Modules", guiceManager.getModules().size()));
        ServerRunnable serverRunnable = new ServerRunnable(guiceManager.getInjector());
        serverRunnable.run();
    }
}
