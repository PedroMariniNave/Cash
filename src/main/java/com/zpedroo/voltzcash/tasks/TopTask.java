package com.zpedroo.voltzcash.tasks;

import com.zpedroo.voltzcash.VoltzCash;
import com.zpedroo.voltzcash.managers.DataManager;
import com.zpedroo.voltzcash.mysql.DBConnection;
import org.bukkit.scheduler.BukkitRunnable;

import static com.zpedroo.voltzcash.utils.config.Settings.TOP_UPDATE;

public class TopTask extends BukkitRunnable {

    public TopTask(VoltzCash voltzCash) {
        this.runTaskTimerAsynchronously(voltzCash, TOP_UPDATE, TOP_UPDATE);
    }

    @Override
    public void run() {
        DataManager.getInstance().saveAll();
        DataManager.getInstance().getDataCache().setTop(DBConnection.getInstance().getDBManager().getTop());
    }
}