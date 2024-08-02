package OrderService;

import Common.ServiceConfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderServiceConfig extends ServiceConfig {

    private String userServiceBaseUrl;
    private String productServiceBaseUrl;
    public OrderServiceConfig() {
    }

    public OrderServiceConfig(int port, String host, String dbHost, String dbUsername, String dbPassword,
                              String userServiceBaseUrl, String productServiceBaseUrl, int requestQueueLength,
                              int cacheType, int logLevel, int httpClientTimeout,
                              int connectionPoolSize, String lan) {
        super(port, host, dbHost, dbUsername, dbPassword, requestQueueLength, cacheType, logLevel, httpClientTimeout,
                connectionPoolSize, lan);
        this.userServiceBaseUrl = userServiceBaseUrl;
        this.productServiceBaseUrl = productServiceBaseUrl;
    }

    public String getUserServiceBaseUrl() {
        return userServiceBaseUrl;
    }

    public String getProductServiceBaseUrl() {
        return productServiceBaseUrl;
    }

    @Override
    public String toString() {
        return "OrderServiceConfig{" +
                "userServiceBaseUrl='" + userServiceBaseUrl + '\'' +
                ", productServiceBaseUrl='" + productServiceBaseUrl + '\'' +
                "} " + super.toString();
    }
}
