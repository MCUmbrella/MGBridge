package vip.floatationdevice.mgbridge;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

import static vip.floatationdevice.mgbridge.MGBridge.instance;
import static vip.floatationdevice.mgbridge.MGBridge.notSet;

public class ConfigManager
{
    static YamlConfiguration cfg;
    static String socksProxyHost = null; // socks proxy settings
    static String socksProxyPort = null;
    static String toGuildedMessageFormat = "<{PLAYER}> {MESSAGE}"; // messages sent to guilded
    static String toMinecraftMessageFormat = "§e<§r{PLAYER}§e> §r{MESSAGE}"; // messages sent to minecraft
    static boolean loadConfig()
    {
        File cfgFile = new File(instance.getDataFolder(), "config.yml");
        if (!cfgFile.exists())
        {
            instance.getLogger().severe("Config file not found and an empty one will be created. Set the token and channel UUID and RESTART server.");
            instance.saveDefaultConfig();
            return false;
        }
        else
        {
            // init configuration system
            cfg = YamlConfiguration.loadConfiguration(cfgFile);
            // set socks proxy
            if(!notSet(cfg.getString("socksProxy"))) // is socksProxy field set?
            {
                String[] socksProxy = cfg.getString("socksProxy").split(":");
                if("default".equalsIgnoreCase(cfg.getString("socksProxy")))
                { // use proxy settings in JVM arguments
                    socksProxyHost = System.getProperty("socksProxyHost");
                    socksProxyPort = System.getProperty("socksProxyPort");
                }
                else if(socksProxy.length == 2 && socksProxy[0].length() > 0 && socksProxy[1].length() > 0 && socksProxy[1].matches("^\\d+$"))
                { // socks proxy is set and valid
                    socksProxyHost = socksProxy[0];
                    socksProxyPort = socksProxy[1];
                }
            }
            // set message formatter
            if("disabled".equalsIgnoreCase(cfg.getString("toGuildedMessageFormat")))
                toGuildedMessageFormat = null;
            else
                toGuildedMessageFormat = cfg.getString("toGuildedMessageFormat", toGuildedMessageFormat);
            if("disabled".equalsIgnoreCase(cfg.getString("toMinecraftMessageFormat")))
                toMinecraftMessageFormat = null;
            else
                toMinecraftMessageFormat = cfg.getString("toMinecraftMessageFormat", toMinecraftMessageFormat);
            return true;
        }
    }
}
