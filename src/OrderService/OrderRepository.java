package OrderService;

import Common.DatabaseRepository;
import Common.ServiceConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository extends DatabaseRepository{


    public OrderRepository(ServiceConfig config) {
        super(config);
    }

    @Override
    protected String getDropTableDDL() {
        return "DROP TABLE IF EXISTS public.orders;";
    }

    @Override
    protected String getCreateTableDDL() {
        return
                "CREATE TABLE IF NOT EXISTS public.orders\n" +
                        "(\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    product_id integer NOT NULL,\n" +
                        "    user_id integer NOT NULL,    \n" +
                        "\tquantity integer NOT NULL\n" +
                        ");";
    }


    public Order placeOrder(Order order) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO orders (product_id, user_id, quantity) VALUES (?, ?, ?) RETURNING id;")) {

            statement.setInt(1, order.getProduct_id());
            statement.setInt(2, order.getUser_id());
            statement.setInt(3, order.getQuantity());

//            System.out.println("Placing order... Executing SQL:");
//            System.out.println(statement);

            try (ResultSet rs = statement.executeQuery()) {
                // Retrieve the generated id.
                if (rs.next()) {
                    order.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage() + "\n");
            throw new RuntimeException(e);
        }
        return order;
    }

    public List<UserProductPuchase> getProductTotalPurchases(int userId) {

        String purchasedSQL = "SELECT product_id, SUM(quantity) AS total_quantity\n" +
                "FROM orders\n" +
                "WHERE user_id = ?\n" +
                "GROUP BY product_id;";

        List<UserProductPuchase> purchases = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(purchasedSQL);
             ) {

            selectStmt.setInt(1, userId);

//            System.out.println("Getting product purchase totals. SQL:");
//            System.out.println(selectStmt);

            ResultSet rs = selectStmt.executeQuery();

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int totalQuantity = rs.getInt("total_quantity");
                purchases.add(new UserProductPuchase(productId, totalQuantity));
            }

        } catch (SQLException e) {
            System.out.println("Error getting purchases");
            System.out.println(e);
            return new ArrayList<>();
        }

        return purchases;
    }


}
