# MGBridge
Formerly known as the "MC2GForward" project, this project is a bridge between Minecraft server and Guilded server.
Its main function is forwarding chat messages, and may have other small useful functions in the future.
## Note:
- When the plugin is first installed on the server it will create an empty configuration file.
You need to fill in your bot token, server ID and channel UUID to start using the plugin.
- You are suggested to restart the server after setting up, not to reload the plugin.
## Configuration: `config.properties`
```properties
language=en_US
token=
server=
channel=
forwardJoinLeaveEvents=true
debug=false
```
- `language`: the language to use for the plugin. Other languages can be found in [/src/main/resources](https://github.com/MCUmbrella/MGBridge/tree/main/src/main/resources).
- `token`: your bot token.
- `server`: target server ID. The plugin will detect messages sent from this server.
- `channel`: target channel ID. All message forwarding occurs in this channel.
- `forwardJoinLeaveEvents`: whether forward player join/quit messages or not.
- `debug`: print the response after forwarding a message to Guilded.
## Binding your Guilded account:
1. Log into the Minecraft server and type `/mgb mkbind` and you will get a 10-digit random binding code.
2. Open Guilded client and type `/mgb mkbind <code>`.
- If you want to unbind, type `/mgb rmbind` at any side.
