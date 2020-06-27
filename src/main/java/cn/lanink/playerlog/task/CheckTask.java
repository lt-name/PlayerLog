package cn.lanink.playerlog.task;

import cn.lanink.playerlog.PlayerLog;
import cn.nukkit.scheduler.PluginTask;

import java.sql.SQLException;

public class CheckTask extends PluginTask<PlayerLog> {

    public CheckTask(PlayerLog owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        try {
            if (owner.getConnection() == null || owner.getConnection().isClosed()) {
                owner.linkMySQL();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
