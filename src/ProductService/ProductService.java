package ProductService;

import Common.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ProductService extends MicroserviceBase {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Check if a command-line argument is provided
        if (args.length != 1) {
            System.err.println("Usage: java ProductService <config_file_path>");
            System.exit(1);
        }

        ServiceConfig config = getConfigAndInitialize(args, "ProductService", ServiceConfig.class);

        if (!checkDependencies(config, null, null, null))
            System.exit(10);

        ProductRepository productRepository = new ProductRepository(config);
        Cache<Product> productCache = createCache(config, "ProductService", config.getHost(), 5701, false, "products");
        HttpServer server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
        addRoute(server, "/product", new ProductHandler(config, productRepository, productCache));
        addRoute(server, "/wipe", new ProductHandler(config, productRepository, productCache));
        addCommonRoutes(server, config);

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        printBindAddress(server);
        printConfigValues(config);

        String baseUrl = "http://" + config.getHost() + ":" + String.valueOf(config.getPort()) + "/product";
        startUserInterface(server, baseUrl, productRepository, () -> {

        });
    }


}
