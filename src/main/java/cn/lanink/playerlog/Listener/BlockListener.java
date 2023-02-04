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
import com.smallaswater.easysql.mysql.data.SqlData;
import com.smallaswater.easysql.mysql.data.SqlDataList;

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
        if (player == null || block == null) {
            return;
        }
        if (this.playerLog.queryPlayer.contains(player)) {
            event.setCancelled(true);
            this.query(player, block);
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        Block oldBlock = block.getLevel().getBlock(block);
        this.insertBlockLog("place", oldBlock, block, player);
    }

    /**
     * 玩家破坏方块事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player == null || block == null) {
            return;
        }
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
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        if (block == null) {
            return;
        }
        this.insertBlockLog("break", block, Block.get(0), "by@burn");
    }

    /**
     * 方块自然衰落消失事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onFade(BlockFadeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        if (block == null) {
            return;
        }
        this.insertBlockLog("break", block, Block.get(0), "by@fade");
    }

    private void insertBlockLog(final String operating, final Block oldBlock, final Block newBlock, final Player player) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.playerLog.getServer().getScheduler().scheduleAsyncTask(this.playerLog, new AsyncTask() {
            @Override
            public void onRun() {
                playerLog.getSqlManager().insertData(
                        playerLog.blockTable,
                        new SqlData()
                                .put("world", oldBlock.getLevel().getName())
                                .put("position", getStringPosition(oldBlock))
                                .put("operating", operating)
                                .put("oldblock", getStringID(oldBlock))
                                .put("newblock", getStringID(newBlock))
                                .put("uuid", player.getUniqueId().toString())
                                .put("name", player.getName())
                                .put("time", time)
                );
            }
        });
    }

    private void insertBlockLog(final String operating, final Block oldBlock, final Block newBlock, final String type) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.playerLog.getServer().getScheduler().scheduleAsyncTask(this.playerLog, new AsyncTask() {
            @Override
            public void onRun() {
                playerLog.getSqlManager().insertData(
                        playerLog.blockTable,
                        new SqlData()
                                .put("world", oldBlock.getLevel().getName())
                                .put("position", getStringPosition(oldBlock))
                                .put("operating", operating)
                                .put("oldblock", getStringID(oldBlock))
                                .put("newblock", getStringID(newBlock))
                                .put("uuid", "------------------------------------")
                                .put("name", type)
                                .put("time", time)
                );
            }
        });
    }

    private void query(final Player player, final Block block) {
        player.sendMessage("正在异步查询，请稍后...");
        this.playerLog.getServer().getScheduler().scheduleAsyncTask(this.playerLog, new AsyncTask() {
            @Override
            public void onRun() {
                SqlDataList<SqlData> datas = playerLog.getSqlManager().getData(
                        playerLog.blockTable,
                        "*",
                        new SqlData().put("world", block.getLevel().getName()).put("position", getStringPosition(block))
                );

                LinkedList<String> linkedList = new LinkedList<>();
                for (SqlData sqlData : datas) {
                    linkedList.add(sqlData.getString("operating") + "#" +
                            sqlData.getString("oldblock") + "#" +
                            sqlData.getString("newblock") + "#" +
                            sqlData.getString("uuid") + "#" +
                            sqlData.getString("name") + "#" +
                            sqlData.getString("time"));
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
