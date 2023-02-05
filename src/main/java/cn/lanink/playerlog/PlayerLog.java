package cn.lanink.playerlog;

import cn.lanink.playerlog.Listener.BlockListener;
import cn.lanink.playerlog.Listener.PlayerChatListener;
import cn.lanink.playerlog.Listener.PlayerListener;
import cn.lanink.playerlog.command.AdminCommand;
import cn.lanink.playerlog.task.CheckTask;
import cn.lanink.playerlog.ui.UiListener;
import cn.lanink.playerlog.ui.UiType;
import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.smallaswater.easysql.exceptions.MySqlLoginException;
import com.smallaswater.easysql.mysql.utils.DataType;
import com.smallaswater.easysql.mysql.utils.TableType;
import com.smallaswater.easysql.mysql.utils.UserData;
import com.smallaswater.easysql.v3.mysql.manager.SqlManager;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class PlayerLog extends PluginBase {

    public static String VERSION = "1.0.2-SNAPSHOT git-f9d7a02";
    private static PlayerLog playerLog;
    private Config config;

    private SqlManager sqlManager;

    public final String blockTable = "blockLog";
    public final String playerTable = "playerLog";
    public final String chatTable = "playerChatLog";
    public HashSet<Player> queryPlayer = new HashSet<>(); //在查询模式的玩家
    public HashMap<Player, LinkedList<LinkedList<String>>> queryCache = new HashMap<>(); //查询结果缓存
    public HashMap<Player, HashMap<Integer, UiType>> uiCache = new HashMap<>(); //ui缓存

    public static PlayerLog getInstance() {
        return playerLog;
    }

    @Override
    public void onEnable() {
        if (playerLog == null) {
            playerLog = this;
        }
        saveDefaultConfig();
        getLogger().info("版本：" + VERSION);
        this.config = new Config(getDataFolder() + "/config.yml", Config.YAML);
        this.linkMySQL();
        if (!this.sqlManager.isEnable()) {
            getLogger().error("§c数据库连接失败！请检查配置文件！");
            getPluginLoader().disablePlugin(this);
            return;
        }
        try {
            if (!this.sqlManager.isExistTable(this.blockTable)) {
                this.getLogger().info("未找到表 " + this.blockTable + " 正在创建");
                this.sqlManager.createTable(this.blockTable,
                        new TableType("id", DataType.getID()),
                        new TableType("world", DataType.getVARCHAR(), true),
                        new TableType("position", DataType.getVARCHAR(), true),
                        new TableType("operating", DataType.getVARCHAR()),
                        new TableType("oldblock", DataType.getVARCHAR()),
                        new TableType("newblock", DataType.getVARCHAR()),
                        new TableType("uuid", DataType.getUUID()),
                        new TableType("name", DataType.getVARCHAR()),
                        new TableType("time", DataType.getDATETIME()));
            }
            if (!this.sqlManager.isExistTable(this.playerTable)) {
                this.getLogger().info("未找到表 " + this.playerTable + " 正在创建");
                this.sqlManager.createTable(this.playerTable,
                        new TableType("id", DataType.getID()),
                        new TableType("uuid", DataType.getUUID(), true),
                        new TableType("name", DataType.getVARCHAR()),
                        new TableType("operating", DataType.getVARCHAR()),
                        new TableType("position", DataType.getVARCHAR()),
                        new TableType("world", DataType.getVARCHAR(), true),
                        new TableType("time", DataType.getDATETIME()));
            }
            if (!this.sqlManager.isExistTable(this.chatTable)) {
                this.getLogger().info("未找到表 " + this.chatTable + " 正在创建");
                this.sqlManager.createTable(this.chatTable,
                        new TableType("id", DataType.getID()),
                        new TableType("uuid", DataType.getUUID(), true),
                        new TableType("name", DataType.getVARCHAR()),
                        new TableType("chat", DataType.getTEXT()),
                        new TableType("time", DataType.getDATETIME()));
            }
        } catch (Exception e) {
            this.getLogger().error("§c创建表失败！请检查配置文件或数据库配置！", e);
        }
        if (this.config.getBoolean("记录方块变化", true)) {
            getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        }
        if (this.config.getBoolean("记录玩家操作", false)) {
            getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        }
        if (this.config.getBoolean("记录玩家聊天信息", true)) {
            boolean transcoding = false;
/*            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                getLogger().warning("您的系统似乎是Windows，插件将尝试为聊天信息进行转码！");
                transcoding = true;
            }*/
            getServer().getPluginManager().registerEvents(new PlayerChatListener(this, transcoding), this);
        }
        getServer().getPluginManager().registerEvents(new UiListener(this), this);
        getServer().getCommandMap().register("", new AdminCommand("playerlog"));
        getServer().getScheduler().scheduleRepeatingTask(this, new CheckTask(this), 1200, true);
        getLogger().info("§a加载完成！");
    }

    @Override
    public void onDisable() {
        if (this.sqlManager != null) {
            this.getLogger().info("§c正在断开数据库连接，请稍后...");
            this.sqlManager.disable();
        }
        this.getLogger().info("§c已卸载！");
    }

    public void linkMySQL() {
        getLogger().info("§a正在尝试连接数据库，请稍后...");
        HashMap<String, Object> sqlConfig = this.config.get("MySQL", new HashMap<>());
        try {
            this.sqlManager = new SqlManager(this,
                    new UserData(
                            (String) sqlConfig.get("user"),
                            (String) sqlConfig.get("passWorld"),
                            (String) sqlConfig.get("host"),
                            (int) sqlConfig.get("port"),
                            (String) sqlConfig.get("database")));
        } catch (MySqlLoginException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Config getConfig() {
        return this.config;
    }

    public SqlManager getSqlManager() {
        return this.sqlManager;
    }

    public Connection getConnection() {
        return this.sqlManager.getConnection();
    }
}
