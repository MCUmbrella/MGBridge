package vip.floatationdevice.mgbridge;

import com.google.common.eventbus.Subscribe;
import org.bukkit.Bukkit;
import vip.floatationdevice.guilded4j.G4JWebSocketClient;
import vip.floatationdevice.guilded4j.event.ChatMessageCreatedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketClosedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketInitializedEvent;
import vip.floatationdevice.guilded4j.object.ChatMessage;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;

import static vip.floatationdevice.mgbridge.BindManager.*;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.*;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class GuildedEventListener
{
    G4JWebSocketClient ws; // used to connect to guilded and receive guilded messages
    private final String socksProxyHost = System.getProperty("socksProxyHost"); // socks proxy settings in the arguments
    private final String socksProxyPort = System.getProperty("socksProxyPort");
    private final ArrayList<GuildedCommandExecutor> executors = new ArrayList<>(); // list of guilded commands to execute

    public GuildedEventListener registerExecutor(GuildedCommandExecutor executor)
    {
        executors.add(executor);
        return this;
    }

    public GuildedEventListener unregisterExecutor(String commandName)
    {
        for(GuildedCommandExecutor executor : executors)
        {
            if(executor.getCommandName().equals(commandName))
                executors.remove(executor);
            return this;
        }
        throw new NullPointerException("No executor found for command " + commandName);
    }

    public GuildedEventListener()
    {
        log.info(translate("connecting"));
        ws = new G4JWebSocketClient(MGBridge.token);
        // set socks proxy if it is set in the arguments
        if(socksProxyHost != null && socksProxyPort != null)
            ws.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksProxyHost, Integer.parseInt(socksProxyPort))));
        ws.eventBus.register(this);
        ws.connect();
    }

    @Subscribe
    public void onG4JConnectionOpened(GuildedWebSocketInitializedEvent event)
    {
        log.info(translate("connected"));
    }

    @Subscribe
    public void onG4JConnectionClosed(GuildedWebSocketClosedEvent event)
    {
        if(mgbRunning)
        {
            // if the plugin is running normally but the connection was closed
            // then we can consider it as unexpected and do a reconnection
            log.warning(translate("disconnected-unexpected"));
            ws = new G4JWebSocketClient(token);
            if(socksProxyHost != null && socksProxyPort != null)
                ws.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksProxyHost, Integer.parseInt(socksProxyPort))));
            ws.eventBus.register(this);
            ws.connect();
        }
        else
            // the plugin is being disabled or the server is stopping, so we can just ignore this
            log.info(translate("disconnected"));
    }

    @Subscribe
    public void onGuildedMessage(ChatMessageCreatedEvent event)
    {
        ChatMessage msg = event.getChatMessageObject();// the received ChatMessage object
        if(msg.getServerId().equals(MGBridge.server) && msg.getChannelId().equals(MGBridge.channel))// in the right server and channel?
        {
            if(msg.getContent().startsWith("/mgb "))
            {
                String[] args = msg.getContent().split(" "); // /mgb subcommand arg1 arg2 ...
                if(args.length == 1) return; // no subcommand. do nothing
                String[] subCommandArgs = new String[args.length - 2]; // arg1 arg2 ...
                System.arraycopy(args, 2, subCommandArgs, 0, subCommandArgs.length);
                for(GuildedCommandExecutor executor : executors)
                    if(executor.getCommandName().equals(args[1]))
                        executor.execute(msg, subCommandArgs);
            }
            else // not a mgb command
            {
                if(!msg.getContent().startsWith("/") && bindMap.containsKey(msg.getCreatorId())) // guilded user bound?
                    Bukkit.broadcastMessage("§e<§r" + getPlayerName(bindMap.get(msg.getCreatorId())) + "§e> §r" + msg.getContent());
            }
        }
    }
}
