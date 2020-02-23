package net.azisaba.main.budgets.command;

import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.google.common.base.Strings;

import net.azisaba.main.budgets.GovernmentBudget;
import net.azisaba.main.budgets.utils.Args;
import net.azisaba.main.budgets.utils.Chat;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GovernmentBudgetCommand implements CommandExecutor {

    private final GovernmentBudget plugin;

    private boolean importing = false;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ( args.length <= 0 ) {
            sendHelpMessage(sender, label);
            return true;
        }

        if ( Args.check(args, 0, "bal", "balance") ) {
            sender.sendMessage(Chat.f("&e現在の予算&a: &c{0}円", plugin.getBank().getBalance().setScale(2, RoundingMode.DOWN).toString()));
            return true;
        }

        if ( Args.check(args, 0, "import") ) {
            if ( importing ) {
                sender.sendMessage(Chat.f("&c現在Import中です！"));
                return true;
            }

            if ( Args.check(args, 1, "ess", "essentials") ) {
                sender.sendMessage(Chat.f("&eEssentialsからプレイヤーデータをImportしています..."));
                importing = true;

                new Thread() {
                    public void run() {
                        try {
                            if ( Bukkit.getPluginManager().getPlugin("Essentials") == null ) {
                                sender.sendMessage(Chat.f("&cエラー: Essentialsが導入されていません！"));
                                return;
                            }

                            Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                            List<User> users = ess.getUserMap().getAllUniqueUsers().stream()
                                    .map(ess::getUser)
                                    .collect(Collectors.toList());
                            int changed = plugin.getTaxDataController().addPlayerData(users);

                            if ( changed >= 0 ) {
                                sender.sendMessage(Chat.f("&e{0}&a人のデータをImportしました！", changed));
                            } else {
                                sender.sendMessage(Chat.f("&cデータのImportに失敗しました！"));
                            }
                        } catch ( Exception e ) {
                            e.printStackTrace();
                        } finally {
                            importing = false;
                        }
                    }
                }.start();
            } else {
                sender.sendMessage(Chat.f("&c使用可能なImport: &eEssentials"));
            }
            return true;
        }

        sendHelpMessage(sender, label);

        return true;
    }

    private void sendHelpMessage(CommandSender sender, String label) {
        StringBuilder builder = new StringBuilder(Chat.f("&b{0}", Strings.repeat("━", 60))).append("\n");
        builder.append(Chat.f("&e/{0} balance &7- &a予算を確認します", label)).append("\n");
        builder.append(Chat.f("&e/{0} import &7- &a別Pluginからプレイヤーデータを読み込みます", label)).append("\n");
        builder.append(Chat.f("&b{0}", Strings.repeat("━", 60)));

        sender.sendMessage(builder.toString());
    }
}
