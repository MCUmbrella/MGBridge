package vip.floatationdevice.mgbridge;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

import static vip.floatationdevice.mgbridge.ConfigManager.toGuildedMessageFormat;
import static vip.floatationdevice.mgbridge.ConfigManager.toMinecraftMessageFormat;
import static vip.floatationdevice.mgbridge.MGBridge.notSet;

public class Test
{
    public static void main(String[] args)
    {
        String socksProxyHost = null;
        String socksProxyPort = null;
        File cfgFile = new File("src/main/resources/config.yml");
        System.out.println(cfgFile.exists());
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(cfgFile);
        System.out.println(cfg.getString("socksProxy"));
        if(!notSet(cfg.getString("socksProxy"))) // is socks proxy field set?
        {
            String[] socksProxy = cfg.getString("socksProxy").split(":");
            if(cfg.getString("socksProxy").equals("default"))
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
        System.out.println("socksProxyHost: " + socksProxyHost);
        System.out.println("socksProxyPort: " + socksProxyPort);
        toGuildedMessageFormat = cfg.getString("toGuildedMessageFormat", toGuildedMessageFormat);
        toMinecraftMessageFormat = cfg.getString("toMinecraftMessageFormat", toMinecraftMessageFormat);
        System.out.println("toGuildedMessageFormat: " + toGuildedMessageFormat);
        System.out.println("toMinecraftMessageFormat: " + toMinecraftMessageFormat);
        String mcPlayer = "MCUmbrella";
        String mcMessage = "Hello world!";
        System.out.println(toMinecraftMessageFormat.replace("{PLAYER}", mcPlayer).replace("{MESSAGE}", mcMessage));
        String gPlayer = "GuildedUmbrella";
        String gMessage = "Hello world from Guilded!";
        System.out.println(toGuildedMessageFormat.replace("{PLAYER}", gPlayer).replace("{MESSAGE}", gMessage));
    }
}
