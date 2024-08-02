package Common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Scanner;

public class MicroserviceBase {

    protected static void addCommonRoutes(HttpServer server, ServiceConfig config) {
        addRoute(server, "/health", new HealthHttpHandler());
        addRoute(server, "/test", new TestHandler());
    }

    protected static <T> Cache<T> createCache(ServiceConfig config, String clusterName, String bindIp, int bindPort, boolean clientOnly, String cacheName) {
        return createCache(config.getCacheType(), config, clusterName, bindIp, bindPort, clientOnly, cacheName);
    }

    protected static <T> Cache<T> createCache(int type, ServiceConfig config, String clusterName, String bindIp, int bindPort, boolean clientOnly, String cacheName) {
        String actualBindIP = config.getLan().isBlank() ? bindIp : getLanIp(config);
        if (type > 0) {
            System.out.print("Creating ");
            if (type == 1) {
                System.out.println("MemCache");
            } else {
                Util.Logger.Log(Util.Logger.FORCE, "DistributedCache (HazelCast) - cluster:%s name:%s ip:%s port:%d",
                        clusterName, cacheName, actualBindIP, bindPort);
            }
        }
        return switch (type) {
            case 0 -> new Cache<T>(); // No caching
            case 1 -> new MemCache<T>();
            case 2 -> new DistributedCache<T>(clusterName, actualBindIP, bindPort, clientOnly, cacheName);
            default -> throw new IllegalStateException("Unexpected value: " + config.getCacheType());
        };
    }

    @FunctionalInterface
    public interface ShutdownHandler {
        void handleShutdown();
    }

    protected static void startUserInterface(HttpServer server, String baseUrl, DatabaseRepository respository,
                                             ShutdownHandler shutdownHandler) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println(
                    "Enter a command ('exit', 'x': shutdown server, 'wipe': delete all data fro this service):");
            String input = scanner.nextLine();
            String[] commandPart = input.split(" ");

            if (input.equalsIgnoreCase("exit")
                    || input.equalsIgnoreCase("x")) {
                System.out.println("Shutting server down and exiting. All live connections will be lost.");
                server.stop(0);
                if (shutdownHandler != null)
                    shutdownHandler.handleShutdown();
                break;
            } else if ("wipe".equalsIgnoreCase(input)) {
                // Wipe all data
                respository.resetDB();
            } else {
                System.out.println("Invalid Command, please try again: ");
            }
        }

        scanner.close();
    }


    protected static void addRoute(HttpServer server, String route, HttpHandler httpHandler) {
        System.out.printf("Adding route %s using %s%n", route, httpHandler.getClass());
        server.createContext(route, httpHandler);
    }


    protected static void printBindAddress(HttpServer server) {
        String message = String.format(
                "Server started and accepting connections on :\n%s%s:%d%s",
                Util.ANSI_GREEN, server.getAddress().getAddress().toString(),
                server.getAddress().getPort(), Util.ANSI_RESET
        );
        System.out.println(message);
    }

    protected static ArrayList<String> getNetworkIpAddresses() {
        ArrayList<String> ipAddresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isUp()) continue;

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    ipAddresses.add(inetAddress.toString().substring(1));
                }
            }

        } catch (Exception e) {
            Util.print("Error enumerating network interfaces.", e);
        }
        return ipAddresses;
    }

    public static String getLanIp(ServiceConfig config) {
        ArrayList<String> lanIps = getNetworkIpAddresses();
        for (String ip : lanIps) {
            if (ip.startsWith(config.getLan())) {
                return ip;
            }
        }
        return "127.0.0.1";
    }


    protected static boolean isPostgresUp(ServiceConfig config) {
        try (Connection conn = DriverManager.getConnection(config.getDbHost(), config.getDbUsername(), config.getDbPassword())) {
            return conn != null;
        } catch (SQLException e) {
            return false;
        }
    }

    protected static boolean isPostgresUpStartupCheck(ServiceConfig config) {
        boolean postgresUp = isPostgresUp(config);
        System.out.println("PostgreSQL is... " + (postgresUp ? Util.Green("UP") : Util.Red("DOWN")));
        return postgresUp;
    }

    public static boolean checkDependencies(ServiceConfig dbConfig,
                                            String userServiceBaseUrl,
                                            String productServiceBaseUrl,
                                            String orderServiceBaseUrl) throws InterruptedException {
        Util.println("Performaing health check on dependencies");

        int down = 1;
        int retries = ServiceConfig.MAX_DEP_RETRIES;

        if (dbConfig != null)
            Util.println("Checking PostgreSQL on host " + dbConfig.getDbHost());
        if (userServiceBaseUrl != null && !userServiceBaseUrl.isBlank())
            Util.println("Checking UserService on host " + userServiceBaseUrl);
        if (productServiceBaseUrl != null && !productServiceBaseUrl.isBlank())
            Util.println("Checking ProductService on host " + productServiceBaseUrl);
        if (orderServiceBaseUrl != null && !orderServiceBaseUrl.isBlank())
            Util.println("Checking ProductService on host " + orderServiceBaseUrl);

        while (down > 0 && retries-- > 0) {
            down = 0;

            try {
                if (dbConfig != null)
                    if (!isPostgresUpStartupCheck(dbConfig)) down++;

                if (userServiceBaseUrl != null && !userServiceBaseUrl.isBlank()) {
                    boolean serviceUp = Http.isServiceUp(userServiceBaseUrl);
                    Util.println("UserService is... " + (serviceUp ? Util.Green("UP") : Util.Red("DOWN")));
                    if (!serviceUp) down++;
                }


                if (productServiceBaseUrl != null && !productServiceBaseUrl.isBlank()) {
                    boolean serviceUp = Http.isServiceUp(productServiceBaseUrl);
                    Util.println("ProductService is... " + (serviceUp ? Util.Green("UP") : Util.Red("DOWN")));
                    if (!serviceUp) down++;
                }

                if (orderServiceBaseUrl != null && !orderServiceBaseUrl.isBlank()) {
                    boolean serviceUp = Http.isServiceUp(orderServiceBaseUrl);
                    Util.println("OrderService is... " + (serviceUp ? Util.Green("UP") : Util.Red("DOWN")));
                    if (!serviceUp) down++;
                }
            } catch (Exception e) {
                Util.printlnRed("Exception while checking dependencies: " + e);
                down++;
            }

            {
                if (down > 0) {
                    Util.printlnRed("One or more dependencies are down. Will check again in 3 seconds.");
                    Thread.sleep(3000);
                }
            }
        }

        if (down == 0) {
            Util.printlnGreen("All dependencies up.");
            return true;
        } else {
            Util.printlnRed("One or more dependencies are down after " + ServiceConfig.MAX_DEP_RETRIES
                    + ". Please check your settings and try again.");
            return false;
        }
    }


    protected static <T extends ServiceConfig> T getConfigAndInitialize(String[] args, String serviceName, Class<T> configClass) throws JsonProcessingException {
        String configFileContent = ServiceConfig.getConfig(args[0], serviceName);
        if (configFileContent == null || configFileContent.isBlank()) {
            Util.Logger.Log(Util.Logger.ERROR, "Cannot read config. Terminating service.");
            System.exit(29);
        }

        T config = new ObjectMapper().readValue(configFileContent, configClass);

        ConnectionPool.initialize(config);
        setDefaults(config);
        return config;


    }

    protected static void printConfigValues(ServiceConfig config) {
        System.out.println("HttpClient timout: " + config.getHttpClientTimeout());
        System.out.print("Caching is ");
        if (config.getUseCache()) {
            Util.printlnGreen("ENABLED (type " + config.getCacheType() + ")");
        } else {
            Util.printlnGreen("DISABLED");
        }
        System.out.print("Connection Pool is ");
        if (config.getConnectionPoolSize() > 0) {
            Util.printlnGreen("ENABLED (connections = " + config.getConnectionPoolSize() + ")");
        } else {
            Util.printlnGreen("DISABLED");
        }
        Util.Logger.logLevel = config.getLogLevel();
        System.out.println("Logging is set to level " + Util.Logger.logLevel);
    }

    protected static void setDefaults(ServiceConfig config) {
        Util.Logger.Log(Util.Logger.DEBUG, "CONFIG:\n%s", config);
        Http.setTimeout(config.getHttpClientTimeout());
    }


}
