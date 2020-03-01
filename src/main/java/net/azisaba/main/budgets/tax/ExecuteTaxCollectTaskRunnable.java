package net.azisaba.main.budgets.tax;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.azisaba.main.budgets.GovernmentBudget;

import lombok.Getter;

public class ExecuteTaxCollectTaskRunnable extends BukkitRunnable {

    private static GovernmentBudget plugin;

    private static long nextExecute = -1L;
    @Getter
    private static BukkitTask task = null;

    public static void initialize(GovernmentBudget plugin) {
        ExecuteTaxCollectTaskRunnable.plugin = plugin;
    }

    @Override
    public void run() {
        if ( nextExecute < 0 ) {
            nextExecute = nextExecuteTime();
            Bukkit.getLogger().info("Next Execute: " + nextExecute);
        }

        if ( nextExecute - System.currentTimeMillis() < 500 ) {
            new TaxCollectTask(plugin).runTaskAsynchronously(plugin);

            nextExecute = -1L;
            task = new ExecuteTaxCollectTaskRunnable().runTaskLater(plugin, 20L * 10L);
            plugin.getLogger().info("Execute collect tax task after " + ticksToTime(20L * 10L));
            return;
        }

        long nextTick = (long) ((double) (nextExecute - System.currentTimeMillis())) / 150;
        Bukkit.getLogger().info("Next Tick: " + nextTick);
        if ( nextTick < 1 ) {
            nextTick = 1;
        } else if ( nextTick > 18000 ) {
            nextTick = 18000;
        }
        task = new ExecuteTaxCollectTaskRunnable().runTaskLater(plugin, nextTick);
        plugin.getLogger().info("Execute collect tax task after " + ticksToTime(nextTick));
    }

    private long nextExecuteTime() {
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.HOUR_OF_DAY, 0);

        while ( cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY || cal.before(now) ) {
            cal.add(Calendar.DATE, 1);
        }

        return cal.getTimeInMillis();
    }

    private String ticksToTime(long ticks) {
        return String.format("%.2f sec", ((double) ticks) / 20d);
    }
}
