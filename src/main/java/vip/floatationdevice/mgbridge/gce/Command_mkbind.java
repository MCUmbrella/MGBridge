package vip.floatationdevice.mgbridge.gce;

import org.bukkit.Bukkit;
import vip.floatationdevice.guilded4j.object.ChatMessage;
import vip.floatationdevice.mgbridge.GuildedCommandExecutor;

import static vip.floatationdevice.mgbridge.BindManager.*;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.*;

public class Command_mkbind implements GuildedCommandExecutor
{
    @Override
    public String getCommandName()
    {
        return "mkbind";
    }

    @Override
    public boolean execute(ChatMessage msg, String[] args)
    {
        if(args.length != 1)// incorrect command format?
        {
            instance.sendGuildedMsg(translate("g-usage"), msg.getId());
            return false;
        }
        else// right usage?
        {
            if(bindMap.containsKey(msg.getCreatorId()))// player already bound?
            {
                instance.sendGuildedMsg(translate("g-already-bound").replace("%PLAYER%", getPlayerName(bindMap.get(msg.getCreatorId()))), msg.getId());
                return false;
            }
            else// player not bound?
            {
                if(pendingMap.containsKey(args[0]))// code matched?
                {
                    bindMap.put(msg.getCreatorId(), pendingMap.get(args[0]));
                    pendingPlayerMap.remove(pendingMap.get(args[0]));
                    pendingMap.remove(args[0]);
                    try
                    {
                        Bukkit.getPlayer(bindMap.get(msg.getCreatorId())).sendMessage(translate("m-bind-success"));
                    }
                    catch(NullPointerException ignored) {}
                    instance.sendGuildedMsg(translate("g-bind-success").replace("%PLAYER%", getPlayerName(bindMap.get(msg.getCreatorId()))), msg.getId());
                    log.info(translate("c-bind-success").replace("%PLAYER%", getPlayerName(bindMap.get(msg.getCreatorId()))));
                    saveBindMap();
                    return true;
                }
                else// code not in pending list?
                {
                    instance.sendGuildedMsg(translate("invalid-code"), msg.getId());
                    return false;
                }
            }
        }
    }
}
