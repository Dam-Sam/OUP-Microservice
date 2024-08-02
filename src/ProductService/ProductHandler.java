package ProductService;


import Common.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.SQLException;

class ProductHandler extends ServiceHttpHandler {

    private final ServiceConfig config;
    private ProductRepository productRepository;

    private Cache<Product> cache;


    public ProductHandler(ServiceConfig config, ProductRepository productRepository, Cache<Product> cache) {
        this.config = config;
        this.productRepository = productRepository;
        this.cache = cache;
    }

    @Override
    protected boolean handleWipe(HttpExchange exchange, String path) throws IOException {
        productRepository.resetDB();
        if(cache != null)
            cache.resetCache();
        sendJsonObjectResponse(exchange, 200, new StatusMessage("Wiped database and cache"));
        return true;
    }

    @Override
    protected boolean handleGet(HttpExchange exchange, String path, String query) throws IOException {
        try {
            String[] pathParts = path.split("/");
            int productId = Integer.parseInt(pathParts[pathParts.length-1]);

            Product product = getProductById(productId);

            if (product != null) {
                sendJsonObjectResponse(exchange, 200, product);
            } else {
                sendResponse(exchange, 404, "Product not found");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Invalid product ID format");
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal Server Error");
        }
        return true;

    }

    private Product getProductById(int id) {
        Product product = cache.getItem(id);
        if(product != null)
            return product;

        product = productRepository.getProductById(id);
        if(product != null)
            cache.updateItem(id, product);
        return product;
    }

    @Override
    protected boolean handlePost(HttpExchange exchange, String path, String query, String command, String messageBody) throws IOException {

        ProductCommand productCommand = new ObjectMapper().readValue(messageBody, ProductCommand.class);


        switch (command) {
            case "create":
                handleCreate(exchange, productCommand);
                break;
            case "update":
                handleUpdate(exchange, productCommand);
                break;
            case "delete":
                handleDelete(exchange, productCommand);
                break;
            default:
                sendJsonObjectResponse(exchange, 400, new StatusMessage("Invalid command"));
                break;
        }
        return true;
    }


    private void handleCreate(HttpExchange exchange, ProductCommand productCommand) throws IOException {
        try {
            if (!hasAllCreateFields(productCommand)) {
                sendJsonObjectResponse(exchange, 400);
                return;
            }
            
            productRepository.createProduct(productCommand);
            cache.updateItem(productCommand.getId(), productCommand.toProduct());
            sendJsonObjectResponse(exchange, 200, productCommand);
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            if (e.getSQLState().equals("23505")) {
                cache.removeItem(productCommand.getId());
                sendJsonObjectResponse(exchange, 409);
                //Just in case it is different. Could be implemented better.
            } else {
                sendJsonObjectResponse(exchange, 400);
            }
        } catch (Exception e) {
            sendJsonObjectResponse(exchange, 500, new StatusMessage("Error creating product.\n" + e.getMessage()));
        }
    }

    private void handleUpdate(HttpExchange exchange, ProductCommand productCommand) throws IOException {
        try {
            if (!validForUpdate(productCommand)) {
                sendJsonObjectResponse(exchange, 400);
                return;
            }

            Product updated = productRepository.updateProductById(productCommand);
            if (updated != null) {
                sendJsonObjectResponse(exchange, 200, updated);
                cache.updateItem(updated.getId(), updated);
            } else {
                sendJsonObjectResponse(exchange, 404, new StatusMessage("Product ID not found"));
                cache.removeItem(productCommand.getId());
            }
        } catch (Exception e) {
            sendJsonObjectResponse(exchange, 500, new StatusMessage("Error updating product.\n" + e.getMessage()));
        }
    }

    private boolean doesProductExist(int productId) {
        if(cache.hasItem(productId))
            return true;
        return productRepository.doesProductExist(productId);
    }

    private void handleDelete(HttpExchange exchange, ProductCommand productCommand) throws IOException {
        try {
            if (!hasAllFields(productCommand)) {
                sendJsonObjectResponse(exchange, 400);
                return;
            }
            if (productRepository.deleteProduct(productCommand)) {
                sendResponse(exchange, 200, "");
                cache.removeItem(productCommand.getId());
            } else {
                sendJsonObjectResponse(exchange, 404, new StatusMessage("Product not found or no changes were made"));
                cache.removeItem(productCommand.getId());
            }
        } catch (Exception e) {
            sendJsonObjectResponse(exchange, 500, new StatusMessage("Error deleting product.\n" + e.getMessage()));
        }
    }

    private boolean hasAllCreateFields(ProductCommand productCommand) {
        if ( productCommand.getId() == 0 )
            return false;
        return !productCommand.getName().isBlank()
//                && productCommand.getDescription() != ""
                && productCommand.getPrice() != -1
                && productCommand.getQuantity() != -1;
    }

    private boolean hasAllFields(ProductCommand productCommand) {
        if ( productCommand.getId() == 0 )
            return false;
        return !productCommand.getName().isBlank()
//                && productCommand.getDescription() != ""
                && productCommand.getPrice() != -1
                && productCommand.getQuantity() != -1;
    }

    private boolean validForUpdate (ProductCommand productCommand) {
        if ( productCommand.getId() < 1 )
            return false;
        return !productCommand.getName().isBlank()
                || !productCommand.getDescription().isBlank()
                || productCommand.getPrice() != -1
                || productCommand.getQuantity() != -1;
    }




}
