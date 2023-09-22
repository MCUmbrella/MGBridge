package vip.floatationdevice.mgbridge;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vip.floatationdevice.guilded4j.G4JClient;
import vip.floatationdevice.mgbridge.event.UserUnboundEvent;

import java.util.Random;
import java.util.UUID;

import static vip.floatationdevice.mgbridge.BindManager.*;
import static vip.floatationdevice.mgbridge.ConfigManager.proxy;
import static vip.floatationdevice.mgbridge.ConfigManager.token;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.instance;
import static vip.floatationdevice.mgbridge.MGBridge.log;

public class BukkitCommandExecutor implements CommandExecutor
{
    static final Random r = new Random(); // used to generate random bind code
    static final int BIND_CODE_LENGTH = 8;
    static final char[] BIND_CODE_CHARS = "1234567890qwertyuiopasdfghjklzxcvbnm".toCharArray();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(args.length == 1 && args[0].equals("reload") && sender.hasPermission("mgbridge.reload"))
        {
            try
            {
                try {instance.gEventListener.disconnect();} catch(IllegalArgumentException ignored) {}
                if(ConfigManager.loadConfig())
                {
                    instance.g4JClient = new G4JClient(token);
                    instance.g4JClient.setProxy(proxy);
                    instance.gEventListener.connect();
                    log.info("MGBridge reloaded");
                    if(sender instanceof Player) sender.sendMessage("[§eMGBridge§f] §aMGBridge reloaded");
                }
                else
                {
                    log.severe("MGBridge reloaded with errors");
                    if(sender instanceof Player) sender.sendMessage("[§eMGBridge§f] §cMGBridge reloaded with errors");
                }
                return true;
            }
            catch(Throwable e)
            {
                log.severe("Failed to reload MGBridge");
                e.printStackTrace();
                if(sender instanceof Player) sender.sendMessage("[§eMGBridge§f] §cFailed to reload MGBridge");
                return false;
            }
        }

        if(!(sender instanceof Player))
        {
            sender.sendMessage(translate("non-player-executor"));
            return false;
        }

        if(args.length == 1 && args[0].equals("mkbind"))
        {
            if(!sender.hasPermission("mgbridge.mkbind")) return false;
            // generate a random bind code
            StringBuilder sb = new StringBuilder(BIND_CODE_LENGTH);
            for(int i = 0; i != BIND_CODE_LENGTH; i++)
                sb.append(BIND_CODE_CHARS[r.nextInt(BIND_CODE_CHARS.length)]);
            String code = sb.toString();
            // remove old bind code if exists
            if(pendingPlayerMap.containsKey(((Player) sender).getUniqueId()))
                pendingMap.remove(pendingPlayerMap.get(((Player) sender).getUniqueId()));
            // add the player and code to the pending map
            pendingMap.put(code, ((Player) sender).getUniqueId());
            pendingPlayerMap.put(((Player) sender).getUniqueId(), code);
            sender.sendMessage(translate("m-code-requested").replace("%CODE%", code));
            log.info(translate("c-code-requested").replace("%PLAYER%", sender.getName()).replace("%CODE%", code));
            return true;
        }
        else if(args.length == 1 && args[0].equals("rmbind"))
        {
            if(!sender.hasPermission("mgbridge.rmbind")) return false;
            for(String userId : bindMap.keySet())
            {
                if(bindMap.get(userId).equals(((Player) sender).getUniqueId()))
                {
                    UUID removed = bindMap.remove(userId);
                    sender.sendMessage(translate("m-unbind-success"));
                    log.info(translate("c-unbind-success").replace("%PLAYER%", sender.getName()));
                    saveBindMap();
                    instance.getServer().getPluginManager().callEvent(new UserUnboundEvent(userId, removed));
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
