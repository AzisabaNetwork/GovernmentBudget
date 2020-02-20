package net.azisaba.main.budgets.tax;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.main.budgets.GovernmentBudget;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaxCollectTask extends BukkitRunnable {

    private final GovernmentBudget plugin;

    @Override
    public void run() {
        new Thread() {
            public void run() {
                List<UUID> weeklyTax = plugin.getTaxDataController().getAccountsForWeeklyTask();
                List<MonthlyTaxInfo> monthlyTax = plugin.getTaxDataController().getMonthlyTaxInfo();

                Economy econ = GovernmentBudget.getEconomy();

                double collectedWeeklyTax = 0;
                double collectedMonthlyTax = 0;

                for ( UUID uuid : weeklyTax ) {
                    double withdrawValue = econ.getBalance(Bukkit.getOfflinePlayer(uuid)) * 0.005; // Error ( Economy username cannot be null ) - Essentials経由で引けば何とかなる...？
                    if ( withdrawValue < 0 ) {
                        Bukkit.getLogger().info("Weekly withdraw value became less than zero (" + uuid.toString() + ", " + withdrawValue + ")");
                        continue;
                    } else if ( withdrawValue == 0 ) {
                        continue;
                    }

                    EconomyResponse res = econ.withdrawPlayer(Bukkit.getOfflinePlayer(uuid), econ.getBalance(Bukkit.getOfflinePlayer(uuid)));
                    if ( res.transactionSuccess() ) {
                        collectedWeeklyTax += withdrawValue;
                    } else {
                        Bukkit.getLogger().warning("Failed to collect weekly tax from a player (" + uuid.toString() + ")");
                    }
                }

                if ( collectedWeeklyTax > 0 ) {
                    plugin.getBank().deposit(BigDecimal.valueOf(collectedWeeklyTax), "Weekly Tax");
                    plugin.getLogger().info("Collected Weekly Tax ( " + collectedWeeklyTax + " )");
                }

                for ( MonthlyTaxInfo info : monthlyTax ) {
                    double withdrawValue = info.getSubtractMoney().doubleValue();
                    if ( withdrawValue < 0 ) {
                        Bukkit.getLogger().info("Monthly withdraw value became less than zero (" + info.getUuid().toString() + ", " + withdrawValue + ")");
                        continue;
                    } else if ( withdrawValue == 0 ) {
                        continue;
                    }

                    EconomyResponse res = econ.withdrawPlayer(Bukkit.getOfflinePlayer(info.getUuid()), withdrawValue);
                    if ( res.transactionSuccess() ) {
                        collectedMonthlyTax += withdrawValue;
                    }
                }

                if ( collectedMonthlyTax > 0 ) {
                    plugin.getBank().deposit(BigDecimal.valueOf(collectedMonthlyTax), "Monthly Tax");
                    plugin.getLogger().info("Collected Monthly Tax ( " + collectedMonthlyTax + " )");
                }
            }
        }.start();
    }
}
