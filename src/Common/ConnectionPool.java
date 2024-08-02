package Common;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class ConnectionPool {

    private static HikariDataSource dataSource;
    private static boolean initialized;

    private static String dbHost;
    private static String dbUser;
    private static String dbPwd;

    public static void initialize(ServiceConfig sConfig){
        dbHost = sConfig.getDbHost();
        dbUser = sConfig.getDbUsername();
        dbPwd = sConfig.getDbPassword();
        if (sConfig.getConnectionPoolSize() == 0) {
            System.out.println(Util.Red("Connection pool is DISABLED (size == 0)"));
            return;
        }

        HikariConfig hConfig = new HikariConfig();
        hConfig.setJdbcUrl(dbHost);
        hConfig.setUsername(dbUser);
        hConfig.setPassword(dbPwd);

        // Optional: add more configuration to tune the pool
        hConfig.setMaximumPoolSize(sConfig.getConnectionPoolSize());
        hConfig.setAutoCommit(true);
        hConfig.addDataSourceProperty("cachePrepStmts", "true");
        hConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(hConfig);
        initialized = true;
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(() -> dataSource.close()));

        System.out.println(Util.Green("Connection pool is initialized (size = "
                + sConfig.getConnectionPoolSize() + ")"));

    }

    public static Connection getConnection() throws SQLException {
            Util.Logger.Log(Util.Logger.DEBUG, "DB Connection request. Servicing with " +
                    (initialized ? "ConnectionPool" : "DriverManager.getConnection()"));
        return initialized ? dataSource.getConnection() : DriverManager.getConnection(dbHost, dbUser, dbPwd);
    }
}