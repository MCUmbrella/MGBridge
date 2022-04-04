package vip.floatationdevice.mgbridge;

public interface GuildedCommandExecutor
{
    String getCommand();
    boolean execute(String[] args);
}
