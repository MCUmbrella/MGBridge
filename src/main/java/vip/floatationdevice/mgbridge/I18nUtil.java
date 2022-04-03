package vip.floatationdevice.mgbridge;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class I18nUtil
{
    private static FileConfiguration l;
    private static String lang = "en_US";

    public static String setLanguage(String language)
    {
        if(language == null || language.equals("")) return lang;
        lang = language;
        File langFile = new File(MGBridge.instance.getDataFolder(), "lang_" + lang + ".yml");
        if(!langFile.exists()) MGBridge.instance.saveResource("lang_" + lang + ".yml", false);
        l = YamlConfiguration.loadConfiguration(langFile);
        return lang;
    }

    public static String translate(String key)
    {
        if(l == null) throw new IllegalStateException("Translation engine not initialized");
        return l.getString(key) == null ? "[NO TRANSLATION: " + key + "]" : l.getString(key);
    }

    public static String getLanguage()
    {
        return lang;
    }
}
