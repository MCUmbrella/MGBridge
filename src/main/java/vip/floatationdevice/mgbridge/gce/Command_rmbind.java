package vip.floatationdevice.mgbridge.gce;

import org.bukkit.Bukkit;
import vip.floatationdevice.guilded4j.object.ChatMessage;
import vip.floatationdevice.guilded4j.object.Embed;
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
            instance.sendGuildedEmbed(new Embed().setTitle(translate("g-unbind-success")).setColor(0x00ff00), msg.getId(), null, null);
            log.info(translate("c-unbind-success").replace("%PLAYER%", getPlayerName(removed)));
            saveBindMap();
            return true;
        }
        else// player not bound?
        {
            instance.sendGuildedEmbed(new Embed().setTitle(translate("g-no-bind")).setColor(0xff0000), msg.getId(), null, null);
            return false;
        }
    }
}
