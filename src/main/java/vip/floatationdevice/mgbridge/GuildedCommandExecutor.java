package vip.floatationdevice.mgbridge;

import vip.floatationdevice.guilded4j.object.ChatMessage;

public interface GuildedCommandExecutor
{
    String getCommandName();
    boolean execute(ChatMessage msg, String[] args);
}
