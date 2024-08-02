package UserService;

import Common.Product;
import Common.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true )
public class UserCommand extends User {
    private String command;

    public UserCommand(String commandType, int id, String username, String email, String password) {
        super(id, username, email, password);
        this.command = commandType;
    }

    public UserCommand() {
    }

    public String getCommand() {
        return command;
    }

    public User toUser() {
        return new User(id, username, email, password);
    }

}
