package net.azisaba.main.budgets.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.azisaba.main.budgets.GovernmentBudget;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateSQLDataListener implements Listener {

    private final GovernmentBudget plugin;

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                new Thread() {
                    public void run() {
                        if ( plugin.isEnableUpdatePlayerData() ) {
                            plugin.getTaxDataController().updatePlayerData(e.getPlayer());
                        }
                    }
                }.start();
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                new Thread() {
                    public void run() {
                        if ( plugin.isEnableUpdatePlayerData() ) {
                            plugin.getTaxDataController().updatePlayerData(e.getPlayer());
                        }
                    }
                }.start();
            }
        });
    }
}
