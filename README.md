# MGBridge
Formerly known as the "MC2GForward" project, this project is a bridge between Minecraft server and Guilded server. Its main function is forwarding chat messages, and may have other small useful functions in the future.
## Note
- When the plugin is first installed on the server it will create an empty configuration file. You need to fill in your bot token, server ID and channel UUID to start using the plugin.
- You are suggested to restart the server after setting up, not to reload the plugin.
## Configuration
All the configurations are explained in the [default config file](https://github.com/MCUmbrella/MGBridge/blob/main/src/main/resources/config.yml).
## Download
- [Development builds](https://github.com/MCUmbrella/MGBridge/actions/workflows/maven.yml)
- [Releases](https://github.com/MCUmbrella/MGBridge/releases)
## Binding your Guilded account
In order to forward chat messages from Guilded server to Minecraft server and use more functions of MGB on the Guilded side, you need to bind your Guilded account to a Minecraft player.
### Steps
1. Log into the Minecraft server and type `/mgb mkbind` and you will get a 10-digit random binding code.
2. Go to the right channel of the Guilded server and type `/mgb mkbind <code>`.

If you want to unbind, type `/mgb rmbind` at any side.
## Permissions
- `mgbridge.mkbind`: Request binding code
- `mgbridge.rmbind`: Unbind your Guilded account
- `mgbridge.reload`: Reload the plugin by using "/mgb reload" (admin-only by default)
## Extensions
You can create your own MGB extension. Go to [the example extension's repository](https://github.com/MCUmbrella/MGBridgeExt) to see how a MGB extension is created.<br>
There are also some extensions made by me: _(all of them are [PlugMan](https://dev.bukkit.org/projects/plugman)-friendly, which means you can safely load/unload them without restarting the server)_
- [MGBDeathExt](https://github.com/MCUmbrella/MGBDeathExt): forward player death messages to Guilded server
- [MGBRoleAward](https://github.com/MCUmbrella/MGBRoleAward): award a role to the Guilded user when he binds to a Minecraft player
- [MGBChouka](https://github.com/MCUmbrella/MGBChouka): card draw plugin for entertainment (display language is Chinese)
