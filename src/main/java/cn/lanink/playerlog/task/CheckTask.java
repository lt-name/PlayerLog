package cn.lanink.playerlog.task;

import cn.lanink.playerlog.PlayerLog;
import cn.nukkit.Server;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.scheduler.Task;

import java.sql.SQLException;

public class CheckTask extends PluginTask<PlayerLog> {

    public CheckTask(PlayerLog owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        try {
            if (owner.getConnection() == null ||
                    owner.getConnection().isClosed() ||
                    !owner.getConnection().isValid(10)) {
                //主线程重连MySQL
                Server.getInstance().getScheduler().scheduleDelayedTask(owner, new Task() {
                    @Override
                    public void onRun(int i) {
                        owner.linkMySQL();
                    }
                }, 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
