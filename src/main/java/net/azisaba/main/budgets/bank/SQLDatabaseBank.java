package net.azisaba.main.budgets.bank;

import java.io.File;
import java.math.BigDecimal;

import net.azisaba.main.budgets.GovernmentBudget;
import net.azisaba.main.budgets.sql.BalanceLogSQLController;
import net.azisaba.main.budgets.sql.SQLHandler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SQLDatabaseBank implements GovernmentBank {

    private final GovernmentBudget plugin;

    @Getter
    private BigDecimal balance = BigDecimal.ZERO;

    private BalanceLogSQLController controller;

    public SQLDatabaseBank load() {
        SQLHandler handler = new SQLHandler(new File(plugin.getDataFolder(), "balance.db"));
        controller = new BalanceLogSQLController(handler).init();

        // 所持金の読み込み
        balance.add(controller.getBalance());
        return this;
    }

    @Override
    public boolean has(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    @Override
    public boolean deposit(BigDecimal amount, String comment) {
        if ( amount.compareTo(BigDecimal.ZERO) <= 0 ) {
            throw new IllegalArgumentException("Value must be greater than zero.");
        }

        balance = balance.add(amount);
        boolean success = controller.addInOutLog(amount, System.currentTimeMillis(), comment);
        return success;
    }

    @Override
    public boolean withdraw(BigDecimal amount, String comment) {
        if ( amount.compareTo(BigDecimal.ZERO) <= 0 ) {
            throw new IllegalArgumentException("Value must be greater than zero.");
        }
        if ( !has(amount) ) {
            throw new IllegalArgumentException("There is no enough money.");
        }

        balance = balance.subtract(amount);
        boolean success = controller.addInOutLog(amount.negate(), System.currentTimeMillis(), comment);
        return success;
    }

    @Override
    public boolean deposit(BigDecimal amount) {
        return deposit(amount, "");
    }

    @Override
    public boolean withdraw(BigDecimal amount) {
        return withdraw(amount, "");
    }

    public boolean save() {
        return controller.saveBalance(balance);
    }

    @Override
    public void onDisable() {
        save();
        controller.close();
    }
}
