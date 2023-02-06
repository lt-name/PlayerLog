package cn.lanink.playerlog.command.admin;

import cn.lanink.playerlog.command.base.BaseSubCommand;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import com.smallaswater.easysql.mysql.data.SqlData;

public class DelBlockLogCommand extends BaseSubCommand {

    public DelBlockLogCommand(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.hasPermission("PlayerLog.Command.DelBlockLog");
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 2) {
            String worldName = args[1];
            if (this.playerLog.getSqlManager().isExistsData(playerLog.blockTable, "world", worldName)) {
                this.playerLog.getSqlManager().deleteData(playerLog.blockTable, new SqlData().put("world", worldName));
                sender.sendMessage("§a世界 " + worldName + " 的记录已删除！");
            }else {
                sender.sendMessage("§c数据库中没有世界 " + worldName + " 的记录！");
            }
        }else {
            sender.sendMessage("§a/playerlog help §e查看帮助");
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { new CommandParameter("world", CommandParamType.TEXT, false) };
    }

}
