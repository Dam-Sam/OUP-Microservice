package OrderService;

import Common.*;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

public class OrderService extends MicroserviceBase {

    public static void main(String[] args) throws IOException, InterruptedException {

        OrderServiceConfig config = getConfigAndInitialize(args, "OrderService", OrderServiceConfig.class);

        if(!checkDependencies(config, config.getUserServiceBaseUrl(), config.getProductServiceBaseUrl(), null))
            System.exit(10);


        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(config.getPort()), config.getRequestQueueLength());
        } catch (IOException e) {
            Util.Logger.Log(e);
            System.exit(50);
        }

        OrderRepository orderRepository = new OrderRepository(config);
        //Cache<Integer> productQtyCache = createCache(config, "OrderService", config.getHost(), 5703, false, "prodQty");
        Cache<Map<Integer, Integer>> userProductQtyCache = createCache(config, "OrderService", config.getHost(), 5703, false, "userProdQty");
        Cache<User> userCache = createCache(config, "UserService", config.getHost(), 5700, true,"users");
        Cache<Product> productCache = createCache(config, "ProductService", config.getHost(), 5701, true, "products");
        Cache<Order> orderCache = createCache(config, "OrderService", config.getHost(), 5701, false, "orders");
        OrderHttpHandler orderHandler = new OrderHttpHandler(orderRepository, config, userProductQtyCache, userCache, productCache, orderCache);
        addRoute(server, "/order", orderHandler);
        addRoute(server, "/user", new UserHttpHandler(orderRepository, config, null));
        addRoute(server, "/user/purchased", orderHandler);
        addRoute(server, "/product", new ProductHttpHandler(config, null));
        addRoute(server, "/wipe", orderHandler);
        addCommonRoutes(server, config);

        // Start the server
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();

        printBindAddress(server);
        printConfigValues(config);

        String baseUrl = "http://" + config.getHost() + ":" + config.getPort() + "/order";
        startUserInterface(server, baseUrl, orderRepository, () -> {
 });

    }








}

