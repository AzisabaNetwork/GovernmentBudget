package net.azisaba.main.budgets.sql;

import java.math.BigDecimal;
import java.sql.ResultSet;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BalanceLogSQLController {

    private final SQLHandler handler;

    private final String balanceTableName = "balance";
    private final String logTableName = "balance_log";

    /**
     * テーブルの作成など初期に必要な処理を行います
     *
     * @return 同じインスタンス
     */
    public BalanceLogSQLController init() {
        // initializedされていない場合はする
        if ( !handler.isInitialized() ) {
            handler.init();
        }

        // balance のテーブルがなければ作成する
        handler.executeCommand("CREATE TABLE IF NOT EXISTS '" + balanceTableName + "' (" +
                "    'id'    INTEGER," +
                "    'balance'   NUMERIC," +
                "    PRIMARY KEY(\"id\")" +
                ");");

        // balance log のテーブルがなければ作成する
        handler.executeCommand("CREATE TABLE IF NOT EXISTS '" + logTableName + "' (" +
                "    \"in_out\"    NUMERIC," +
                "    \"time\"  INTEGER," +
                "    \"content\"   BLOB" +
                ");");

        return this;
    }

    public BigDecimal getBalance() {
        try {
            ResultSet set = handler.executeQuery("select balance from '" + balanceTableName + "';");

            if ( set.next() ) {
                return new BigDecimal(set.getString(1));
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public boolean addInOutLog(BigDecimal amount, long time, String comment) {
        try {
            int change = handler.executeCommand("insert into balance_log "
                    + "( in_out, time, content ) "
                    + "values ( " + amount + ", " + time + ", \"" + comment + "\" );");

            return change > 0;
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean saveBalance(BigDecimal balance) {
        try {
            int changes = handler.executeCommand("insert or replace into " + balanceTableName + " (id, balance) values (1, " + balance + ");");
            return changes > 0;
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return false;
    }

    public void close() {
        handler.closeConnection();
    }
}
