package net.azisaba.main.budgets.command;

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

//
//        ------[Debug]------
//
//        if ( args[0].equalsIgnoreCase("import") && sender instanceof Player ) {
//            Player p = (Player) sender;
//            new Thread() {
//                public void run() {
//                    Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
//                    List<User> users = ess.getUserMap().getAllUniqueUsers().stream()
//                            .map(uuid -> ess.getUser(uuid))
//                            .collect(Collectors.toList());
//                    int changed = plugin.getTaxDataController().addPlayerData(users);
//
//                    p.sendMessage("Changed: " + changed);
//                }
//            }.start();
//            return true;
//        }
//
//        if ( args[0].equalsIgnoreCase("execute") ) {
//            new Thread() {
//                public void run() {
//                    new TaxCollectTask(plugin).runTaskAsynchronously(plugin);
//                }
//            }.start();
//
//            return true;
//        }
//
//        if ( args[0].equalsIgnoreCase("check") ) {
//            sender.sendMessage("Balance: " + plugin.getBank().getBalance().toString());
//            return true;
//        }
//
//      ------[Debug]------
//
        return true;
    }
}
