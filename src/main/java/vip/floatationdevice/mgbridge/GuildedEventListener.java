package vip.floatationdevice.mgbridge;

import com.google.common.eventbus.Subscribe;
import org.bukkit.Bukkit;
import vip.floatationdevice.guilded4j.G4JWebSocketClient;
import vip.floatationdevice.guilded4j.event.ChatMessageCreatedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketClosedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketWelcomeEvent;
import vip.floatationdevice.guilded4j.object.ChatMessage;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;

import static vip.floatationdevice.mgbridge.BindManager.*;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.*;
import static vip.floatationdevice.mgbridge.ConfigManager.*;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class GuildedEventListener
{
    G4JWebSocketClient ws; // used to connect to guilded and receive guilded messages
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
            {
                executors.remove(executor);
                return this;
            }
        }
        throw new IllegalArgumentException("No executor found for command " + commandName);
    }

    public void unregisterAllExecutors()
    {
        executors.clear();
    }

    public GuildedEventListener()
    {
        connect();
    }

    void connect()
    {
        log.info(translate("connecting"));
        ws = new G4JWebSocketClient(token);
        if(socksProxyHost != null && socksProxyPort != null)
            ws.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksProxyHost, Integer.parseInt(socksProxyPort))));
        ws.eventBus.register(this);
        ws.connect();
    }

    void disconnect()
    {
        // unregister gEventListener from event bus first to prevent automatic reconnection
        ws.eventBus.unregister(this);
        ws.close();
        log.info(translate("disconnected"));
    }

    @Subscribe
    public void onG4JConnectionOpened(GuildedWebSocketWelcomeEvent event){log.info(translate("connected"));}

    @Subscribe
    public void onG4JConnectionClosed(GuildedWebSocketClosedEvent event)
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

    @Subscribe
    public void onGuildedMessage(ChatMessageCreatedEvent event)
    {
        ChatMessage msg = event.getChatMessage();// the received ChatMessage object
        if(msg.getServerId().equals(server) && msg.getChannelId().equals(channel))// in the right server and channel?
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
            else // not a mgb command. consider it as normal message
            { // check if G->M forwarding is enabled, the message is not a command, and the message is from a user who is bound to Minecraft
                if(toMinecraftMessageFormat != null && !msg.getContent().startsWith("/") && bindMap.containsKey(msg.getCreatorId()))
                    Bukkit.broadcastMessage(toMinecraftMessageFormat.replace("{PLAYER}", getPlayerName(bindMap.get(msg.getCreatorId()))).replace("{MESSAGE}", msg.getContent()));
            }
        }
    }
}
