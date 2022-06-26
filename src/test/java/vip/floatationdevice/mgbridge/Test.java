package vip.floatationdevice.mgbridge;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;

import static vip.floatationdevice.mgbridge.ConfigManager.toGuildedMessageFormat;
import static vip.floatationdevice.mgbridge.ConfigManager.toMinecraftMessageFormat;
import static vip.floatationdevice.mgbridge.MGBridge.notSet;

public class Test
{
    public static void main(String[] args)
    {
        Proxy proxy = Proxy.NO_PROXY;
        File cfgFile = new File("src/main/resources/config.yml");
        System.out.println(cfgFile.exists());
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(cfgFile);
        System.out.println(cfg.getString("socksProxy"));
        if(!notSet(cfg.getString("socksProxy"))) // is socksProxy field set?
        {
            String[] socksProxy = cfg.getString("socksProxy").split(":");
            if("default".equalsIgnoreCase(cfg.getString("socksProxy"))) // use proxy settings in JVM arguments
                try
                {
                    proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(System.getProperty("socksProxyHost"), Integer.parseInt(System.getProperty("socksProxyPort"))));
                }
                catch(Exception ignored) {}
            else if(socksProxy.length == 2) // socksProxy is set in config file
                try
                {
                    proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksProxy[0], Integer.parseInt(socksProxy[1])));
                }
                catch(Exception ignored) {}
        }
        System.out.println("proxy: " + proxy);
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
