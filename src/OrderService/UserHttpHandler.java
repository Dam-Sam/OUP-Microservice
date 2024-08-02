package OrderService;

import Common.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

class UserHttpHandler extends ServiceHttpHandler {
    private final OrderRepository orderRepository;
    private final OrderServiceConfig config;
    private final Cache<User> cache;


    public UserHttpHandler(OrderRepository orderRepository, OrderServiceConfig config, Cache<User> cache) {
        this.orderRepository = orderRepository;
        this.config = config;

        this.cache = cache;
    }


    @Override
    protected boolean handlePost(HttpExchange exchange, String path, String query, String command, String messageBody) throws IOException {

        Http.Response response = Http.postJson(config.getUserServiceBaseUrl(), path, messageBody);
        sendResponse(exchange, response.getCode(), response.getBody());

//        if(response.getCode() == 200) {
//            if (command.equals("create")) {
//                try {
//                    User u = User.from(response.getBody());
//                    cache.updateItem(u.getId(), u);
//                } catch (JsonProcessingException e) {
//                    Util.Logger.Log("Could not cache new user", e);
//                }
//            }
//        }
        return true;
    }

    @Override
    protected boolean handleWipe(HttpExchange exchange, String path) throws IOException {
        return false;
    }

    protected boolean handleGet(HttpExchange exchange, String path, String query) throws IOException {
        Http.Response response = Http.get(config.getUserServiceBaseUrl(), path);
        sendResponse(exchange, response.getCode(), response.getBody());
        return true;
    }




}

