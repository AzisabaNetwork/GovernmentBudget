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

                BigDecimal collectedMonthlyTax = BigDecimal.valueOf(0), collectedWeeklyTax = BigDecimal.valueOf(0);

                for ( MonthlyTaxInfo info : monthlyTax ) {
                    // アジを除外
                    if ( info.getUuid().toString().equals("58becc44-c5b7-420f-8800-15ba88820973") ) {
                        continue;
                    }

                    BigDecimal withdrawValue = info.getSubtractMoney();
                    if ( withdrawValue.compareTo(BigDecimal.ZERO) < 0 ) {
                        Bukkit.getLogger().info("Monthly withdraw value became less than zero (" + info.getUuid().toString() + ", " + withdrawValue + ")");
                        continue;
                    } else if ( withdrawValue.compareTo(BigDecimal.ZERO) == 0 ) {
                        continue;
                    } else if ( info.getCurrentStage() >= 4 ) {
                        try {
                            withdrawValue = BigDecimal.valueOf(econ.getBalance(Bukkit.getOfflinePlayer(info.getUuid())));
                        } catch ( Exception e ) {
                            try {
                                withdrawValue = ess.getUser(info.getUuid()).getMoney();
                            } catch ( Exception ex ) {
                                ex.printStackTrace();
                                return;
                            }
                        }
                    } else {
                        withdrawValue = withdrawValue.setScale(2, RoundingMode.DOWN);
                    }

                    boolean success = takeMoney(ess, info.getUuid(), withdrawValue);
                    if ( success ) {
                        collectedMonthlyTax = collectedMonthlyTax.add(withdrawValue);
                    } else {
                        Bukkit.getLogger().warning("Failed to collect monthly tax from a player (" + info.getUuid().toString() + ")");
                    }
                }

                List<UUID> uuidList = monthlyTax.stream()
                        .map(info -> info.getUuid())
                        .collect(Collectors.toList());
                plugin.getTaxDataController().incrementStage(uuidList);

                if ( collectedMonthlyTax.compareTo(BigDecimal.ZERO) > 0 ) {
                    plugin.getBank().deposit(collectedMonthlyTax, "Monthly Tax");
                    plugin.getLogger().info("Collected Monthly Tax ( " + collectedMonthlyTax.toString() + " )");
                }

                for ( UUID uuid : weeklyTax ) {
                    // アジを除外
                    if ( uuid.toString().equals("58becc44-c5b7-420f-8800-15ba88820973") ) {
                        continue;
                    }

                    BigDecimal withdrawValue;
                    try {
                        withdrawValue = BigDecimal.valueOf(econ.getBalance(Bukkit.getOfflinePlayer(uuid))).multiply(BigDecimal.valueOf(0.005d));
                    } catch ( Exception e ) {
                        // Essentials 経由で値段取得
                        if ( ess == null ) {
                            e.printStackTrace();
                            return;
                        }
                        try {
                            withdrawValue = ess.getUser(uuid).getMoney().multiply(BigDecimal.valueOf(0.005));
                        } catch ( Exception ex ) {
                            ex.printStackTrace();
                            return;
                        }
                    }
                    if ( withdrawValue.compareTo(BigDecimal.ZERO) < 0 ) {
                        Bukkit.getLogger().info("Weekly withdraw value became less than zero (" + uuid.toString() + ", " + withdrawValue + ")");
                        continue;
                    } else if ( withdrawValue.compareTo(BigDecimal.ZERO) == 0 ) {
                        continue;
                    }
                    withdrawValue = withdrawValue.setScale(2, RoundingMode.DOWN);

                    boolean success = takeMoney(ess, uuid, withdrawValue);
                    if ( success ) {
                        collectedWeeklyTax = collectedWeeklyTax.add(withdrawValue);
                    } else {
                        Bukkit.getLogger().warning("Failed to collect weekly tax from a player (" + uuid.toString() + ")");
                    }
                }

                if ( collectedWeeklyTax.compareTo(BigDecimal.ZERO) > 0 ) {
                    plugin.getBank().deposit(collectedWeeklyTax, "Weekly Tax");
                    plugin.getLogger().info("Collected Weekly Tax ( " + collectedWeeklyTax.toString() + " )");
                }

                Bukkit.getLogger().info("Finished to collect tax!");
            }
        }.start();
    }

    private boolean takeMoney(Essentials ess, UUID uuid, BigDecimal value) {
        try {
            EconomyResponse res = GovernmentBudget.getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(uuid), value.doubleValue());
            if ( res.transactionSuccess() ) {
                return true;
            } else {
                return takeMoneyUsingEssentials(ess, uuid, value);
            }
        } catch ( Exception e ) {
            return takeMoneyUsingEssentials(ess, uuid, value);
        }
    }

    private boolean takeMoneyUsingEssentials(Essentials ess, UUID uuid, BigDecimal value) {
        if ( ess == null ) {
            return false;
        }

        try {
            ess.getUser(uuid).takeMoney(value);
            return true;
        } catch ( Exception ex ) {
            return false;
        }
    }
}
