package net.azisaba.main.budgets.command;

import java.math.BigDecimal;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.azisaba.main.budgets.GovernmentBudget;
import net.md_5.bungee.api.ChatColor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GovernmentBudgetCommand implements CommandExecutor {

    private final GovernmentBudget plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ( args.length <= 0 ) {
            sender.sendMessage(ChatColor.RED + "使い方が違うよ！");
            return true;
        }

        if ( args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("take") ) {
            BigDecimal value = new BigDecimal(args[1]);
            if ( args[0].equalsIgnoreCase("add") ) {
                plugin.getBank().deposit(value, "test deposit!");
            } else {
                plugin.getBank().withdraw(value, "test withdraw!");
            }
        }
        return true;
    }
}
