package dojo.liftpasspricing;

import java.sql.Connection;
import java.sql.ResultSet;

public class DbArtifactCostByType {
    private final Connection connection;
    private final ResultSet result;

    public DbArtifactCostByType(Connection connection, ResultSet result) {
        this.connection = connection;
        this.result = result;
    }

    public Connection getConnection() {
        return connection;
    }

    public ResultSet getResult() {
        return result;
    }
}
