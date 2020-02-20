package net.azisaba.main.budgets.sql;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.earth2me.essentials.User;

import net.azisaba.main.budgets.GovernmentBudget;
import net.azisaba.main.budgets.tax.MonthlyTaxInfo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaxDataController {

    private final GovernmentBudget plugin;
    private SQLHandler handler = null;

    private final String dataTableName = "player_and_bank_data";

    private final long monthInMilli = 1000L * 60L * 60L * 24L * 30L;

    public TaxDataController init() {
        handler = new SQLHandler(new File(plugin.getDataFolder(), "TaxData.db"));

        // initializedされていない場合はする
        if ( !handler.isInitialized() ) {
            handler.init();
        }

        // tax and player data のテーブルを作成する
        handler.executeCommand("CREATE TABLE IF NOT EXISTS '" + dataTableName + "' (" +
                "    'uuid'  BLOB NOT NULL," +
                "    'lastjoined'    BIGINT DEFAULT -1," +
                "    'bank'  NUMERIC DEFAULT 0," +
                "    'stage' INTEGER DEFAULT 0," +
                "    PRIMARY KEY('uuid')" +
                ");");

        return this;
    }

    public int addPlayerData(List<User> userDataList) {
        try {
            List<String> commandList = new ArrayList<>();
            for ( User user : userDataList ) {
                UUID uuid = user.getConfigUUID();
                long lastJoined = user.getLastLogout();
                double bank = user.getMoney().doubleValue();

                commandList.add("('" + uuid.toString() + "', " + lastJoined + ", " + bank + ", 0)");
            }

            return handler.executeCommand("insert or replace into '" + dataTableName + "' (uuid, lastjoined, bank, stage) values " + String.join(", ", commandList));
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<UUID> getAccountsForWeeklyTask() {
        long limit = System.currentTimeMillis() - monthInMilli;

        try {
            ResultSet set = handler.executeQuery("select uuid from '" + dataTableName + "' where lastjoined > " + limit + " and stage = 0");

            List<UUID> uuidList = new ArrayList<>();
            while ( set.next() ) {
                UUID uuid = UUID.fromString(set.getString("uuid"));
                uuidList.add(uuid);
            }

            return uuidList;
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return null;
    }

    public List<MonthlyTaxInfo> getMonthlyTaxInfo() {
        List<MonthlyTaxInfo> taxInfoList = new ArrayList<>();

        try {
            for ( int stage = 1; stage < 5; stage++ ) {
                long limit = System.currentTimeMillis() - monthInMilli * stage;

                ResultSet set = handler.executeQuery("select uuid, bank, stage from '" + dataTableName + "' where lastjoined < " + limit + " and stage < " + (stage + 1));

                while ( set.next() ) {
                    UUID uuid = UUID.fromString(set.getString("uuid"));
                    BigDecimal value = BigDecimal.valueOf(set.getDouble("bank"));
                    int currentStage = set.getInt("stage");

                    taxInfoList.add(new MonthlyTaxInfo(uuid, value, currentStage));
                }
            }

            return taxInfoList;
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return null;
    }
}