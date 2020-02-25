package net.azisaba.main.budgets.bank;

import java.io.File;
import java.math.BigDecimal;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.earth2me.essentials.Essentials;

import net.azisaba.main.budgets.GovernmentBudget;
import net.azisaba.main.budgets.sql.BalanceLogSQLController;
import net.azisaba.main.budgets.sql.SQLHandler;
import net.milkbowl.vault.economy.Economy;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerAccountBank implements GovernmentBank {

    private final GovernmentBudget plugin;

    private final UUID playerUUID;
    private final Economy econ;

    private Essentials ess;

    private BalanceLogSQLController controller;

    public PlayerAccountBank load() {
        SQLHandler handler = new SQLHandler(new File(plugin.getDataFolder(), "balance.db"));
        controller = new BalanceLogSQLController(handler).init();
        return this;
    }

    @Override
    public boolean has(BigDecimal amount) {
        try {
            return econ.has(Bukkit.getOfflinePlayer(playerUUID), amount.doubleValue());
        } catch ( Exception e ) {
            try {
                setupEssentials();
                if ( ess != null ) {
                    return ess.getUser(playerUUID).getMoney().compareTo(amount) >= 0;
                } else {
                    e.printStackTrace();
                    return false;
                }
            } catch ( Exception ex ) {
                ex.printStackTrace();
                throw new IllegalAccessError("Something wrong on checking the owner account (" + playerUUID.toString() + ")");
            }
        }
    }

    @Override
    public BigDecimal getBalance() {
        try {
            return BigDecimal.valueOf(econ.getBalance(Bukkit.getOfflinePlayer(playerUUID)));
        } catch ( Exception e ) {
            try {
                setupEssentials();
                if ( ess != null ) {
                    return ess.getUser(playerUUID).getMoney();
                } else {
                    throw new IllegalStateException("Failed to get balance and Essentials is not enabled.");
                }
            } catch ( Exception ex ) {
                ex.printStackTrace();
                throw new IllegalAccessError("Something wrong on checking the owner account (" + playerUUID.toString() + ")");
            }
        }
    }

    @Override
    public boolean deposit(BigDecimal amount, String comment) {
        try {
            econ.depositPlayer(Bukkit.getOfflinePlayer(playerUUID), amount.doubleValue());
            return controller.addInOutLog(amount, System.currentTimeMillis(), comment);
        } catch ( Exception e ) {
            try {
                setupEssentials();
                if ( ess != null ) {
                    ess.getUser(playerUUID).giveMoney(amount);
                    return controller.addInOutLog(amount, System.currentTimeMillis(), comment);
                } else {
                    throw new IllegalStateException("Failed to deposit money and Essentials is not enabled.");
                }
            } catch ( Exception ex ) {
                ex.printStackTrace();
                throw new IllegalAccessError("Something wrong on depositting the owner account (" + playerUUID.toString() + ")");
            }
        }
    }

    @Override
    public boolean withdraw(BigDecimal amount, String comment) {
        if ( !has(amount) ) {
            throw new IllegalArgumentException("The owner haven't enough money.");
        }
        try {
            econ.withdrawPlayer(Bukkit.getOfflinePlayer(playerUUID), amount.doubleValue());
            return controller.addInOutLog(amount.negate(), System.currentTimeMillis(), comment);
        } catch ( Exception e ) {
            try {
                setupEssentials();
                if ( ess != null ) {
                    ess.getUser(playerUUID).takeMoney(amount);
                    return controller.addInOutLog(amount.negate(), System.currentTimeMillis(), comment);
                } else {
                    throw new IllegalStateException("Failed to withdraw money and Essentials is not enabled.");
                }
            } catch ( Exception ex ) {
                ex.printStackTrace();
                throw new IllegalAccessError("Something wrong on withdrawing the owner account (" + playerUUID.toString() + ")");
            }
        }
    }

    @Override
    public boolean withdraw(BigDecimal amount) {
        return withdraw(amount, "");
    }

    @Override
    public boolean deposit(BigDecimal amount) {
        return deposit(amount, "");
    }

    @Override
    public void onDisable() {
        controller.close();
    }

    private void setupEssentials() {
        if ( ess != null && ess.isEnabled() ) {
            return;
        }
        if ( ess != null && !ess.isEnabled() ) {
            ess = null;
            return;
        }
        if ( Bukkit.getPluginManager().getPlugin("Essentials") != null ) {
            ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        }
    }
}
