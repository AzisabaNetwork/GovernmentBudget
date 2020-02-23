package net.azisaba.main.budgets.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.earth2me.essentials.Essentials;

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
                List<MonthlyTaxInfo> monthlyTax = plugin.getTaxDataController().getMonthlyTaxInfo();
                List<UUID> weeklyTax = plugin.getTaxDataController().getAccountsForWeeklyTask();

                Economy econ = GovernmentBudget.getEconomy();
                Essentials ess = null;
                if ( Bukkit.getPluginManager().getPlugin("Essentials") != null ) {
                    ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                }

                double collectedMonthlyTax = 0;
                double collectedWeeklyTax = 0;

                for ( MonthlyTaxInfo info : monthlyTax ) {
                    double withdrawValue = info.getSubtractMoney().doubleValue();
                    if ( withdrawValue < 0 ) {
                        Bukkit.getLogger().info("Monthly withdraw value became less than zero (" + info.getUuid().toString() + ", " + withdrawValue + ")");
                        continue;
                    } else if ( withdrawValue == 0 ) {
                        continue;
                    } else if ( info.getCurrentStage() >= 4 ) {
                        try {
                            withdrawValue = econ.getBalance(Bukkit.getOfflinePlayer(info.getUuid()));
                        } catch ( Exception e ) {
                            try {
                                withdrawValue = ess.getUser(info.getUuid()).getMoney().doubleValue();
                            } catch ( Exception ex ) {
                                ex.printStackTrace();
                                return;
                            }
                        }
                    } else {
                        withdrawValue = BigDecimal.valueOf(withdrawValue).setScale(2, RoundingMode.DOWN).doubleValue();
                    }

                    try {
                        EconomyResponse res = econ.withdrawPlayer(Bukkit.getOfflinePlayer(info.getUuid()), withdrawValue);
                        if ( res.transactionSuccess() ) {
                            collectedMonthlyTax += withdrawValue;
                        }
                    } catch ( Exception e ) {
                        if ( ess == null ) {
                            e.printStackTrace();
                            return;
                        }
                        try {
                            ess.getUser(info.getUuid()).takeMoney(BigDecimal.valueOf(withdrawValue));
                            collectedMonthlyTax += withdrawValue;
                        } catch ( Exception ex ) {
                            Bukkit.getLogger().warning("Failed to collect monthly tax from a player (" + info.getUuid().toString() + ")");
                            ex.printStackTrace();
                            return;
                        }
                    }
                }

                List<UUID> uuidList = monthlyTax.stream()
                        .map(info -> info.getUuid())
                        .collect(Collectors.toList());
                plugin.getTaxDataController().incrementStage(uuidList);

                if ( collectedMonthlyTax > 0 ) {
                    plugin.getBank().deposit(BigDecimal.valueOf(collectedMonthlyTax), "Monthly Tax");
                    plugin.getLogger().info("Collected Monthly Tax ( " + collectedMonthlyTax + " )");
                }

                for ( UUID uuid : weeklyTax ) {
                    double withdrawValue;
                    try {
                        withdrawValue = econ.getBalance(Bukkit.getOfflinePlayer(uuid)) * 0.005;
                    } catch ( Exception e ) {
                        // Essentials 経由で値段取得
                        if ( ess == null ) {
                            e.printStackTrace();
                            return;
                        }
                        try {
                            withdrawValue = ess.getUser(uuid).getMoney().multiply(BigDecimal.valueOf(0.005)).doubleValue();
                        } catch ( Exception ex ) {
                            ex.printStackTrace();
                            return;
                        }
                    }
                    if ( withdrawValue < 0 ) {
                        Bukkit.getLogger().info("Weekly withdraw value became less than zero (" + uuid.toString() + ", " + withdrawValue + ")");
                        continue;
                    } else if ( withdrawValue == 0 ) {
                        continue;
                    }
                    withdrawValue = BigDecimal.valueOf(withdrawValue).setScale(2, RoundingMode.DOWN).doubleValue();

                    try {
                        EconomyResponse res = econ.withdrawPlayer(Bukkit.getOfflinePlayer(uuid), econ.getBalance(Bukkit.getOfflinePlayer(uuid)));
                        if ( res.transactionSuccess() ) {
                            collectedWeeklyTax += withdrawValue;
                        } else {
                            Bukkit.getLogger().warning("Failed to collect weekly tax from a player (" + uuid.toString() + ")");
                        }
                    } catch ( Exception e ) {
                        if ( ess == null ) {
                            e.printStackTrace();
                            return;
                        }
                        try {
                            ess.getUser(uuid).takeMoney(BigDecimal.valueOf(withdrawValue));
                            collectedWeeklyTax += withdrawValue;
                        } catch ( Exception ex ) {
                            Bukkit.getLogger().warning("Failed to collect weekly tax from a player (" + uuid.toString() + ")");
                            ex.printStackTrace();
                            return;
                        }
                    }
                }

                if ( collectedWeeklyTax > 0 ) {
                    plugin.getBank().deposit(BigDecimal.valueOf(collectedWeeklyTax), "Weekly Tax");
                    plugin.getLogger().info("Collected Weekly Tax ( " + collectedWeeklyTax + " )");
                }
            }
        }.start();
    }
}
