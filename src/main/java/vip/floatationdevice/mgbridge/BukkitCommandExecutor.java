package vip.floatationdevice.mgbridge;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static vip.floatationdevice.mgbridge.BindManager.*;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.log;

public class BukkitCommandExecutor implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(!(sender instanceof Player))
        {
            sender.sendMessage(translate("non-player-executor"));
            return false;
        }

        if(args.length == 1 && args[0].equals("mkbind"))
        {
            String code = "";// 10-digit random bind code from ASCII x21(!) to x7E(~)
            for(int i = 0; i != 10; i++) code += String.valueOf((char) (r.nextInt(94) + 33));// StringBuilder not needed
            if(pendingPlayerMap.containsKey(((Player) sender).getUniqueId())) // remove old bind code if exists
                pendingMap.remove(pendingPlayerMap.get(((Player) sender).getUniqueId()));
            pendingMap.put(code, ((Player) sender).getUniqueId());
            pendingPlayerMap.put(((Player) sender).getUniqueId(), code);
            sender.sendMessage(translate("m-code-requested").replace("%CODE%", code));
            log.info(translate("c-code-requested").replace("%PLAYER%", sender.getName()).replace("%CODE%", code));
            return true;
        }
        else if(args.length == 1 && args[0].equals("rmbind"))
        {
            for(String u : bindMap.keySet())
            {
                if(bindMap.get(u).equals(((Player) sender).getUniqueId()))
                {
                    bindMap.remove(u);
                    sender.sendMessage(translate("m-unbind-success"));
                    log.info(translate("c-unbind-success").replace("%PLAYER%", sender.getName()));
                    saveBindMap();
                    return true;
                }
            }
            sender.sendMessage(translate("m-no-bind"));
            return false;
        }
        else
        {
            sender.sendMessage(translate("m-usage"));
            return false;
        }
    }
}
