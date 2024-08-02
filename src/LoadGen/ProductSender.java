package LoadGen;

import Common.Http;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class ProductSender extends ServiceSender {

    public ProductSender(LoadGenConfig config) {
        super(config);
        endpoint = "/product";
        baseUrl = config.getProductServiceBaseUrl();
        directUrl = config.getProductServiceBaseUrl();

    }

    @Override
    public Http.Response sendPost(LoadInstruction instruction) {

        Map<String, Object> body = new HashMap<>();
        body.put("command", instruction.getCommand());
        String[] values = instruction.getValues();
        body.put("id", values[0]);


        switch (instruction.getCommand() ) {
            case "create":
            case "delete":
                body.put("name", values[1]);
                //body.put("description", instruction.getValues()[2]);
                body.put("description", "Description for product id " + values[0] + " provided by LoadGen!");
                body.put("price", values[2]);
                body.put("quantity", values[3]);
                break;
            case "update":
                for (int i = 1; i < values.length; i++) {
                    String[] valSplit = values[i].split(":", 2);
                    if(valSplit.length == 1)
                        throw new IllegalArgumentException("Invalid parameter " + values[i]);

                    body.put(valSplit[0].strip(), valSplit[1].strip());
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid command for PRODUCT: " + instruction.getCommand());
        }

        try {
            String jsonBody = new ObjectMapper().writeValueAsString(body);
            return Http.postJson(baseUrl, endpoint, jsonBody, instruction.getHeaders());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

}
