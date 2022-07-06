package vip.floatationdevice.mgbridge.gce;

import vip.floatationdevice.guilded4j.object.ChatMessage;
import vip.floatationdevice.mgbridge.GuildedCommandExecutor;

import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.instance;

public class Command_helpof implements GuildedCommandExecutor
{
    @Override
    public String getCommandName()
    {
        return "helpof";
    }

    @Override
    public String getCommandDescription()
    {
        return translate("g-cmd-helpof-desc");
    }

    @Override
    public String getCommandUsage()
    {
        return "/mgb helpof <SUBCOMMAND>";
    }

    @Override
    public boolean execute(ChatMessage msg, String[] args)
    {
        if(args.length == 1 && instance.getGEventListener().getExecutors().containsKey(args[0]))
        {
            GuildedCommandExecutor gce = instance.getGEventListener().getExecutors().get(args[0]);
            String desc = null, usage = null;
            try
            {// GuildedCommandExecutor from MGB version 0.9.3 and older doesn't implement getCommandDescription() and getCommandUsage()
                desc = gce.getCommandDescription();
                usage = gce.getCommandUsage();
            }
            catch(AbstractMethodError e)
            {
                instance.getLogger().warning("Guilded subcommand '" + args[0] + "' is made for old version of MGBridge. It's recommended to update it");
            }
            instance.sendGuildedMessage(
                    translate("g-cmd-helpof-msg")
                            .replace("%SUBCOMMAND%", gce.getCommandName())
                            .replace("%DESCRIPTION%", desc != null ? desc : "???")
                            .replace("%USAGE%", usage != null ? usage : "???"),
                    msg.getId(),
                    null,
                    null
            );
            return true;
        }
        else return false;
    }
}
