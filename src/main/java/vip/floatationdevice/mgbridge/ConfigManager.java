package vip.floatationdevice.mgbridge;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;

import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.*;

public class ConfigManager
{
    final static int CONFIG_VERSION = 1;
    static YamlConfiguration cfg;

    static String lang;
    static String token;
    static String server;
    static String channel;

    static boolean forwardJoinLeaveEvents = true;
    static boolean debug = false;
    static Proxy proxy = Proxy.NO_PROXY;
    static String toGuildedMessageFormat = "**{PLAYER} ⟫** {MESSAGE}";
    static String toMinecraftMessageFormat = "[§eGuilded§r] <{PLAYER}> {MESSAGE}";
    static long reconnectDelay = 3;

    /**
     * Load the config file of MGBridge.
     * @return True if the config file is loaded and valid, false otherwise.
     */
    static boolean loadConfig()
    {
        File cfgFile = new File(instance.getDataFolder(), "config.yml");
        if(!cfgFile.exists())
        { // create default config file if it doesn't exist
            log.severe("Config file not found and an empty one will be created. Set the token and channel UUID and RESTART server.");
            instance.saveDefaultConfig();
            return false;
        }
        else
        {
            // init configuration system
            cfg = YamlConfiguration.loadConfiguration(cfgFile);
            int configYmlVersion = cfg.getInt("version", Integer.MIN_VALUE);
            if(CONFIG_VERSION != configYmlVersion)
            {
                log.severe("The 'version' key in config.yml should be " + CONFIG_VERSION + " but found " + configYmlVersion + ",");
                log.severe("which usually means that you need to update config.yml.");
                log.severe("Please check out the changelog and consider updating it.");
                log.severe("Starting MGBridge, but things may not work as expected");
            }
            lang = cfg.getString("language");
            token = cfg.getString("token");
            server = cfg.getString("server");
            channel = cfg.getString("channel");
            forwardJoinLeaveEvents = cfg.getBoolean("forwardJoinLeaveEvents", true);
            reconnectDelay = Math.max(cfg.getLong("reconnectDelay", 3L), 1L);
            debug = cfg.getBoolean("debug", false);
            I18nUtil.setLanguage(lang);
            log.info("Language: " + translate("language") + " (" + lang + ") by " + translate("language-file-contributor"));
            if(notSet(lang, token, server, channel) || !lang.matches("^[a-z]{2}_[A-Z]{2}$") || channel.length() != 36 || server.length() != 8)
            {
                log.severe(translate("invalid-config"));
                return false;
            }
            BindManager.loadBindMap();
            // set proxy
            if("direct".equalsIgnoreCase(cfg.getString("proxy.type", "direct")))
                ; // do nothing
            else if("default".equalsIgnoreCase(cfg.getString("proxy.type")))
            {
                boolean hasSocksProxy = System.getProperty("socksProxyHost") != null && System.getProperty("socksProxyPort") != null,
                        hasHttpProxy = System.getProperty("httpProxyHost") != null && System.getProperty("httpProxyPort") != null;
                if(hasSocksProxy)
                    proxy = new Proxy(
                            Proxy.Type.SOCKS,
                            new InetSocketAddress(
                                    System.getProperty("socksProxyHost"),
                                    Integer.parseInt(System.getProperty("socksProxyPort"))
                            )
                    );
                else if(hasHttpProxy)
                    proxy = new Proxy(
                            Proxy.Type.HTTP,
                            new InetSocketAddress(
                                    System.getProperty("httpProxyHost"),
                                    Integer.parseInt(System.getProperty("httpProxyPort"))
                            )
                    );
            }
            else
            {
                proxy = new Proxy(
                        Proxy.Type.valueOf(cfg.getString("proxy.type").toUpperCase()),
                        new InetSocketAddress(
                                cfg.getString("proxy.address").split(":")[0],
                                Integer.parseInt(cfg.getString("proxy.address").split(":")[1])
                        )
                );
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
