package LoadGen;

import Common.Http;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class UserSender extends ServiceSender {



    public UserSender(LoadGenConfig config) {
        super(config);
        endpoint = "/user";
        baseUrl = config.getUserServiceBaseUrl();
        directUrl = config.getUserServiceBaseUrl();

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
                body.put("username", values[1]);
                body.put("email", values[2]);
                body.put("password", values[3]);
                break;
            case "update":
                for (int i = 1; i < values.length; i++) {
                    String[] valSplit = values[i].split(":");
                    if(valSplit.length == 1)
                        throw new IllegalArgumentException("Invalid parameter " + values[i]);
                    body.put(valSplit[0].strip(), valSplit[1].strip());
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid command for USER: " + instruction.getCommand());
        }

        try {
            String jsonBody = new ObjectMapper().writeValueAsString(body);
            return Http.postJson(baseUrl, endpoint, jsonBody, instruction.getHeaders());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }


}
