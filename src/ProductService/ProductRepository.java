package ProductService;

import Common.DatabaseRepository;
import Common.Product;
import Common.ServiceConfig;
import Common.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductRepository extends DatabaseRepository {

    public ProductRepository(ServiceConfig serviceConfig) {
        super(serviceConfig);
    }

    protected String getDropTableDDL() {
        return "DROP TABLE IF EXISTS public.products;";
    }

    @Override
    protected String getCreateTableDDL() {
        return
                """
                        CREATE TABLE IF NOT EXISTS public.products
                        (
                            id integer NOT NULL,
                            name character varying(100) COLLATE pg_catalog."default",
                            description character varying(100) COLLATE pg_catalog."default",   \s
                        \tprice character varying(100) COLLATE pg_catalog."default",
                        \tquantity integer,
                            CONSTRAINT products_pkey PRIMARY KEY (id)
                        );""";
    }


    public void createProduct(Product product) throws SQLException {

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO products (id, name, description, price, quantity) VALUES (?, ?, ?, ?, ?)")) {

            statement.setInt(1, product.getId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getDescription());
            statement.setFloat(4, product.getPrice());
            statement.setInt(5, product.getQuantity());

            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating product: " + e.getMessage());
            throw e;
        }
    }

    public Product getProductById(int productId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM products WHERE id = ?")
        ) {
            statement.setInt(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Product found, create a User object
                    return new Product(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getFloat("price"),
                            resultSet.getInt("quantity"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }

        return null;
    }

    public boolean doesProductExist(int productId) {
        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement("SELECT id FROM products WHERE id = ?")) {
            statement.setInt(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }
        return false;
    }

    public int getQuantity(int productId) {
        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement("SELECT SUM(quantity) FROM products WHERE id = ?")) {
            statement.setInt(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            Util.Logger.Log("Error getting product quantity", e);
        }
        return 0;
    }

    public Product updateProductById(Product product) throws SQLException {
        Util.Logger.LogInfo("Updating product:\n " + product);

        try (Connection connection = getConnection()) {
            StringBuilder queryBuilder = new StringBuilder("UPDATE products SET ");
            List<Object> params = new ArrayList<>();

            int updateCount = 0;
            if (!product.getName().isEmpty()) {
                queryBuilder.append("name = ?");
                params.add(product.getName());
                updateCount++;
            }
            if (!product.getDescription().isEmpty()) {
                if (updateCount > 0)
                    queryBuilder.append(", ");
                queryBuilder.append("description = ?");
                params.add(product.getDescription());
                updateCount++;
            }
            if (product.getPrice() > 0) {
                if (updateCount > 0)
                    queryBuilder.append(", ");
                queryBuilder.append("price = ?");
                params.add(product.getPrice());
                updateCount++;
            }
            if (product.getQuantity() != -1) {
                if (updateCount > 0)
                    queryBuilder.append(", ");
                queryBuilder.append("quantity").append(" = ?");
                params.add(product.getQuantity());
                updateCount++;
            }

            if (updateCount == 0) {
                return null;
            }

            queryBuilder.append(" WHERE id = ? RETURNING *;");
            params.add(product.getId());

            String baseQuery = queryBuilder.toString();

            try (PreparedStatement statement = connection.prepareStatement(baseQuery)) {
                for (int i = 1; i <= params.size(); i++) {
                    statement.setObject(i, params.get(i - 1));
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Product found, create a User object
                        Util.Logger.Log(Util.Logger.INFO, "Product with ID %d updated successfully.", product.getId());

                        return new Product(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("description"),
                                resultSet.getFloat("price"),
                                resultSet.getInt("quantity"));

                    } else {
                        Util.Logger.LogInfo("Product with ID " + product.getId() + " not found or no changes were made.");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Error updating product by ID: " + e.getMessage());
            }
        }
        return null;
    }

    public boolean deleteProduct(ProductCommand productCommand) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM products WHERE id = ? AND name = ? AND description = ? AND quantity = ?");

            statement.setInt(1, productCommand.getId());
            statement.setString(2, productCommand.getName());
            statement.setString(3, productCommand.getDescription());
            // statement.setFloat(4, productCommand.getPrice());
            statement.setInt(4, productCommand.getQuantity());

            //System.err.println(statement);
            int rowsAffected = statement.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            // System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }


    private Map<Integer, Product> loadProductData() {
        Map<Integer, Product> productMap = new HashMap<>();
        try (Connection con = getConnection()) {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM products");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    Float price = resultSet.getFloat("price");
                    int quantity = resultSet.getInt("quantity");

                    Product productH = new Product(id, name, description, price, quantity);
                    productMap.put(id, productH);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
        //If loading fails or the file is not found, return a new HashMap
        return productMap;
    }
}
