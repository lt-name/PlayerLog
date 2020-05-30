package cn.lanink.playerlog.command;

import cn.lanink.playerlog.command.admin.QueryCommand;
import cn.lanink.playerlog.command.base.BaseCommand;
import cn.nukkit.command.CommandSender;

public class AdminCommand extends BaseCommand {

    public AdminCommand(String name) {
        super(name, "管理命令");
        this.setPermission("PlayerLog.command.admin");
        this.addSubCommand(new QueryCommand("query"));
        this.loadCommandBase();
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage("§a/playerlog query §e进入查询模式");
    }

}
