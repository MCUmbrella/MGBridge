package vip.floatationdevice.mgbridge.gce;

import org.bukkit.entity.Player;
import vip.floatationdevice.guilded4j.object.ChatMessage;
import vip.floatationdevice.mgbridge.GuildedCommandExecutor;

import java.util.ArrayList;

import static vip.floatationdevice.mgbridge.BindManager.bindMap;
import static vip.floatationdevice.mgbridge.MGBridge.instance;

public class Command_list implements GuildedCommandExecutor
{
    @Override
    public String getCommandName()
    {
        return "list";
    }

    @Override
    public boolean execute(ChatMessage msg, String[] args)
    {
        ArrayList<String> list = new ArrayList<>();
        for (Player p : instance.getServer().getOnlinePlayers())
            list.add(p.getName());
        if(bindMap.containsKey(msg.getCreatorId()))
        {
            instance.sendGuildedMsg(list.size() + ": " + list, msg.getId());
            return true;
        }
        else return false;
    }
}
