package LoadGen;

import Common.Http;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class OrderSender extends ServiceSender {

    public OrderSender(LoadGenConfig config) {
        super(config);
        baseUrl = config.getOrderServiceBaseUrl();
        directUrl = config.getOrderServiceBaseUrl();
        endpoint = "/order";
    }


    @Override
    public Http.Response sendPost(LoadInstruction instruction) {

        endpoint = "/order";

        Map<String, Object> body = new HashMap<>();
        body.put("command", instruction.getCommand());
        String[] values = instruction.getValues();


        switch (instruction.getCommand()) {
            case "place order":
                body.put("user_id", values[0]);
                body.put("product_id", values[1]);
                body.put("quantity", values[2]);
                break;
            default:
                throw new IllegalArgumentException("Invalid POST command for ORDER service: " + instruction.getCommand());
        }
        try {
            String jsonBody = new ObjectMapper().writeValueAsString(body);
            return Http.postJson(baseUrl, endpoint, jsonBody, instruction.getHeaders());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Http.Response sendGet(LoadInstruction instruction) {
        endpoint = "/user/purchased";
        return Http.get(baseUrl, endpoint + "/" + instruction.getValues()[0], instruction.getHeaders());
    }
}
