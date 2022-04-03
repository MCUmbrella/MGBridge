package vip.floatationdevice.mgbridge;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

import static vip.floatationdevice.mgbridge.MGBridge.instance;

public class ConfigManager
{
    static YamlConfiguration cfg;
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
            cfg = YamlConfiguration.loadConfiguration(cfgFile);
            return true;
        }
    }
}
