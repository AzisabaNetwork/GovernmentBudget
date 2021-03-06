package net.azisaba.main.budgets;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.azisaba.main.budgets.bank.GovernmentBank;
import net.azisaba.main.budgets.bank.PlayerAccountBank;
import net.azisaba.main.budgets.command.GovernmentBudgetCommand;
import net.azisaba.main.budgets.config.DefaultConfig;
import net.azisaba.main.budgets.listener.UpdateSQLDataListener;
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

    @Getter
    private boolean enableUpdatePlayerData = true;

    @Override
    public void onEnable() {
        defaultConfig = new DefaultConfig(this);
        defaultConfig.loadConfig();

        setupEconomy();

        if ( defaultConfig.isDontUsePlayerAccount() ) {
            // bank = new SQLDatabaseBank(this).load(); // SQL Bank
            bank = new PlayerAccountBank(this, UUID.fromString("58becc44-c5b7-420f-8800-15ba88820973"), economy).load(); // ledlaggazi's account
        } else {
            getLogger().warning("予算の読み込みに失敗しました。Pluginを無効化しています...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        taxDataController = new TaxDataController(this).init();

        ExecuteTaxCollectTaskRunnable.initialize(this);
        new ExecuteTaxCollectTaskRunnable().runTask(this);

        Bukkit.getPluginManager().registerEvents(new UpdateSQLDataListener(this), this);

        Bukkit.getPluginCommand("governmentbudget").setExecutor(new GovernmentBudgetCommand(this));

        Bukkit.getLogger().info(getName() + " enabled.");
    }

    @Override
    public void onDisable() {
        if ( Bukkit.getOnlinePlayers().size() > 0 ) {
            Bukkit.getOnlinePlayers().forEach(p -> {
                taxDataController.updatePlayerData(p);
            });
        }
        enableUpdatePlayerData = false;
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
