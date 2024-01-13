# TMDiscordBot
TMDiscordBot is an addon which pushes ticket creations and modifications from TicketManager to Discord. Users can additionally choose which ticket actions appear in Discord via the configuration file.

# Requirements
### Dependencies
- Server Version:
	- TM:Core
	- TM:SE
- Proxy Version:
	- None

### Other Requirements
This guide assumes you know how to:
- create a bot application via the Discord Developer Portal. It must have permission to send text channel messages.
- acquire the bot token and channel ID.

# Usage

### Operation Mode
TMDiscordBot has two operational modes: `CLIENT` and `RELAY`:
- `CLIENT`: Discord bot will launch like normal. Use this mode if you want one bot per server. This mode is available for both servers and proxies.
- `RELAY`: Does not launch the Discord bot. Instead, discord messages will be forwarded to the proxy. Use this mode if you have a single-proxy network and only want one bot. This mode is only available to servers. Some important notes on this mode below:
	- You must also install TMDiscordBot on the proxy in client mode.
	- Tickets will only be forwarded to the proxy if at least one user is on the server. This is a limitation with Paper/Spigot and not the plugin.



### Config Information
TMDiscordBot has three configuration files:
- `config-common.yml`: contains shared data between the two modes. Additionally, this is where you set the operation mode. By default, it is set to client mode.
- `config-client.yml`: contains data specific to client mode, such as which ticket actions to push to Discord, the bot token, and the channel ID.
- `config-relay.yml`: contains data specific to relay mode, such as which ticket actions to push to the relay.

It is worth noting that the operational mode determines which config files load. For example, relay mode will only load the common and relay configs, and client mode will only load the common and client configs.

# Installation
1. Drag the appropriate version into the plugins folder.
2. Start the server. An error will show up in console—this is normal.
3. Configure `config-common.yml` and the associated config for the preferred operational mode. Based on your platform, these files are in different places:
	- Servers: `~plugins/TicketManager/addons/DiscordBot/`
	- Waterfall: `~plugins/TMDiscordBot/`
	- Velocity: `~plugins/tmdiscordbot/`
5. Perform the command `/tmdiscord reload`. See Commands & Permissions below if you do not have permission.

# Commands & Permissions
TMDiscordBot has one permission `tmdiscord.reload` that grants access to the command `/tmdiscord reload` to reload the plugin.
