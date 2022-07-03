package vip.floatationdevice.mgbridge.gce;

import org.bukkit.entity.Player;
import vip.floatationdevice.guilded4j.object.ChatMessage;
import vip.floatationdevice.guilded4j.object.Embed;
import vip.floatationdevice.mgbridge.GuildedCommandExecutor;

import java.util.ArrayList;

import static vip.floatationdevice.mgbridge.BindManager.bindMap;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.instance;

public class Command_list implements GuildedCommandExecutor
{
    @Override
    public String getCommandName(){return "list";}

    @Override
    public String getDescription(){return translate("g-cmd-list-desc");}

    @Override
    public String getUsage(){return "/mgb list";}

    @Override
    public boolean execute(ChatMessage msg, String[] args)
    {
        if(bindMap.containsKey(msg.getCreatorId()))
        {
            ArrayList<String> list = new ArrayList<>();
            for(Player p : instance.getServer().getOnlinePlayers())
                list.add(p.getName());
            StringBuilder sb = new StringBuilder();
            for(String s : list)
                sb.append(s).append("\n");
            if(list.size() != 0)
                sb.deleteCharAt(sb.length() - 1);
            instance.sendGuildedEmbed(
                    new Embed()
                            .setTitle(String.valueOf(list.size()))
                            .setDescription(sb.toString())
                            .setColor(0xffff00),
                    msg.getId(),
                    null,
                    true
            );
            return true;
        }
        else return false;
    }
}
