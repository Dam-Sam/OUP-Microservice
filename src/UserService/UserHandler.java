package UserService;

import Common.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;


class UserHandler extends ServiceHttpHandler {

    private final ServiceConfig config;
    private UserRepository userRepository;

    private Cache<User> cache;
    private boolean useCache;

    public UserHandler(ServiceConfig config, UserRepository userRepository, Cache<User> cache) {
        this.config = config;
        this.userRepository = userRepository;
        this.cache = cache;
    }


    @Override
    protected boolean handleWipe(HttpExchange exchange, String path) throws IOException {
        userRepository.resetDB();
        if(cache != null)
            cache.resetCache();
        sendJsonObjectResponse(exchange, 200, new StatusMessage("Wiped database and cache"));
        return true;
    }

    @Override
    protected boolean handleGet(HttpExchange exchange, String path, String query) throws IOException {
        try {
                String[] pathParts = path.split("/");
                int userId = Integer.parseInt(pathParts[pathParts.length-1]);
                User user = getUserById(userId);

                if (user != null) {
                    String response = new ObjectMapper().writeValueAsString(user);
                    sendJsonObjectResponse(exchange, 200, user);
                } else {
                    sendJsonObjectResponse(exchange, 404, new StatusMessage("User not found"));
                }
        } catch (NumberFormatException e) {
            sendJsonObjectResponse(exchange, 400, new StatusMessage("Invalid user ID"));
        } catch (Exception e) {
            sendJsonObjectResponse(exchange, 500, new StatusMessage("Internal Server Error"));
        }
        return true;

    }

    private User getUserById(int userId) {
        User user = cache.getItem(userId);
        if(user != null)
            return user;

        user = userRepository.getUserById(userId);
        if(user != null)
            cache.updateItem(userId, user);
        return user;
    }

    @Override
    protected boolean handlePost(HttpExchange exchange, String path, String query, String command, String messageBody) throws IOException {

        UserCommand userCommand = new ObjectMapper().readValue(messageBody, UserCommand.class);

        switch (command) {
            case "create":
                handleCreate(exchange, userCommand);
                break;
            case "update":
                handleUpdate(exchange, userCommand);
                break;
            case "delete":
                handleDelete(exchange, userCommand);
                break;
            default:
                sendJsonObjectResponse(exchange, 400, new StatusMessage("Invalid command"));
                break;
        }

        return true;
    }

    private void handleCreate(HttpExchange exchange, UserCommand userCommand) throws IOException {
        try {
            if (!validFields(userCommand)) {
                sendJsonObjectResponse(exchange, 400);
                return;
            }
            
            User newUser = new User(
                    userCommand.getId(),
                    userCommand.getUsername(),
                    userCommand.getEmail(),
                    userCommand.getPassword()
            );
            boolean created = userRepository.createUser(userCommand);

            if (created) {
                cache.updateItem(newUser.getId(), newUser);
                sendJsonObjectResponse(exchange, 200, newUser);
            } else {
                sendJsonObjectResponse(exchange, 409, new StatusMessage("User with id already exists"));
            }

        } catch (Exception e) {
            sendJsonObjectResponse(exchange, 500, new StatusMessage("Error creating user.\n" + e.getMessage()));
        }
    }

    private void handleUpdate(HttpExchange exchange, UserCommand userCommand) throws IOException {
        try {
            if (!validForUpdate(userCommand)) {
                sendJsonObjectResponse(exchange, 400);
                return;
            }
            // If fields are provided or not, proceed with the update
            User updated = userRepository.updateUserById(userCommand);

            if (updated != null) {
                cache.updateItem(updated.getId(), updated);
                sendJsonObjectResponse(exchange, 200, updated);
            } else {
                sendJsonObjectResponse(exchange, 400, new StatusMessage("Invalid Request"));
                cache.removeItem(userCommand.getId());
            }
        } catch (Exception e) {
            sendJsonObjectResponse(exchange, 500, new StatusMessage("Error updating user.\n" + e.getMessage()));
        }
    }

    private void handleDelete(HttpExchange exchange, UserCommand userCommand) throws IOException {
        try {
            if (!validFields(userCommand)) {
                sendJsonObjectResponse(exchange, 400);
                return;
            }
            if (userRepository.deleteUser(userCommand)) {
                cache.removeItem(userCommand.getId());
                sendResponse(exchange, 200, "");
            } else {
                sendJsonObjectResponse(exchange, 404, new StatusMessage("User not found or no changes were made"));
                cache.removeItem(userCommand.getId());
            }
        } catch (Exception e) {
            sendJsonObjectResponse(exchange, 500, new StatusMessage("Error deleting user.\n" + e.getMessage()));
        }
    }

    private boolean validFields(UserCommand userCommand) {
        if(userCommand.getId() == 0)
            return false;
        return !userCommand.getUsername().isBlank()
                && !userCommand.getEmail().isBlank()
                && !userCommand.getPassword().isBlank();
    }

    private boolean validForUpdate(UserCommand userCommand) {
        if(userCommand.getId() == 0)
            return false;
        return !userCommand.getUsername().isBlank()
                || !userCommand.getEmail().isBlank()
                || !userCommand.getPassword().isBlank();

    }

}
