# minecraft-discord-verification
A Bungeecord and Spigot plugin that runs a discord bot and forces players to verify on your discord before playing on your server.

## Installation

To use this project, you need a bungeecord or spigot minecraft (proxy) server.

Insert the built or downloaded jar into the plugins folder. After the first run, a config file is generated in `./plugins/DiscordVerification`.

You also need a discord developer application with a bot. Invite this bot to your server and paste your token from the discord dev center into the config file.
You also need to set your discord server (guild) id there. Make sure the bot has the server members and guild message intents activated.

When the plugin is loaded, the discord bot will startup too and listen for `!verify` messages.

## Usage

Users on your server can use the `!verify <ign>` command to verify their minecraft account. There can only be on minecraft account per discord user.
When a player tries to connect to the proxy without verification, they get kicked and the kick-message in the config file shows up.
When a user leaves your discord server, they get unverified.

## Proxy Commands

| Syntax             | Permission Node              | Description                                                                                                                         |
|:-------------------|------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| isverified \<ign\> | discordverification.check    | Returns, whether the player is verified or not                                                                                      |
| clearverifications | discordverification.clear    | Clears all verifications.                                                                                                           |
| verifiedplayers    | discordverification.list     | Lists all verified IGNs                                                                                                             |
| unverify \<ign\>   | discordverification.unverify | Unverifies a player                                                                                                                 |
| verify \<ign\>     | discordverification.verify   | Verifies a player. This is until the player gets unverified or the verifications get cleared, because no discord id is getting set. |

## Database

The SQLite3 database is created in the proxy root directory. Its name is by default db.db and can be changed in the config. It has only one table: ```verified_users```. This table has the following columns.

| Field Name | Type                | Description                                            |
|:-----------|---------------------|--------------------------------------------------------|
| id         | INTEGER PRIMARY KEY | The key of the verification                            |
| ign        | TEXT                | The minecraft name of the verified player              |
| verified   | INTEGER             | A Java ``System.currentTimeMillis() / 1000`` timestamp |
| discord    | INTEGER             | The discord user id of the player                      |

## Config
The config values are explained in the `config.yml` file.
