package vip.floatationdevice.mgbridge;

import com.google.common.eventbus.Subscribe;
import org.bukkit.Bukkit;
import vip.floatationdevice.guilded4j.event.ChatMessageCreatedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketClosedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketWelcomeEvent;
import vip.floatationdevice.guilded4j.object.ChatMessage;

import java.util.HashMap;

import static vip.floatationdevice.mgbridge.BindManager.bindMap;
import static vip.floatationdevice.mgbridge.ConfigManager.*;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.getPlayerName;
import static vip.floatationdevice.mgbridge.MGBridge.log;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class GuildedEventListener
{
    private final HashMap<String, GuildedCommandExecutor> executors = new HashMap<>(); // subcommands of the guilded command "/mgb"

    /**
     * Gets the map of subcommands of the Guilded command "/mgb".
     * @return A HashMap object. The key is the name of the subcommand, the value is the GuildedCommandExecutor object of the subcommand.
     */
    public HashMap<String, GuildedCommandExecutor> getExecutors()
    {
        return executors;
    }

    /**
     * Register a subcommand.
     * @param executor The GuildedCommandExecutor object of the subcommand.
     */
    public GuildedEventListener registerExecutor(GuildedCommandExecutor executor)
    {
        executors.put(executor.getCommandName(), executor);
        return this;
    }

    /**
     * Unregister a subcommand.
     * @param commandName The name of the subcommand.
     * @throws IllegalArgumentException If the subcommand does not exist.
     */
    public GuildedEventListener unregisterExecutor(String commandName)
    {
        if(executors.remove(commandName) == null)
            throw new IllegalArgumentException("No executor found for command " + commandName);
        return this;
    }

    void unregisterAllExecutors()
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
        MGBridge.instance.getG4JClient().registerEventListener(this).connectWebSocket();
    }

    void disconnect()
    {
        MGBridge.instance.getG4JClient().unregisterEventListener(this).disconnectWebSocket();
        log.info(translate("disconnected"));
    }

    @Subscribe
    public void onG4JConnectionOpened(GuildedWebSocketWelcomeEvent event){log.info(translate("connected"));}

    @Subscribe
    public void onG4JConnectionClosed(GuildedWebSocketClosedEvent event)
    {
        // if the plugin is running normally but the connection was closed
        // then we can consider it as unexpected and try to reconnect
        log.warning(translate("disconnected-unexpected").replace("%DELAY%", String.valueOf(reconnectDelay)));
        Bukkit.getScheduler().runTaskLater(MGBridge.instance, this::connect, reconnectDelay * 20L);
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
