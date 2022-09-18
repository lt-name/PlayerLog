package cn.lanink.playerlog.command;

import cn.lanink.playerlog.command.admin.DelBlockLogCommand;
import cn.lanink.playerlog.command.admin.QueryCommand;
import cn.lanink.playerlog.command.base.BaseCommand;
import cn.nukkit.command.CommandSender;

public class AdminCommand extends BaseCommand {

    public AdminCommand(String name) {
        super(name, "管理命令");
        this.addSubCommand(new QueryCommand("query"));
        this.addSubCommand(new DelBlockLogCommand("delblocklog"));
        this.loadCommandBase();
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage("§a/playerlog query §e进入查询模式");
        sender.sendMessage("§a/playerlog delblocklog 世界 §e删除指定世界的方块操作记录");
    }

}
