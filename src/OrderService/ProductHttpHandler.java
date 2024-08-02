package OrderService;

import Common.Cache;
import Common.Http;
import Common.ServiceHttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

class ProductHttpHandler extends ServiceHttpHandler {

    private final OrderServiceConfig config;
    private final Cache cacheManager;


    public ProductHttpHandler(OrderServiceConfig config, Cache cacheManager) {
        this.config = config;
        this.cacheManager = cacheManager;
    }

    @Override
    protected boolean handlePost(HttpExchange exchange, String path, String query, String command, String messageBody) throws IOException {
        Http.Response response = Http.postJson(config.getProductServiceBaseUrl(), path, messageBody);
        sendResponse(exchange, response.getCode(), response.getBody());

       // if(response.getCode() == 200) {
            //Object[] cmd_id = getCommandAndIdAndQty(messageBody);
//            if (cmd_id[0].equals("create")) {
//                cacheManager.addProduct((int) cmd_id[1], (int) cmd_id[2]);
//            }
        //}

        return true;
    }

    @Override
    protected boolean handleWipe(HttpExchange exchange, String path) throws IOException {
        return false;
    }

    protected boolean handleGet(HttpExchange exchange, String path, String query) throws IOException {
        Http.Response response = Http.get(config.getProductServiceBaseUrl(), path);
        sendResponse(exchange, response.getCode(), response.getBody());
        return true;
    }

    protected Object[] getCommandAndIdAndQty(String messageBody) {
        try {
            Map<String, Object> json = jsonToMap(messageBody);
            Object[] cmd_id_qty = new Object[3];
            cmd_id_qty[0] = String.valueOf(json.get("command"));
            cmd_id_qty[1] = Integer.parseInt(String.valueOf(json.get("id")));
            cmd_id_qty[2] = Integer.parseInt(String.valueOf(json.get("quantity")));
            return cmd_id_qty;
        } catch (IOException e) {
            throw new RuntimeException(e); //TODO: Handle correctly
        }
    }


}

