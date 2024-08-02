package Common;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class TestHandler extends ServiceHttpHandler {
    @Override
    protected boolean handleGet(HttpExchange exchange, String path, String query) throws IOException {
        sendResponse(exchange, 200, "OK");
        return true;
    }


}
