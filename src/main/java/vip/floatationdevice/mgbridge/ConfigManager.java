package vip.floatationdevice.mgbridge;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;

import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.*;

public class ConfigManager
{
    static YamlConfiguration cfg;

    static String lang;
    static String token;
    static String server;
    static String channel;

    static boolean forwardJoinLeaveEvents = true;
    static boolean debug = false;
    static Proxy proxy = Proxy.NO_PROXY;
    static String toGuildedMessageFormat = "<{PLAYER}> {MESSAGE}"; // messages sent to guilded
    static String toMinecraftMessageFormat = "§e<§r{PLAYER}§e> §r{MESSAGE}"; // messages sent to minecraft
    static boolean loadConfig()
    {
        File cfgFile = new File(instance.getDataFolder(), "config.yml");
        if (!cfgFile.exists())
        { // create default config file if it doesn't exist
            log.severe("Config file not found and an empty one will be created. Set the token and channel UUID and RESTART server.");
            instance.saveDefaultConfig();
            return false;
        }
        else
        {
            // init configuration system
            cfg = YamlConfiguration.loadConfiguration(cfgFile);
            lang = cfg.getString("language");
            token = cfg.getString("token");
            server = cfg.getString("server");
            channel = cfg.getString("channel");
            forwardJoinLeaveEvents = cfg.getBoolean("forwardJoinLeaveEvents", true);
            debug = cfg.getBoolean("debug", false);
            I18nUtil.setLanguage(lang);
            log.info("Language: " + translate("language") + " (" + lang + ") by " + translate("language-file-contributor"));
            if(notSet(lang, token, server, channel) || !lang.matches("^[a-z]{2}_[A-Z]{2}$") || channel.length() != 36 || server.length() != 8)
            {
                log.severe(translate("invalid-config"));
                return false;
            }
            BindManager.loadBindMap();
            // set socks proxy
            if(!notSet(cfg.getString("socksProxy"))) // is socksProxy field set?
            {
                String[] socksProxy = cfg.getString("socksProxy").split(":");
                if("default".equalsIgnoreCase(cfg.getString("socksProxy"))) // use proxy settings in JVM arguments
                    proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(System.getProperty("socksProxyHost"), Integer.parseInt(System.getProperty("socksProxyPort"))));
                else if(socksProxy.length == 2 && socksProxy[0].length() > 0 && socksProxy[1].length() > 0 && socksProxy[1].matches("^\\d+$")) // socksProxy is set and valid
                    proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksProxy[0], Integer.parseInt(socksProxy[1])));
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
