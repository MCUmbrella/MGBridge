package vip.floatationdevice.mgbridge;

import vip.floatationdevice.guilded4j.object.ChatMessage;

public interface GuildedCommandExecutor
{
    /**
     * Gets the name of the subcommand (the first argument when the Guilded command "/mgb" is called).
     * For example, if the command you want to implement is "/mgb test", this function should return "test".
     * @return The name of the subcommand.
     */
    String getCommandName();

    /**
     * Gets the description of the subcommand.
     * @return The description of the subcommand.
     */
    String getDescription();

    /**
     * Gets the usage of the subcommand.
     * @return The usage of the subcommand.
     */
    String getUsage();
    boolean execute(ChatMessage msg, String[] args);
}
