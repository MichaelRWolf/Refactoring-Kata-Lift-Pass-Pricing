package dojo.liftpasspricing;

import java.sql.SQLException;

public interface CostForTypeProvider {
    void setLiftPassCostForLiftPassType(int liftPassCost, String liftPassType) throws SQLException;

    int getCostForLiftTicketType(String liftTicketType) throws SQLException;
}
