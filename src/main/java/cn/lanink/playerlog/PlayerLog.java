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
import ru.nukkit.dblib.DbLib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class PlayerLog extends PluginBase {

    public static String VERSION = "?";
    private static PlayerLog playerLog;
    private Config config;
    private Connection connection;
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
        if (playerLog == null) playerLog = this;
        saveDefaultConfig();
        getLogger().info("版本：" + VERSION);
        this.config = new Config(getDataFolder() + "/config.yml", 2);
        this.linkMySQL();
        if (this.connection == null) {
            getLogger().error("§c数据库连接失败！请检查配置文件！");
            getPluginLoader().disablePlugin(this);
            return;
        }
        ResultSet resultSet;
        PreparedStatement preparedStatement;
        try {
            resultSet = connection.getMetaData().getTables(null, null, this.blockTable, null);
            if (!resultSet.next()) {
                getLogger().info("未找到表 " + this.blockTable + " 正在创建");
                preparedStatement = connection.prepareStatement("create table " + this.blockTable +
                        "(id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                        "world VARCHAR(255) NOT NULL," +
                        "position VARCHAR(255) NOT NULL, " +
                        "operating VARCHAR(255) NOT NULL, " +
                        "oldblock VARCHAR(255) NOT NULL, " +
                        "newblock VARCHAR(255) NOT NULL, " +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "name VARCHAR(255) NOT NULL, " +
                        "time DATETIME NOT NULL, " +
                        "INDEX(world), INDEX(position), INDEX(uuid)" +
                        ")ENGINE=InnoDB DEFAULT CHARSET=utf8");
                preparedStatement.execute();
            }
            resultSet = connection.getMetaData().getTables(null, null, this.playerTable, null);
            if (!resultSet.next()) {
                getLogger().info("未找到表 " + this.playerTable + " 正在创建");
                preparedStatement = connection.prepareStatement("create table " + this.playerTable +
                        "(id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "name VARCHAR(255) NOT NULL, " +
                        "operating VARCHAR(255) NOT NULL, " +
                        "position VARCHAR(255) NOT NULL, " +
                        "world VARCHAR(255) NOT NULL, " +
                        "time DATETIME NOT NULL, " +
                        "INDEX(uuid), INDEX(world)" +
                        ")ENGINE=InnoDB DEFAULT CHARSET=utf8");
                preparedStatement.execute();
            }
            resultSet = connection.getMetaData().getTables(null, null, this.chatTable, null);
            if (!resultSet.next()) {
                getLogger().info("未找到表 " + this.chatTable + " 正在创建");
                preparedStatement = connection.prepareStatement("create table " + this.chatTable +
                        "(id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "name VARCHAR(255) NOT NULL, " +
                        "chat TEXT NOT NULL, " +
                        "time DATETIME NOT NULL, " +
                        "INDEX(uuid)" +
                        ")ENGINE=InnoDB DEFAULT CHARSET=utf8");
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        if (this.connection != null) {
            getLogger().info("§c正在断开数据库连接，请稍后...");
            try {
                this.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        getLogger().info("§c已卸载！");
    }

    public void linkMySQL() {
        getLogger().info("§a正在尝试连接数据库，请稍后...");
        HashMap<String, Object> sqlConfig = this.config.get("MySQL", new HashMap<>());
        this.connection = DbLib.getMySqlConnection("jdbc:mysql://" + sqlConfig.get("host") + ':' +
                        sqlConfig.get("port") + '/' +
                        sqlConfig.get("database") + "?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8",
                (String) sqlConfig.get("user"),
                (String) sqlConfig.get("passWorld"));
    }

    @Override
    public Config getConfig() {
        return this.config;
    }

    public Connection getConnection() {
        return this.connection;
    }
}
