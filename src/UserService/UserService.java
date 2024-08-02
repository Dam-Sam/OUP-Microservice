package UserService;

import Common.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


public class UserService extends MicroserviceBase {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            System.err.println("Usage: java UserService <config_file_path>");
            System.exit(1);
        }

        ServiceConfig config = getConfigAndInitialize(args, "UserService", ServiceConfig.class);

        if (!checkDependencies(config, null, null, null))
            System.exit(10);

        UserRepository userRepository = new UserRepository(config);
        Cache<User> userCache = createCache(config, "UserService", config.getHost(), 5700, false,  "users");

        HttpServer server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
        addRoute(server, "/user", new UserHandler(config, userRepository, userCache));
        addRoute(server, "/wipe", new UserHandler(config, userRepository, userCache));
        addCommonRoutes(server, config);

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();

        printBindAddress(server);
        printConfigValues(config);

        String baseUrl = "http://" + config.getHost() + ":" + String.valueOf(config.getPort()) + "/user";
        startUserInterface(server, baseUrl, userRepository, () -> {

        });

    }

}
