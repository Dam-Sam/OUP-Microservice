package Common;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.lang.management.ManagementFactory;

public class HealthHttpHandler extends ServiceHttpHandler {

    @Override
    protected boolean handleGet(HttpExchange exchange, String path, String query) throws IOException {
        if(path.endsWith("up")) {
            sendResponse(exchange, 200, "UP");
        }
        else {
            HealthInfo info = new HealthInfo(ManagementFactory.getRuntimeMXBean().getUptime());
            sendJsonObjectResponse(exchange, 200, info);
        }
        return true;
    }

}
