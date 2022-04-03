# MGBridge
Formerly known as the "MC2GForward" project, this project is a bridge between Minecraft server and Guilded server.
Its main function is forwarding chat messages, and may have other small useful functions in the future.
## Note:
- When the plugin is first installed on the server it will create an empty configuration file.
You need to fill in your bot token, server ID and channel UUID to start using the plugin.
- You are suggested to restart the server after setting up, not to reload the plugin.
## Configuration:
All the configurations are explained in the [default config file](https://github.com/MCUmbrella/MGBridge/blob/main/src/main/resources/config.yml).
## Download:
- [Development builds](https://github.com/MCUmbrella/MGBridge/actions/workflows/maven.yml)
- [Releases](https://github.com/MCUmbrella/MGBridge/releases)
## Binding your Guilded account:
1. Log into the Minecraft server and type `/mgb mkbind` and you will get a 10-digit random binding code.
2. Open Guilded client and type `/mgb mkbind <code>`.

If you want to unbind, type `/mgb rmbind` at any side.
