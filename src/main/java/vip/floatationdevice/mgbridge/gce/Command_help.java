package vip.floatationdevice.mgbridge.gce;

import vip.floatationdevice.guilded4j.object.ChatMessage;
import vip.floatationdevice.mgbridge.GuildedCommandExecutor;

import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.instance;

public class Command_help implements GuildedCommandExecutor
{
    @Override
    public String getCommandName()
    {
        return "help";
    }

    @Override
    public String getDescription()
    {
        return translate("g-cmd-help-desc");
    }

    @Override
    public String getUsage()
    {
        return "/mgb help";
    }

    @Override
    public boolean execute(ChatMessage msg, String[] args)
    {
        StringBuilder sb = new StringBuilder();
        for(String gceName : instance.getGEventListener().getExecutors().keySet())
        {
            sb.append('`').append(gceName).append("`, ");
        }
        sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1);
        instance.sendGuildedMessage(
                translate("g-cmd-help-msg").replace("%SUBCOMMANDS%", sb.toString()),
                msg.getId(),
                null,
                null
        );
        return true;
    }
}
