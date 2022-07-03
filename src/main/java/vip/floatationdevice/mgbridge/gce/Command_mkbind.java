package vip.floatationdevice.mgbridge.gce;

import org.bukkit.Bukkit;
import vip.floatationdevice.guilded4j.object.ChatMessage;
import vip.floatationdevice.guilded4j.object.Embed;
import vip.floatationdevice.mgbridge.GuildedCommandExecutor;
import vip.floatationdevice.mgbridge.event.UserBoundEvent;

import static vip.floatationdevice.mgbridge.BindManager.*;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.*;

public class Command_mkbind implements GuildedCommandExecutor
{
    @Override
    public String getCommandName(){return "mkbind";}

    @Override
    public String getDescription(){return translate("g-cmd-mkbind-desc");}

    @Override
    public String getUsage(){return "/mgb mkbind <CODE>";}

    @Override
    public boolean execute(ChatMessage msg, String[] args)
    {
        if(args.length != 1)// incorrect command format?
        {
            instance.sendGuildedEmbed(new Embed().setTitle(translate("g-usage")).setColor(0xff0000), msg.getId(), null, null);
            return false;
        }
        else// right usage?
        {
            if(bindMap.containsKey(msg.getCreatorId()))// player already bound?
            {
                instance.sendGuildedEmbed(new Embed().setTitle(translate("g-already-bound").replace("%PLAYER%", getPlayerName(bindMap.get(msg.getCreatorId())))).setColor(0xff0000), msg.getId(), null, null);
                return false;
            }
            else// player not bound?
            {
                if(pendingMap.containsKey(args[0]))// code matched?
                {// bind!
                    bindMap.put(msg.getCreatorId(), pendingMap.get(args[0]));
                    pendingPlayerMap.remove(pendingMap.get(args[0]));
                    pendingMap.remove(args[0]);
                    try
                    {
                        Bukkit.getPlayer(bindMap.get(msg.getCreatorId())).sendMessage(translate("m-bind-success"));
                    }
                    catch(NullPointerException ignored) {} // player is offline. ignore
                    instance.sendGuildedEmbed(new Embed().setTitle(translate("g-bind-success").replace("%PLAYER%", getPlayerName(bindMap.get(msg.getCreatorId())))).setColor(0x00ff00), msg.getId(), null, null);
                    log.info(translate("c-bind-success").replace("%PLAYER%", getPlayerName(bindMap.get(msg.getCreatorId()))));
                    saveBindMap();
                    Bukkit.getServer().getPluginManager().callEvent(new UserBoundEvent(msg.getCreatorId(), bindMap.get(msg.getCreatorId())));
                    return true;
                }
                else// code not in pending list?
                {
                    instance.sendGuildedEmbed(new Embed().setTitle(translate("invalid-code")).setColor(0xff0000), msg.getId(), null, null);
                    return false;
                }
            }
        }
    }
}
