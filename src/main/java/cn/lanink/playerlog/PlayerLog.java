package cn.lanink.playerlog;

import cn.lanink.playerlog.Listener.BlockListener;
import cn.lanink.playerlog.Listener.PlayerChatListener;
import cn.lanink.playerlog.Listener.PlayerListener;
import cn.lanink.playerlog.command.AdminCommand;
import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.smallaswater.easysql.api.SqlEnable;
import com.smallaswater.easysql.mysql.BaseMySql;
import com.smallaswater.easysql.mysql.manager.SqlManager;
import com.smallaswater.easysql.mysql.utils.TableType;
import com.smallaswater.easysql.mysql.utils.Types;
import com.smallaswater.easysql.mysql.utils.UserData;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

public class PlayerLog extends PluginBase {

    private static PlayerLog playerLog;
    private Config config;
    private SqlEnable sqlEnable;
    private SqlManager sqlManager;
    private Connection connection;
    public final String blockTitle = "blockLog";
    public final String playerTitle = "playerLog";
    public final String chatTitle = "playerChatLog";
    public ArrayList<Player> queryPlayer = new ArrayList<>();

    public static PlayerLog getInstance() {
        return playerLog;
    }

    @Override
    public void onEnable() {
        if (playerLog == null) playerLog = this;
        saveDefaultConfig();
        this.config = new Config(getDataFolder() + "/config.yml", 2);
        HashMap<String, Object> sqlConfig = this.config.get("MySQL", new HashMap<>());
        getLogger().info("§a正在尝试连接数据库，请稍后...");
        Types id = Types.INT;
        id.setValue("NOT NULL AUTO_INCREMENT PRIMARY KEY");
        Types uuid = Types.CHAR;
        uuid.setSize(36);
        Types time = Types.DATE;
        time.setSql("DATETIME");
        this.sqlEnable = new SqlEnable(this, this.blockTitle, new UserData(
                (String) sqlConfig.get("user"),
                (String) sqlConfig.get("passWorld"),
                (String) sqlConfig.get("host"),
                (Integer) sqlConfig.get("port"),
                (String) sqlConfig.get("database")),
                new TableType("id", id),
                new TableType("position", Types.VARCHAR),
                new TableType("world", Types.VARCHAR),
                new TableType("operating", Types.VARCHAR),
                new TableType("oldblock", Types.VARCHAR),
                new TableType("newblock", Types.VARCHAR),
                new TableType("uuid", uuid),
                new TableType("name", Types.VARCHAR),
                new TableType("time", time));
        this.sqlManager = this.sqlEnable.getManager();
        this.connection = this.sqlManager.getConnection();
        if (this.connection == null) {
            getLogger().error("数据库连接失败！请检查配置文件！");
            getPluginLoader().disablePlugin(this);
            return;
        }
        this.sqlManager.createTable(this.connection, this.playerTitle,
                BaseMySql.getDefaultTable(
                        new TableType("id", id),
                        new TableType("uuid", uuid),
                        new TableType("name", Types.VARCHAR),
                        new TableType("operating", Types.VARCHAR),
                        new TableType("position", Types.VARCHAR),
                        new TableType("world", Types.VARCHAR),
                        new TableType("time", time)));
        this.sqlManager.createTable(this.connection, this.chatTitle,
                BaseMySql.getDefaultTable(
                        new TableType("id", id),
                        new TableType("uuid", uuid),
                        new TableType("name", Types.VARCHAR),
                        new TableType("chat", Types.TEXT),
                        new TableType("time", time)));
        if (this.config.getBoolean("记录方块变化", true)) {
            getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        }
        if (this.config.getBoolean("记录玩家操作", false)) {
            getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        }
        if (this.config.getBoolean("记录玩家聊天信息", true)) {
            boolean transcoding = false;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                getLogger().warning("您的系统似乎是Windows，插件将尝试为聊天信息进行转码！");
                transcoding = true;
            }
            getServer().getPluginManager().registerEvents(new PlayerChatListener(this, transcoding), this);
        }
        getServer().getCommandMap().register("", new AdminCommand("playerlog"));
        getLogger().info("§a加载完成！");
    }

    @Override
    public void onDisable() {
        if (this.connection != null) {
            getLogger().info("§c正在断开数据库连接，请稍后...");
        }
        this.sqlEnable.disable();
        getLogger().info("§c已卸载！");
    }

    @Override
    public Config getConfig() {
        return this.config;
    }

    public SqlEnable getSqlEnable() {
        return this.sqlEnable;
    }

    public SqlManager getSqlManager() {
        return this.sqlManager;
    }

    public Connection getConnection() {
        return this.connection;
    }
}
