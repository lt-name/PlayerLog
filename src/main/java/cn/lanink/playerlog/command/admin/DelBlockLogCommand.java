package cn.lanink.playerlog.command.admin;

import cn.lanink.playerlog.command.base.BaseSubCommand;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DelBlockLogCommand extends BaseSubCommand {

    public DelBlockLogCommand(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 2) {
            boolean isHave = false;
            try {
                PreparedStatement preparedStatement = playerLog.getConnection()
                        .prepareStatement("select world from " + playerLog.blockTable + " where world = ?");
                preparedStatement.setString(1, args[1]);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    isHave = resultSet.getString("world") != null;
                }
                if (isHave) {
                    preparedStatement = playerLog.getConnection()
                            .prepareStatement("delete from " + playerLog.blockTable + " where world = ?");
                    preparedStatement.setString(1, args[1]);
                    preparedStatement.execute();
                    sender.sendMessage("§a世界 " + args[1] + " 的记录已删除！");
                }else {
                    sender.sendMessage("§c数据库中没有世界 " + args[1] + " 的记录！");
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
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
