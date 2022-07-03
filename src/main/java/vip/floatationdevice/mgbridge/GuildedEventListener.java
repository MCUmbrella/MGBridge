package vip.floatationdevice.mgbridge;

import com.google.common.eventbus.Subscribe;
import org.bukkit.Bukkit;
import vip.floatationdevice.guilded4j.G4JWebSocketClient;
import vip.floatationdevice.guilded4j.event.ChatMessageCreatedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketClosedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketWelcomeEvent;
import vip.floatationdevice.guilded4j.object.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static vip.floatationdevice.mgbridge.BindManager.*;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.*;
import static vip.floatationdevice.mgbridge.ConfigManager.*;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class GuildedEventListener
{
    G4JWebSocketClient ws; // used to connect to guilded and receive guilded messages
    private final HashMap<String, GuildedCommandExecutor> executors = new HashMap<>(); // subcommands of the guilded command "/mgb"

    public HashMap<String, GuildedCommandExecutor> getExecutors()
    {
        return executors;
    }

    public GuildedEventListener registerExecutor(GuildedCommandExecutor executor)
    {
        executors.put(executor.getCommandName(), executor);
        return this;
    }

    public GuildedEventListener unregisterExecutor(String commandName)
    {
        if(executors.remove(commandName) == null) throw new IllegalArgumentException("No executor found for command " + commandName);
        return this;
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
        ws.setProxy(proxy);
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
        ws.setProxy(proxy);
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
                if(executors.get(args[1]) != null)
                    executors.get(args[1]).execute(msg, subCommandArgs);
            }
            else // not a mgb command. consider it as normal message
            { // check if G->M forwarding is enabled, the message is not a command, and the message is from a user who is bound to Minecraft
                if(toMinecraftMessageFormat != null && !msg.getContent().startsWith("/") && bindMap.containsKey(msg.getCreatorId()))
                    Bukkit.broadcastMessage(toMinecraftMessageFormat.replace("{PLAYER}", getPlayerName(bindMap.get(msg.getCreatorId()))).replace("{MESSAGE}", msg.getContent()));
            }
        }
    }
}
