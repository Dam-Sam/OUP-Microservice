package LoadGen;

import Common.Http;

import java.util.Map;
import java.util.Objects;

public abstract class ServiceSender {

    protected final LoadGenConfig config;
    protected String baseUrl;
    protected String directUrl;

    protected String endpoint = "";

    public ServiceSender(LoadGenConfig config) {
        this.config = config;
    }

    public static ServiceSender createSender(String service, LoadGenConfig config){
        switch (service) {
            case "USER":
                return new UserSender(config);
            case "PRODUCT":
                return new ProductSender(config);
            case "ORDER":
                return new OrderSender(config);
            default:
                throw new IllegalArgumentException("Unknown service, cannot create: " + service);
        }
    }

    public Http.Response sendCommand(LoadInstruction instruction) {
//        System.out.println("ServiceSender.sendCommand");
//        System.out.println(instruction);
        if(instruction.getMethod().equals("POST")) {
            if(Objects.equals(instruction.getCommand(), "wipe")) {
                return Http.postJson(directUrl, "/wipe/"+instruction.getService().toLowerCase(), "", instruction.getHeaders());
            } else {
                return sendPost(instruction);
            }

        } else if(instruction.getMethod().equals("GET")) {
            return sendGet(instruction);
        }
        else {
            throw new IllegalArgumentException("Invalid method/command: " + instruction.getMethod());
        }
    }

    public Http.Response sendGet(LoadInstruction instruction) {
        return Http.get(baseUrl, endpoint + "/" + instruction.getValues()[0], instruction.getHeaders());
    }
    public abstract Http.Response sendPost(LoadInstruction instruction);


    public String getBaseUrl() {
        return baseUrl;
    }
}
