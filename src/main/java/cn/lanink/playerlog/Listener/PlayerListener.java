package cn.lanink.playerlog.Listener;

import cn.lanink.playerlog.PlayerLog;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.scheduler.AsyncTask;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerListener implements Listener {

    private final PlayerLog playerLog;

    public PlayerListener(PlayerLog playerLog) {
        this.playerLog = playerLog;
    }

    /**
     * 玩家加入游戏事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        this.insertPlayerLog(player, "JoinGame");
    }

    /**
     * 玩家退出游戏事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        this.playerLog.queryPlayer.remove(player);
        this.playerLog.queryCache.remove(player);
        this.playerLog.uiCache.remove(player);
        this.insertPlayerLog(player, "QuitGame");
    }

    /**
     * 玩家更改游戏模式事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        int newGameMode = event.getNewGamemode();
        if (player == null) return;
        this.insertPlayerLog(player, "GameModeChange:" + newGameMode);
    }

    /**
     * 拾取方块事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickupItem(InventoryPickupItemEvent event) {
        if (event.isCancelled()) return;
        if (event.getInventory() instanceof PlayerInventory) {
            Player player = (Player) event.getInventory().getHolder();
            Item item = event.getItem().getItem();
            this.insertPlayerLog(player, "PickupItem:" + item.getId() + ":" + item.getDamage());
        }
    }

    /**
     * 玩家丢出物品事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (player == null || item == null) return;
        this.insertPlayerLog(player, "DropItem:" + item.getId() + ":" + item.getDamage());
    }

    /**
     * 玩家使用消耗品事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (player == null || item == null) return;
        this.insertPlayerLog(player, "UseItem:" + item.getId() + ":" + item.getDamage());
    }

    private void insertPlayerLog(final Player player, final String operating) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.playerLog.getServer().getScheduler().scheduleAsyncTask(this.playerLog, new AsyncTask() {
            @Override
            public void onRun() {
                String position = player.getFloorX() + ":" + player.getFloorY() + ":" + player.getFloorZ();
                try {
                    PreparedStatement preparedStatement = playerLog.getConnection()
                            .prepareStatement("insert into " + playerLog.playerTable +
                                    "(uuid, name, operating, position, world, time) values(?,?,?,?,?,?)");
                    preparedStatement.setString(1, player.getUniqueId().toString());
                    preparedStatement.setString(2, player.getName());
                    preparedStatement.setString(3, operating);
                    preparedStatement.setString(4, position);
                    preparedStatement.setString(5, player.getLevel().getName());
                    preparedStatement.setString(6, time);
                    preparedStatement.execute();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }

}
