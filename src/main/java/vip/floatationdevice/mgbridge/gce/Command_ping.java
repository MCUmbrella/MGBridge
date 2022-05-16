package vip.floatationdevice.mgbridge.gce;

import vip.floatationdevice.guilded4j.object.ChatMessage;
import vip.floatationdevice.mgbridge.GuildedCommandExecutor;

import static vip.floatationdevice.mgbridge.BindManager.bindMap;
import static vip.floatationdevice.mgbridge.MGBridge.instance;

public class Command_ping implements GuildedCommandExecutor
{
    @Override
    public String getCommandName(){return "ping";}

    @Override
    public boolean execute(ChatMessage msg, String[] args)
    {
        if(bindMap.containsKey(msg.getCreatorId()))
        {
            instance.sendGuildedMessage("pong", msg.getId(), null, null);
            return true;
        }
        else return false;
    }
}
