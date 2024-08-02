package Common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceConfig {

    private static String ROOT_CONFIG = "root";
    private int port;
    private String host;
    private String dbHost;
    private String dbUsername;
    private String dbPassword;
    private int requestQueueLength;
    private int cacheType;
    private int logLevel = 1;
    private int httpClientTimeout = 60;
    private int connectionPoolSize;
    private String lan;
    public static final int MAX_DEP_RETRIES = 10;

    public ServiceConfig() {
    }

    public ServiceConfig(int port, String host, String dbHost, String dbUsername, String dbPassword, int requestQueueLength,
                         int cacheType, int logLevel, int httpClientTimeout, int connectionPoolSize, String lan) {
        this.port = port;
        this.host = host;
        this.dbHost = dbHost;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.requestQueueLength = requestQueueLength;
        this.cacheType = cacheType;
        this.logLevel = logLevel;
        this.httpClientTimeout = httpClientTimeout;
        this.connectionPoolSize = connectionPoolSize;
        this.lan = lan;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getDbHost() {
        return dbHost;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }



    public int getRequestQueueLength() {
        return requestQueueLength;
    }

    public boolean getUseCache() {
        return cacheType > 0;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public static String getConfig(String configPath, String serviceName) {
        return getConfig(configPath, serviceName, false);
    }

    public static String getConfig(String configPath, String serviceName, boolean noroot) {
        try {
            ObjectMapper om = new ObjectMapper();
            String configFileContent = Files.readString(Paths.get(configPath));
            Map<String, Map<String, Object>> source = om.readValue(configFileContent, new TypeReference<>() {
            });

            Map<String, Object> target = noroot ? new HashMap<>() : new HashMap<>(source.get("root"));

            // Get the service map and merge it into the target map
            Map<String, Object> serviceMap = source.get(serviceName);
            if (serviceMap != null) {
                target.putAll(serviceMap);
            }
            return om.writeValueAsString(target);
        } catch (Exception e) {
            Util.Logger.Log(e);
        }
        return null;
    }

    public int getHttpClientTimeout() {
        return httpClientTimeout;
    }

    public int getCacheType() {
        return cacheType;
    }

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    @Override
    public String toString() {
        return "ServiceConfig{" +
                "port=" + port +
                ", host='" + host + '\'' +
                ", dbHost='" + dbHost + '\'' +
                ", dbUsername='" + dbUsername + '\'' +
                ", dbPassword='" + dbPassword + '\'' +
                ", requestQueueLength=" + requestQueueLength +
                ", cacheType=" + cacheType +
                ", logLevel=" + logLevel +
                ", httpClientTimeout=" + httpClientTimeout +
                ", connectionPoolSize=" + connectionPoolSize +
                '}';
    }

    public String getLan() {
        return lan;
    }
}