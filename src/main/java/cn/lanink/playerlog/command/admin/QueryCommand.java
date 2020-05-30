package cn.lanink.playerlog.command.admin;

import cn.lanink.playerlog.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class QueryCommand extends BaseSubCommand {

    public QueryCommand(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer() && sender.isOp();
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (this.playerLog.queryPlayer.contains(player)) {
            player.sendMessage("已退出查询模式！");
            while (this.playerLog.queryPlayer.contains(player)) {
                this.playerLog.queryPlayer.remove(player);
            }
        }else {
            player.sendMessage("已进入查询模式！");
            player.sendMessage("提示:请在要查询的地方放置或破坏方块！");
            this.playerLog.queryPlayer.add(player);
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
