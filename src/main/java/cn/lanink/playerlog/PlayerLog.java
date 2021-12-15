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
import com.smallaswater.easysql.mysql.data.SqlData;
import com.smallaswater.easysql.mysql.data.SqlDataList;
import com.smallaswater.easysql.mysql.utils.TableType;
import com.smallaswater.easysql.mysql.utils.Types;
import com.smallaswater.easysql.mysql.utils.UserData;
import com.smallaswater.easysql.v3.mysql.manager.SqlManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class PlayerLog extends PluginBase {

    public static String VERSION = "1.0.2-SNAPSHOT git-2a13a36";
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
        this.config = new Config(getDataFolder() + "/config.yml", 2);
        this.linkMySQL();
        if (!this.sqlManager.isEnable()) {
            getLogger().error("§c数据库连接失败！请检查配置文件！");
            getPluginLoader().disablePlugin(this);
            return;
        }
        PreparedStatement preparedStatement;
        try {

            final SqlDataList<SqlData> data = this.sqlManager.getData("select * from " + this.blockTable + " where id = 1 or 1=1");
            for (SqlData data1 : data) {
                this.getLogger().info(data1.getString("position"));
            }

            if (!this.sqlManager.isExistTable(this.blockTable)) {
                this.getLogger().info("未找到表 " + this.blockTable + " 正在创建");
                this.sqlManager.createTable(this.blockTable,
                        new TableType("id", Types.ID),
                        new TableType("world", Types.VARCHAR),
                        new TableType("position", Types.VARCHAR),
                        new TableType("operating", Types.VARCHAR),
                        new TableType("oldblock", Types.VARCHAR),
                        new TableType("newblock", Types.VARCHAR),
                        new TableType("uuid", Types.CHAR.setSize(36)),
                        new TableType("name", Types.VARCHAR),
                        new TableType("time", Types.DATETIME));
            }
            if (!this.sqlManager.isExistTable(this.playerTable)) {
                getLogger().info("未找到表 " + this.playerTable + " 正在创建");
                preparedStatement = this.sqlManager.getConnection().prepareStatement("create table " + this.playerTable +
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
            if (!this.sqlManager.isExistTable(this.chatTable)) {
                getLogger().info("未找到表 " + this.chatTable + " 正在创建");
                preparedStatement = this.sqlManager.getConnection().prepareStatement("create table " + this.chatTable +
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
