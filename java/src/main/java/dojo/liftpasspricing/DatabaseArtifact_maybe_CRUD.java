package dojo.liftpasspricing;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DatabaseArtifact_maybe_CRUD {
    private final Connection connection;
    private final PreparedStatement costStmt;

    public DatabaseArtifact_maybe_CRUD(Connection connection, PreparedStatement costStmt) {
        this.connection = connection;
        this.costStmt = costStmt;
    }

    public Connection getConnection() {
        return connection;
    }

    public PreparedStatement getCostStmt() {
        return costStmt;
    }
}
