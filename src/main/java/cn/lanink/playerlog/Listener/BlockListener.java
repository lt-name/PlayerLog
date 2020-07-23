package cn.lanink.playerlog.Listener;

import cn.lanink.playerlog.PlayerLog;
import cn.lanink.playerlog.ui.UiCreate;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockBurnEvent;
import cn.nukkit.event.block.BlockFadeEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.scheduler.AsyncTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

public class BlockListener implements Listener {

    private final PlayerLog playerLog;

    public BlockListener(PlayerLog playerLog) {
        this.playerLog = playerLog;
    }

    /**
     * 玩家放置方块事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player == null || block == null) return;
        if (this.playerLog.queryPlayer.contains(player)) {
            event.setCancelled(true);
            this.query(player, block);
            return;
        }
        if (event.isCancelled()) return;
        Block oldBlock = block.getLevel().getBlock(block);
        this.insertBlockLog("place", oldBlock, block, player);
    }

    /**
     * 玩家破坏方块事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player == null || block == null) return;
        if (this.playerLog.queryPlayer.contains(player)) {
            event.setCancelled(true);
            this.query(player, block);
            return;
        }
        this.insertBlockLog("break", block, Block.get(0), player);
    }

    /**
     * 方块被火烧掉事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBurn(BlockBurnEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        if (block == null) return;
        this.insertBlockLog("break", block, Block.get(0), "by@burn");
    }

    /**
     * 方块自然衰落消失事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onFade(BlockFadeEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        if (block == null) return;
        this.insertBlockLog("break", block, Block.get(0), "by@fade");
    }

    private void insertBlockLog(final String operating, final Block oldBlock, final Block newBlock, final Player player) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.playerLog.getServer().getScheduler().scheduleAsyncTask(this.playerLog, new AsyncTask() {
            @Override
            public void onRun() {
                try {
                    PreparedStatement preparedStatement = playerLog.getConnection()
                            .prepareStatement("insert into " + playerLog.blockTable + "(world, position, operating, oldblock, newblock, uuid, name, time) values(?,?,?,?,?,?,?,?)");
                    preparedStatement.setString(1, oldBlock.getLevel().getName());
                    preparedStatement.setString(2, getStringPosition(oldBlock));
                    preparedStatement.setString(3, operating);
                    preparedStatement.setString(4, getStringID(oldBlock));
                    preparedStatement.setString(5, getStringID(newBlock));
                    preparedStatement.setString(6, player.getUniqueId().toString());
                    preparedStatement.setString(7, player.getName());
                    preparedStatement.setString(8, time);
                    preparedStatement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void insertBlockLog(final String operating, final Block oldBlock, final Block newBlock, final String type) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.playerLog.getServer().getScheduler().scheduleAsyncTask(this.playerLog, new AsyncTask() {
            @Override
            public void onRun() {
                try {
                    PreparedStatement preparedStatement = playerLog.getConnection()
                            .prepareStatement("insert into " + playerLog.blockTable + "(world, position, operating, oldblock, newblock, uuid, name, time) values(?,?,?,?,?,?,?,?)");
                    preparedStatement.setString(1, oldBlock.getLevel().getName());
                    preparedStatement.setString(2, getStringPosition(oldBlock));
                    preparedStatement.setString(3, operating);
                    preparedStatement.setString(4, getStringID(oldBlock));
                    preparedStatement.setString(5, getStringID(newBlock));
                    preparedStatement.setString(6, "------------------------------------");
                    preparedStatement.setString(7, type);
                    preparedStatement.setString(8, time);
                    preparedStatement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void query(final Player player, final Block block) {
        player.sendMessage("正在异步查询，请稍后...");
        this.playerLog.getServer().getScheduler().scheduleAsyncTask(this.playerLog, new AsyncTask() {
            @Override
            public void onRun() {
                LinkedList<String> linkedList = new LinkedList<>();
                try {
                    PreparedStatement preparedStatement = playerLog.getConnection()
                            .prepareStatement("select operating,oldblock,newblock,uuid,name,time from " +
                                    playerLog.blockTable + " where world = ? and position = ?");
                    preparedStatement.setString(1, block.getLevel().getName());
                    preparedStatement.setString(2, getStringPosition(block));
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        linkedList.add(resultSet.getString("operating") + "#" +
                                resultSet.getString("oldblock") + "#" +
                                resultSet.getString("newblock") + "#" +
                                resultSet.getString("uuid") + "#" +
                                resultSet.getString("name") + "#" +
                                resultSet.getString("time"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (linkedList.size() > 0) {
                    Collections.reverse(linkedList);
                    LinkedList<LinkedList<String>> cache = new LinkedList<>();
                    LinkedList<String> send = new LinkedList<>();
                    for (String string : linkedList) {
                        if (send.size() >= 20) {
                            cache.add(send);
                            send = new LinkedList<>();
                        }
                        String[] s = string.split("#");
                        String name;
                        if (s[3].matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
                            UUID uuid = UUID.fromString(s[3]);
                            name = "§f玩家§a" + playerLog.getServer().getOfflinePlayer(uuid).getName();
                        }else {
                            name = "§f非玩家操作:§c" + s[4].split("@")[1];
                        }
                        send.add(name + " §f操作:§e" + s[0] + " §f旧方块:§c" + s[1] + " §f新方块:§a" + s[2] + " §f时间:§b" + s[5]);
                    }
                    if (cache.size() == 0) {
                        cache.add(send);
                    }
                    playerLog.queryCache.put(player, cache);
                    UiCreate.showQueryBlockLog(player, 1);
                }else {
                    player.sendMessage("此方块没有操作记录！");
                }
            }
        });
    }

    private String getStringPosition(Block block) {
        return block.getFloorX() + ":" + block.getFloorY() + ":" + block.getFloorZ();
    }

    private String getStringID(Block block) {
        return block.getId() + ":" + block.getDamage();
    }

}
