package Common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

public abstract class ServiceHttpHandler implements HttpHandler {
    public static final String HEADER_X_SIM = "X-Load-Simulate";
        public void handle(HttpExchange exchange) {
            Headers headers = exchange.getRequestHeaders();
            if(headers.containsKey(HEADER_X_SIM)) {
                int status = Integer.parseInt(headers.get(HEADER_X_SIM).getFirst());
                try {
                    sendResponse(exchange, status, "SIMULATED RESPONSE " + HEADER_X_SIM);
                    Util.Logger.LogInfo("SIMULATED RESPONSE - " + exchange.getRequestMethod() + "-" + exchange.getRequestURI());
                } catch (IOException e) {
                    Util.Logger.Log("Exception during response simulation", e);
                }
                return;
            }

            URI requestUri = exchange.getRequestURI();
            String method = exchange.getRequestMethod();

            Util.Logger.Log(Util.Logger.INFO, "%s %s%n", method, requestUri);

            boolean handled = false;
            String path = requestUri.getPath();
            if(path.contains("?"))
                path = path.split("\\?")[0];
            String query = requestUri.getQuery();

            try{

                if (method.equals("POST")) {
                    if(path.contains("/wipe")) {
                        handled = handleWipe(exchange, path);
                    }
                    else {
                        byte[] bodyAsBytes = exchange.getRequestBody().readAllBytes();
                        String command = getCommand(bodyAsBytes);
                        String requestBody =  new String(bodyAsBytes);
                        //System.out.println("Request Body:\n" + requestBody);
                        handled = handlePost(exchange, path, query, command, requestBody);
                    }
                } else if (method.equals("GET")) {
                    handled = handleGet(exchange, path, query);
                }

                if(!handled){
                    // Handle invalid HTTP method
                    String response = String.format("Invalid HTTP method. %s not accepted.", method);
                    sendResponse(exchange, 405, response);

                }
            }
            catch (Exception e) {
                Util.Logger.Log(String.format(
                        "An exception occurred while handling this request\nRequest URI: %s\nMethod: %s",
                        requestUri, method), e);
            }

        }

    protected boolean handleWipe(HttpExchange exchange, String path) throws IOException {
            return false;
    }

    protected boolean handleGet(HttpExchange exchange, String path, String query) throws IOException{
        return false;
    }


    protected boolean handlePost(HttpExchange exchange, String path, String query, String command, String messageBody)
            throws IOException{
        return false;
    }

    private String getCommand(byte[] messageBody) {
        try {
            Map<String, Object> json = jsonToMap(messageBody);
            return (String)json.get("command");
        } catch (IOException e) {
            throw new RuntimeException(e); //TODO: Handle correctly
        }
    }

    private String getId(byte[] messageBody) {
        try {
            Map<String, Object> json = jsonToMap(messageBody);
            return (String)json.get("id");
        } catch (IOException e) {
            throw new RuntimeException(e); //TODO: Handle correctly
        }
    }

    protected Object[] getCommandAndId(String messageBody) {
        try {
            Map<String, Object> json = jsonToMap(messageBody);
            Object[] cmd_id = new Object[2];
            cmd_id[0] = String.valueOf(json.get("command"));
            cmd_id[1] = Integer.parseInt(String.valueOf(json.get("id")));
            return cmd_id;
        } catch (IOException e) {
            throw new RuntimeException(e); //TODO: Handle correctly
        }
    }



    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
//        exchange.getResponseHeaders().set("Cache-Control", "max-age=1");
        exchange.sendResponseHeaders(statusCode, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        Util.Logger.LogTrace("%s %s -> %d - %s", exchange.getRequestMethod(), exchange.getRequestURI(),
                statusCode, response);
    }

    protected void sendJsonObjectResponse(HttpExchange exchange, int statusCode, Object responseObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(responseObject);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, statusCode, json);
    }

    protected void sendJsonObjectResponse(HttpExchange exchange, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, statusCode, "{}");
    }

    protected Map<String, Object> jsonToMap(byte[] messageBody) throws IOException {
        return new ObjectMapper().readValue(messageBody, new TypeReference<>() {});
    }

    protected Map<String, Object> jsonToMap(String messageBody) throws IOException {
        return new ObjectMapper().readValue(messageBody, new TypeReference<>() {});
    }


}
