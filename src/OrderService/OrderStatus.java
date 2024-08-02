package OrderService;

public class OrderStatus extends Order {

    private String status;
    public OrderStatus(int id, int product_id, int user_id, int quantity, String status) {
        super(id, product_id, user_id, quantity);
        this.status = status;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
