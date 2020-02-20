package net.azisaba.main.budgets;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.azisaba.main.budgets.bank.GovernmentBank;
import net.azisaba.main.budgets.bank.SQLDatabaseBank;
import net.azisaba.main.budgets.command.GovernmentBudgetCommand;
import net.azisaba.main.budgets.config.DefaultConfig;
import net.azisaba.main.budgets.sql.SQLHandler;
import net.azisaba.main.budgets.sql.TaxDataController;
import net.azisaba.main.budgets.tax.ExecuteTaxCollectTaskRunnable;
import net.milkbowl.vault.economy.Economy;

import lombok.Getter;

public class GovernmentBudget extends JavaPlugin {

    @Getter
    private DefaultConfig defaultConfig;
    @Getter
    private GovernmentBank bank = null;
    @Getter
    private SQLHandler sqlHandler;
    @Getter
    private TaxDataController taxDataController;

    @Getter
    private static Economy economy;

    @Override
    public void onEnable() {
        defaultConfig = new DefaultConfig(this);
        defaultConfig.loadConfig();

        if ( defaultConfig.isDontUsePlayerAccount() ) {
            bank = new SQLDatabaseBank(this).load();
        } else {
            getLogger().warning("現在SQL以外は未対応です！ Pluginを無効化しています...");
            Bukkit.getPluginManager().enablePlugin(this);
            return;
        }

        setupEconomy();
        ExecuteTaxCollectTaskRunnable.initialize(this);
        taxDataController = new TaxDataController(this).init();

        Bukkit.getPluginCommand("governmentbudget").setExecutor(new GovernmentBudgetCommand(this));

        Bukkit.getLogger().info(getName() + " enabled.");
    }

    @Override
    public void onDisable() {
        if ( bank != null ) {
            bank.onDisable();
        }

        Bukkit.getLogger().info(getName() + " disabled.");
    }

    public static boolean setupEconomy() {
        try {
            if ( Bukkit.getPluginManager().getPlugin("Vault") == null ) {
                return false;
            }
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if ( rsp == null ) {
                return false;
            }
            economy = rsp.getProvider();
            return economy != null;
        } catch ( Exception e ) {
            return false;
        }
    }
}
