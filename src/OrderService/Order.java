package OrderService;

import Common.Entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order extends Entity {
    private int id = -1;
    private int product_id;
    private int user_id;
    private int quantity;

    public Order(int id, int product_id, int user_id, int quantity) {
        this.id = id;
        this.product_id = product_id;
        this.user_id = user_id;
        this.quantity = quantity;
    }

    public Order(int product_id, int user_id, int quantity) {
        this.product_id = product_id;
        this.user_id = user_id;
        this.quantity = quantity;
    }

    public Order() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProduct_id() {
        return product_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", product_id=" + product_id +
                ", user_id=" + user_id +
                ", quantity=" + quantity +
                '}';
    }

    public OrderStatus toOrderStatus(String status) {
        return new OrderStatus(this.getId(), this.getProduct_id(), this.getUser_id(), this.getQuantity(), status);
    }

    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(id);
        out.writeInt(product_id);
        out.writeInt(user_id);
        out.writeInt(quantity);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        id = in.readInt();
        product_id = in.readInt();
        user_id = in.readInt();
        quantity = in.readInt();
    }
}
