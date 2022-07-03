package vip.floatationdevice.mgbridge.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event that is fired when a Guilded user is bound to a Minecraft player.
 */
public class UserBoundEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private final String userId;
    private final UUID uuid;

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    /**
     * Gets the user ID of the user bound to the Minecraft player.
     * @return The Guilded user ID.
     */
    public String getUserId()
    {
        return userId;
    }

    /**
     * Gets the UUID of the Minecraft player bound to the user.
     * @return The UUID.
     */
    public UUID getPlayerUUID()
    {
        return uuid;
    }

    public UserBoundEvent(String userId, UUID uuid)
    {
        this.userId = userId;
        this.uuid = uuid;
    }
}
