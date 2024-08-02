package OrderService;

import Common.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;

import static Common.Util.Logger;

class OrderHttpHandler extends ServiceHttpHandler {
    private final OrderRepository orderRepository;
    private final OrderServiceConfig config;
    private final Cache<Map<Integer, Integer>> userProdQtyPurchased;

    private final Cache<User> userCache;
    private final Cache<Order> orderCache;

    private final Cache<Product> productCache;

    public OrderHttpHandler(OrderRepository orderRepository, OrderServiceConfig config,
                            Cache<Map<Integer, Integer>> userProdQtyPurchased,
                            Cache<User> userCache, Cache<Product> productCache, Cache<Order> orderCache) {
        this.orderRepository = orderRepository;
        this.config = config;
        this.userProdQtyPurchased = userProdQtyPurchased;
        this.userCache = userCache;
        this.productCache = productCache;
        this.orderCache = orderCache;
    }


    @Override
    protected boolean handlePost(HttpExchange exchange, String path, String query, String command, String messageBody) throws IOException {
        if (command.equalsIgnoreCase("place order")) {
            placeOrder(exchange, messageBody);
        } else {
            String response = String.format("Invalid command for OrderService: %s", command);
            sendResponse(exchange, 405, response);
        }
        return true;
    }

    @Override
    protected boolean handleWipe(HttpExchange exchange, String path) throws IOException {
        orderRepository.resetDB();
        if (userProdQtyPurchased != null)
            userProdQtyPurchased.resetCache();
        sendJsonObjectResponse(exchange, 200, new StatusMessage("Wiped database and cache"));
        return true;
    }

    protected boolean handleGet(HttpExchange exchange, String path, String query) throws IOException {
        if (path.contains("purchased")) {
            return getOrders(exchange, path);
        }
        return false;
    }

    private void placeOrder(HttpExchange exchange, String messageBody) throws IOException {
        OrderCommand orderCommand;

        try {
            orderCommand = new ObjectMapper().readValue(messageBody, OrderCommand.class);
        } catch (Exception e) {
            sendJsonObjectResponse(exchange, 400, new StatusMessage("Invalid Request: Unable to parse JSON"));
            return;
        }

        if (orderCommand.getQuantity() < 1) {
            sendJsonObjectResponse(exchange, 400, orderCommand.toOrderStatus("Invalid Request: Quantity must be greater than 0"));
            return;
        }

        if (orderCommand.getUser_id() < 1) {
            sendJsonObjectResponse(exchange, 400, orderCommand.toOrderStatus("Invalid Request: Invalid user_id"));
            return;
        }
        if (orderCommand.getProduct_id() < 1) {
            sendJsonObjectResponse(exchange, 400, orderCommand.toOrderStatus("Invalid Request: Invalid user_id"));
            return;
        }

        if (!doesUserExist(orderCommand.getUser_id())) {
            sendJsonObjectResponse(exchange, 400, orderCommand.toOrderStatus("Invalid Request: User does not exist"));
            return;
        }

        // Get the product quantity
        Product product = getProduct(orderCommand.getProduct_id());
        if (product == null) {
            sendJsonObjectResponse(exchange, 400, orderCommand.toOrderStatus("Invalid Request: Product does not exist"));
            return;
        }

        int newQty = product.getQuantity() - orderCommand.getQuantity();

        if (newQty < 0) {
            sendJsonObjectResponse(exchange, 400, orderCommand.toOrderStatus("Exceeded quantity limit"));
            return;
        }

        try {


            Order order = new Order(orderCommand.getProduct_id(), orderCommand.getUser_id(), orderCommand.getQuantity());
            order = orderRepository.placeOrder(order);
            product.setQuantity(newQty);
            productCache.updateItem(product.getId(), product);
            sendJsonObjectResponse(exchange, 200, order.toOrderStatus("Success"));
            orderCache.updateItem(order.getId(), order);

            // Update the database
            boolean succeeded = updateProduct(orderCommand.getProduct_id(), newQty);
            if (!succeeded) {
                String response = "Something went wrong trying to update the quantity via product service. prodId:"
                        + orderCommand.getProduct_id();
                Logger.LogError(response);
                //sendJsonObjectResponse(exchange, 500, orderCommand.toOrderStatus(response));
                return;
            }


            // cache.addProductPurchase(order.getUser_id(), order.getProduct_id(), order.getQuantity());
        } catch (Exception e) {
            Logger.LogError("Error placing order via OrderRepository:");
            Logger.Log(e);

            //Need to increment product count back to what it was
//            System.out.print("Reverting product quantity update");
//            updateProduct(orderCommand.getProduct_id(), product.getQuantity());
//            sendJsonObjectResponse(exchange, 500, "Internal Server Error");
            return;
        }


    }

    private void recomputeAndCachePurchases(Order order) {
        Logger.LogTrace("OrderHandler.recomputeAndCachePurchases id=%d userid=%d prodid=%d qty="
                + order.getQuantity(), order.getId(), order.getUser_id(), order.getProduct_id());
        Map<Integer, Integer> productQtyCache = userProdQtyPurchased.getItem(order.getUser_id());
        if (productQtyCache == null) {
            productQtyCache = new HashMap<>();
        }
        int new_qty = order.getQuantity();
        if (productQtyCache.containsKey(order.getProduct_id())) {
            new_qty += productQtyCache.get(order.getProduct_id());
        }
        productQtyCache.put(order.getProduct_id(), new_qty);
        userProdQtyPurchased.updateItem(order.getUser_id(), productQtyCache);

    }

    private boolean updateProduct(int id, int newQty) throws JsonProcessingException {
        Map<String, Object> prodMap = new HashMap<String, Object>();
        prodMap.put("command", "update");
        prodMap.put("id", id);
        prodMap.put("quantity", newQty);
        String productJson = new ObjectMapper().writeValueAsString(prodMap);
        Http.Response response = Http.postJson(config.getProductServiceBaseUrl(), "/product", productJson);
//        if(response.getCode() == 200){
//            Product product = Product.from(response.getBody());
//            cache.updateItem(id, newQty);
//        }
//            cache.addProduct(productId, newQty);
        return response.getCode() == 200;
    }

    private Product getProduct(int productId) throws JsonProcessingException {
        Product product = productCache.getItem(productId);
        if (product != null)
            return product;

        Http.Response response = Http.get(config.getProductServiceBaseUrl(), "/product/" + productId);
        if (response.getCode() == 200) {
            product = Product.from(response.getBody(), Product.class);
            productCache.updateItem(productId, product);
            return product; //Nonsense!
        } else
            return null;
    }

    private boolean doesUserExist(int userId) {
//        if(config.getUseCache())       {
//            Util.println("doesUserExist USING CACHE", Util.ANSI_BLUE);
//            return cache.hasUser(userId);
//        } else {
//            Http.Response response = Http.get(config.getUserServiceBaseUrl(), "/user/" + userId);
//            return response.getCode() == 200;
//        }

//        User user = userCache.getItem(userId);
        if (userCache.hasItem(userId))
            return true;
        Http.Response response = Http.get(config.getUserServiceBaseUrl(), "/user/" + userId);
        return response.getCode() == 200;
//            if(response.getCode() != 200)
//                return false;
//            user = User.from(response.getBody());
//            if(user != null)
//                return false;
//
//
//            return true;
    }


    private boolean getOrders(HttpExchange exchange, String path) throws IOException {
        try {
            // Extract the user ID from the URL
            String[] pathParts = path.split("/");
            int userId = 0;

            try {
                userId = Integer.parseInt(pathParts[pathParts.length - 1]);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Invalid Request: Invalid user ID format");
                return true;
            }

            Logger.LogTrace("getOrders for user id %d", userId);
            List<UserProductPuchase> totalPurchases = getTotalPurchases(userId);
            Logger.LogTrace("getOrders for user id %d productCount = %d", userId, totalPurchases.size());
            sendJsonObjectResponse(exchange, 200, totalPurchases);
        } catch (Exception e) {
            System.out.println(e);
            sendResponse(exchange, 500, "Internal server error " + e);
        }

        return true;

    }

    private List<UserProductPuchase> getTotalPurchases(int userId) {
        return config.getUseCache() ?
                getCachedPurchases(userId)
                : orderRepository.getProductTotalPurchases(userId);
    }

    private List<UserProductPuchase> getCachedPurchases(int userId) {
        Map<Integer, Order> cache = orderCache.getMap();
        if (cache instanceof IMap<Integer, Order> dCache) {
            Predicate<Integer, Order> predicate = Predicates.equal("user_id", userId);
            Collection<Order> userOrders = dCache.values(predicate);

            Map<Integer, Integer> productQuantities = new HashMap<>();
            for (Order order : userOrders) {
                productQuantities.merge(order.getProduct_id(), order.getQuantity(), Integer::sum);
            }

            ArrayList<UserProductPuchase> totals = new ArrayList<>();
            productQuantities.forEach((productId, sumQuantity) ->
                    totals.add(new UserProductPuchase(productId, sumQuantity))
            );
            return totals;
        } else {
            return orderRepository.getProductTotalPurchases(userId);
        }

//        // Retrieve filtered orders
//
//        Map<Integer, Integer> productQty = userProdQtyPurchased.getItem(userId);
//        if (productQty == null)
//            return new UserProductPuchase[0];
//
//        UserProductPuchase[] purchaseTotalsArray = new UserProductPuchase[productQty.size()];
//        int index = 0;
//        for (Map.Entry<Integer, Integer> entry : productQty.entrySet()) {
//            UserProductPuchase prodQty = new UserProductPuchase(entry.getKey(), entry.getValue());
//            purchaseTotalsArray[index++] = prodQty;
//        }
//        return purchaseTotalsArray;

    }

}

