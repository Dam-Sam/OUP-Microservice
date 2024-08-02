package UserService;

import Common.DatabaseRepository;
import Common.ServiceConfig;
import Common.User;
import Common.Util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository extends DatabaseRepository {

    public UserRepository(ServiceConfig serviceConfig) {
        super(serviceConfig);
    }

    protected String getDropTableDDL() {
        return "DROP TABLE IF EXISTS public.users;";
    }

    @Override
    protected String getCreateTableDDL() {
        return
                "CREATE TABLE IF NOT EXISTS public.users\n" +
                "(\n" +
                "    id integer NOT NULL,\n" +
                "    username character varying(100) COLLATE pg_catalog.\"default\",\n" +
                "    email character varying(100) COLLATE pg_catalog.\"default\",\n" +
                "    password character varying(100) COLLATE pg_catalog.\"default\",\n" +
                "    CONSTRAINT users_pkey PRIMARY KEY (id)\n" +
                ");";
    }


    public boolean createUser(UserCommand user) {

        // Check if user with the given ID already exists
        if (isUserIdExists(user.getId())) {
            Util.Logger.LogInfo("User with ID " + user.getId() + " already exists.");
            return false; // or throw an exception or handle it accordingly
        }
    
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO users (id, username, email, password) VALUES (?, ?, ?, ?)")) {
    
            statement.setInt(1, user.getId());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword());
    
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }


    private boolean isUserIdExists(int userId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id FROM users WHERE id = ?")) {
    
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next(); // Returns true if a user with the given ID exists
            }
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
            return false; // Handle the error accordingly
        }
    }

    public User getUserById(int userId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
        ) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // User found, create a User object
                    return new User(
                            resultSet.getInt("id"),
                            resultSet.getString("username"),
                            resultSet.getString("email"),
                            hashPassword(resultSet.getString("password")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }
        return null;
    }

    // Method to hash a password using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage());
        }
    }


    public User updateUserById(User user) throws SQLException {

        StringBuilder queryBuilder = new StringBuilder("UPDATE users SET ");
        List<Object> params = new ArrayList<>();
        int updateCount = 0;
        if (!user.getUsername().isEmpty()) {
            queryBuilder.append("username = ?");
            params.add(user.getUsername());
            updateCount++;
        }
        if (!user.getEmail().isEmpty()) {
            if(updateCount > 0)
                queryBuilder.append(", ");
            queryBuilder.append("email = ?");
            params.add(user.getEmail());
            updateCount++;
        }
        if (!user.getPassword().isEmpty()) {
            if(updateCount > 0)
                queryBuilder.append(", ");
            queryBuilder.append("password = ?");
            params.add(user.getPassword());
            updateCount++;
        }

        // Check if any fields were provided for update
        if (updateCount == 0) {
            return null;
        }

        queryBuilder.append(" WHERE id = ? RETURNING *;");
        params.add(user.getId());

        try (Connection connection = getConnection()) {


            try (PreparedStatement statement = connection.prepareStatement(queryBuilder.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    statement.setObject(i + 1, params.get(i));
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Product found, create a User object
                        Util.Logger.Log(Util.Logger.INFO, "Product with ID %d updated successfully.", user.getId());

                        return new User(
                                resultSet.getInt("id"),
                                resultSet.getString("username"),
                                resultSet.getString("email"),
                                hashPassword(resultSet.getString("password")));
                    } else {
                        Util.Logger.LogInfo("User with ID " + user.getId() + " not found or no changes were made.");
                    }
                }

            }
        } catch (SQLException e) {
            System.err.println("Error updating user by ID: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public boolean deleteUser(UserCommand userCommand) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ? AND username = ? AND email = ? AND password = ?")) {

            statement.setInt(1, userCommand.getId());
            statement.setString(2, userCommand.getUsername());
            statement.setString(3, userCommand.getEmail());
            statement.setString(4, userCommand.getPassword());

            int rowsAffected = statement.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }


}
