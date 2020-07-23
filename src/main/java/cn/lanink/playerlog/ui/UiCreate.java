package cn.lanink.playerlog.ui;

import cn.lanink.playerlog.PlayerLog;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author lt_name
 */
public class UiCreate {

    public static void showQueryBlockLog(Player player, int pages) {
        LinkedList<LinkedList<String>> cache = PlayerLog.getInstance().queryCache.get(player);
        pages--;
        if (cache != null && cache.size() > pages) {
            StringBuilder s = new StringBuilder();
            for (String string : cache.get(pages)) {
                s.append(string).append("\n\n");
            }
            FormWindowSimple simple = new FormWindowSimple("§9记录--第§e" + (pages + 1) + "§e§9页/共" + cache.size() + "页", s.toString());
            if (pages > 0) {
                simple.addButton(new ElementButton("上一页"));
            }
            if ((pages + 1) < cache.size()) {
                simple.addButton(new ElementButton("下一页"));
            }
            showFormWindow(player, simple, UiType.queryBlockLog);
        }
    }

    public static void showFormWindow(Player player, FormWindow formWindow, UiType uiType) {
        HashMap<Integer, UiType> map;
        if (!PlayerLog.getInstance().uiCache.containsKey(player)) {
            map = new HashMap<>();
            PlayerLog.getInstance().uiCache.put(player, map);
        }else {
            map = PlayerLog.getInstance().uiCache.get(player);
        }
        map.put(player.showFormWindow(formWindow), uiType);
    }

}
