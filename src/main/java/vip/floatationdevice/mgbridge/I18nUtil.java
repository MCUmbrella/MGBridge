package vip.floatationdevice.mgbridge;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class I18nUtil
{
    private static FileConfiguration l;
    private static String lang = null;

    /**
     * Set the language of the plugin.
     * @param language Locale code of the language to use.
     * @return The language code.
     */
    public static String setLanguage(String language)
    {
        if(language == null || language.equals(""))
            throw new IllegalArgumentException("Language cannot be null or empty");
        lang = language;
        File langFile = new File(MGBridge.instance.getDataFolder(), "lang_" + lang + ".yml");
        if(!langFile.exists()) MGBridge.instance.saveResource("lang_" + lang + ".yml", false);
        l = YamlConfiguration.loadConfiguration(langFile);
        return lang;
    }

    /**
     * Translate a string.
     * @param key The key of the string.
     * @return The translated string. If the key does not exist, return "[NO TRANSLATION: key]"
     * @throws IllegalArgumentException If the language is not set.
     */
    public static String translate(String key)
    {
        if(l == null) throw new IllegalStateException("Translation engine not initialized");
        return l.getString(key) == null ? "[NO TRANSLATION: " + key + "]" : l.getString(key);
    }

    /**
     * Get the language of the plugin.
     * @return The language of the plugin.
     * @throws IllegalStateException If the language is not set.
     */
    public static String getLanguage()
    {
        if(lang == null) throw new IllegalStateException("Translation engine not initialized");
        return lang;
    }
}
