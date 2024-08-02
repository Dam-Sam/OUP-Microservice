package ProductService;

import Common.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductCommand extends Product {
    private String command;
    private Map<String, Object> map;

    public ProductCommand(String command, int id, String name, String description, float price, int quantity) {
        super(id, name, description, price, quantity);
        this.command = command;
    }

    public ProductCommand() {
    }


    public String getCommand() {
        return command;
    }

    public Product toProduct() {
        return new Product(id, name, description, price, quantity);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}
