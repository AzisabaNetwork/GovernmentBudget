package net.azisaba.main.budgets.tax;

import java.util.Calendar;

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
        }

        if ( nextExecute - System.currentTimeMillis() < 500 ) {
            new TaxCollectTask(plugin).runTaskAsynchronously(plugin);

            nextExecute = -1L;
            task = new ExecuteTaxCollectTaskRunnable().runTaskLater(plugin, 20L * 10L);
            return;
        }

        long nextTick = (long) ((double) (nextExecute - System.currentTimeMillis())) / 150;
        if ( nextTick < 1 ) {
            nextTick = 1;
        } else if ( nextTick > 9000 ) {
            nextTick = 9000;
        }
        task = new ExecuteTaxCollectTaskRunnable().runTaskLater(plugin, nextTick);
    }

    private long nextExecuteTime() {
        Calendar cal = Calendar.getInstance();
        int nextFor = Calendar.SATURDAY - cal.get(Calendar.DAY_OF_WEEK);
        while ( nextFor <= 0 ) {
            nextFor += 7;
        }
        cal.add(Calendar.DATE, nextFor);
        cal.clear(Calendar.MILLISECOND);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.HOUR_OF_DAY);

        return cal.getTimeInMillis();
    }
}
