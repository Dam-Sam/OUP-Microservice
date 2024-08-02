package LoadGen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadGenConfig {

    private String userServiceBaseUrl;

    private String productServiceBaseUrl;

    private String orderServiceBaseUrl;

    private int mode = 0; //0 = use load file, 1 = auto

    private String loadFile = "";
    private String[] loadFiles = new String[0];

    private int maxParallelRequests = 20;
    private int httpClientTimeout = 60;
    private int logLevel = 2;


    public LoadGenConfig(String userServiceBaseUrl, String productServiceBaseUrl, String orderServiceBaseUrl,
                         int mode, String loadFile, String[] loadFiles, int maxParallelRequests, int httpClientTimeout, int logLevel) {
        this.userServiceBaseUrl = userServiceBaseUrl;
        this.productServiceBaseUrl = productServiceBaseUrl;
        this.orderServiceBaseUrl = orderServiceBaseUrl;
        this.mode = mode;
        this.loadFile = loadFile;
        this.loadFiles = loadFiles;
        this.maxParallelRequests = maxParallelRequests;
        this.httpClientTimeout = httpClientTimeout;
        this.logLevel = logLevel;
    }

    public LoadGenConfig() {
    }

    public String getUserServiceBaseUrl() {
        return userServiceBaseUrl;
    }

    public String getProductServiceBaseUrl() {
        return productServiceBaseUrl;
    }

    public String getOrderServiceBaseUrl() {
        return orderServiceBaseUrl;
    }

    public int getMode() {
        return mode;
    }

    public String getLoadFile() {
        return loadFile;
    }

    public int getMaxParallelRequests() {
        return maxParallelRequests;
    }

    public String[] getLoadFiles() {
        return loadFiles;
    }

    public int getHttpClientTimeout() {
        return httpClientTimeout;
    }

    public void setHttpClientTimeout(int httpClientTimeout) {
        this.httpClientTimeout = httpClientTimeout;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }
}
