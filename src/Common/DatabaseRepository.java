package Common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class DatabaseRepository {
    protected final ServiceConfig serviceConfig;

    public DatabaseRepository(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
        this.createTableIfNotExist();
    }

    protected Connection getConnection() throws SQLException {
        return ConnectionPool.getConnection();
    }


    public void dropTable() {

        String dropSQL = getDropTableDDL();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(dropSQL);
        ) {
            statement.execute();

        } catch (SQLException e) {
            System.err.println("Error when attempting to drop table:");
            System.err.println(dropSQL);
            Util.Logger.Log(e);
        }
    }

    protected abstract String getDropTableDDL();


    public void createTableIfNotExist() {

        String createSQL = getCreateTableDDL();
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(createSQL)
        ) {
            statement.execute();

        } catch (SQLException e) {
            System.err.println("Error when attempting to create table using the following SQL: ");
            System.err.println(createSQL);
            Util.Logger.Log(e);
        }
    }

    public void resetDB() {

        dropTable();
        createTableIfNotExist();
        System.out.println("All data wiped.");

    }

    protected abstract String getCreateTableDDL();

}
