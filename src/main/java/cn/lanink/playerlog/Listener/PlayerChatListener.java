package cn.lanink.playerlog.Listener;

import cn.lanink.playerlog.PlayerLog;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.scheduler.AsyncTask;
import com.smallaswater.easysql.mysql.data.SqlData;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerChatListener implements Listener {

    private final PlayerLog playerLog;
    private final boolean transcoding;

    public PlayerChatListener(PlayerLog playerLog, boolean transcoding) {
        this.playerLog = playerLog;
        this.transcoding = transcoding;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String chat = event.getMessage();
        if (player == null || chat == null || chat.trim().equals("")) return;
/*        if (this.transcoding) {
            try {
                chat = new String(chat.getBytes("GBK"), StandardCharsets.UTF_8);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }*/
        this.insertChatLog(player, chat);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String chat = event.getMessage();
        if (player == null || chat == null || chat.trim().equals("")) return;
/*        if (this.transcoding) {
            try {
                chat = new String(chat.getBytes("GBK"), StandardCharsets.UTF_8);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }*/
        this.insertChatLog(player, chat);
    }

    private void insertChatLog(final Player player, final String message) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.playerLog.getServer().getScheduler().scheduleAsyncTask(this.playerLog, new AsyncTask() {
            @Override
            public void onRun() {
                SqlData sqlData = new SqlData();
                sqlData.put("uuid", player.getUniqueId().toString());
                sqlData.put("name", player.getName());
                sqlData.put("chat", message);
                sqlData.put("time", time);
                playerLog.getSqlManager().insertData(playerLog.chatTable, sqlData);
            }
        });
    }

}
