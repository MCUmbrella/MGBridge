package vip.floatationdevice.mgbridge;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

import static vip.floatationdevice.mgbridge.MGBridge.instance;
import static vip.floatationdevice.mgbridge.MGBridge.log;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;

public class BindManager
{
    public static HashMap<String, UUID> bindMap = new HashMap<String, UUID>();// key: guilded userId; value: mc player uuid
    public static HashMap<String, UUID> pendingMap = new HashMap<String, UUID>();// key: bind code; value: mc player uuid
    public static HashMap<UUID, String> pendingPlayerMap = new HashMap<UUID, String>();// pendingMap but with upside down

    public BindManager()
    {
        loadBindMap();
    }

    public static class BindMapContainer implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public HashMap<String, UUID> saveBindMap;

        public BindMapContainer(HashMap<String, UUID> bindMap){saveBindMap = bindMap;}
    }

    public static void saveBindMap()
    {
        try
        {
            ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(new File(instance.getDataFolder(), "bindMap.dat")));
            o.writeObject(new BindMapContainer(bindMap));
            o.close();
            log.info(translate("bindmap-save-success"));
        }
        catch(Exception e)
        {
            log.severe(translate("bindmap-save-failure").replace("%EXCEPTION%", e.toString()));
        }
    }

    public static void loadBindMap()
    {
        try
        {
            ObjectInputStream o = new ObjectInputStream(new FileInputStream(new File(instance.getDataFolder(), "bindMap.dat")));
            BindMapContainer temp = (BindMapContainer) o.readObject();
            o.close();
            bindMap = temp.saveBindMap;
            log.info(translate("bindmap-load-success").replace("%COUNT%", String.valueOf(bindMap.size())));
        }
        catch(FileNotFoundException ignored) {}
        catch(Exception e)
        {
            log.severe(translate("bindmap-load-failure").replace("%EXCEPTION%", e.toString()));
        }
    }
}
