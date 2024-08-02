package OrderService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCommand {

    private String command;
    private int user_id;
    private int product_id;
    private int quantity;

    public OrderCommand(String command, int user_id, int product_id, int quantity) {
        this.command = command;
        this.user_id = user_id;
        this.product_id = product_id;
        this.quantity = quantity;
    }


    public OrderCommand() {
    }

    public String getCommand() {
        return command;
    }

    public int getUser_id() {
        return user_id;
    }

    public int getProduct_id() {
        return product_id;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "PlaceOrderDetails{" +
                "command='" + command + '\'' +
                ", user_id=" + user_id +
                ", product_id=" + product_id +
                ", quantity=" + quantity +
                '}';
    }

    public OrderStatus toOrderStatus(String status) {
        return new OrderStatus(-1, this.getProduct_id(), this.getUser_id(), this.getQuantity(), status);
    }
}
