package net.azisaba.main.budgets;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.azisaba.main.budgets.bank.GovernmentBank;
import net.azisaba.main.budgets.bank.SQLDatabaseBank;
import net.azisaba.main.budgets.command.GovernmentBudgetCommand;
import net.azisaba.main.budgets.config.DefaultConfig;

import lombok.Getter;

public class GovernmentBudget extends JavaPlugin {

    @Getter
    private DefaultConfig defaultConfig;
    @Getter
    private GovernmentBank bank = null;

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
}
