package vip.floatationdevice.mgbridge.gce;

import org.bukkit.Bukkit;
import vip.floatationdevice.guilded4j.object.ChatMessage;
import vip.floatationdevice.mgbridge.GuildedCommandExecutor;

import java.util.UUID;

import static vip.floatationdevice.mgbridge.BindManager.bindMap;
import static vip.floatationdevice.mgbridge.BindManager.saveBindMap;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.*;
import static vip.floatationdevice.mgbridge.MGBridge.instance;

public class Command_rmbind implements GuildedCommandExecutor
{
    @Override
    public String getCommandName()
    {
        return "rmbind";
    }

    @Override
    public boolean execute(ChatMessage msg, String[] args)
    {
        if(bindMap.containsKey(msg.getCreatorId()))// player bound?
        {
            try
            {
                Bukkit.getPlayer(bindMap.get(msg.getCreatorId())).sendMessage(translate("m-unbind-success"));
            }
            catch(Exception ignored) {}
            UUID removed = bindMap.remove(msg.getCreatorId());
            instance.sendGuildedMsg(translate("g-unbind-success"), msg.getId());
            log.info(translate("c-unbind-success").replace("%PLAYER%", getPlayerName(removed)));
            saveBindMap();
            return true;
        }
        else// player not bound?
        {
            instance.sendGuildedMsg(translate("g-no-bind"), msg.getId());
            return false;
        }
    }
}
