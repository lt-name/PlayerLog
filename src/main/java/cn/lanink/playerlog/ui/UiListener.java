package cn.lanink.playerlog.ui;

import cn.lanink.playerlog.PlayerLog;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.FormWindowSimple;

/**
 * @author lt_name
 */
public class UiListener implements Listener {

    private final PlayerLog playerLog;

    public UiListener(PlayerLog playerLog) {
        this.playerLog = playerLog;
    }

    @EventHandler
    public void onFormResponded(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getWindow() == null || event.getResponse() == null) {
            return;
        }
        UiType uiType = this.playerLog.uiCache.containsKey(player) ? this.playerLog.uiCache.get(player).get(event.getFormID()) : null;
        if (uiType == null) return;
        this.playerLog.uiCache.get(player).remove(event.getFormID());
        if (event.getWindow() instanceof FormWindowSimple) {
            FormWindowSimple simple = (FormWindowSimple) event.getWindow();
            if (uiType == UiType.queryBlockLog) {
                int pages = Integer.parseInt(simple.getTitle().split("§e")[1]);
                switch (simple.getResponse().getClickedButtonId()) {
                    case 0:
                        if (pages <= 1) {
                            pages++;
                        }else {
                            pages--;
                        }
                        break;
                    case 1:
                    default:
                        pages++;
                        break;
                }
                UiCreate.showQueryBlockLog(player, pages);
            }
        }
    }

}
