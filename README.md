![](https://cdn.modrinth.com/data/KyEGDSv3/images/df606c5a2d6dd813c41b2ef2cad8381861ccb9ac.png)

## Description

LogicTags is a Minecraft plugin that allows users to configure and manage tags for players. Users can set tags that players can choose from and also add permissions required for some commands.

## Links

- [Discord](https://codearray.dev/discord)
- [Wiki](https://codearray.dev/docs/logictags) (coming soon)
- [Modrinth (Download)](https://modrinth.com/plugins/logictags)
- [Developement Builds](https://jenkins.codearray.dev/job/LogicTags/)

## Showcase
![LogicTags Video Showcase](https://youtu.be/I0Kn1qKCPAc)

![](https://cdn.modrinth.com/data/KyEGDSv3/images/41f73cf8b39d34aeed5adb25a96393d76526ede9.png)
![](https://cdn.modrinth.com/data/KyEGDSv3/images/e123e9375b61905c3e86539a22a186b42985af1f.png)
![](https://cdn.modrinth.com/data/KyEGDSv3/images/e32311d25ec0966785683735a5004f36be5ad065.png)
![](https://cdn.modrinth.com/data/KyEGDSv3/images/e795b4a58f210631e32732d864dfa539a528b0c4.png)

## Commands

The plugin provides several commands for managing tags:

- `/tag` - View your current tag
- `/tag list` - List all available tags
- `/tag select <id>` - Select a tag by ID
- `/tag change <tag>` - Change your tag to the specified text (requires `logictags.change` permission)
- `/tag remove` - Remove your current tag
- `/tag reload` - Reload the tags configuration (requires `logictags.reload` permission)
- `/tag help` - Display help information for the plugin commands

## Configuration

### tags.yml

The `tags.yml` file is used to configure the available tags. Each tag has an ID, name, description, and an optional permission. Here is an example of the `tags.yml` file:

```yaml
tags:
  - id: funnyTag
    name: "&5Funny"
    description: A tag for funny content
  - id: coolTag
    name: Cool
    description: A tag for cool players
    permission: logictags.cool
  - id: proTag
    name: Pro
    description: A tag for professional players
    permission: logictags.pro
```

### config.yml

The `config.yml` file contains various settings for the plugin. Here are the available options:

- `customTeams`: (default: true) Whether to use custom teams for tag display. Custom teams should only be disabled when you use a plugin that displays ranks for the player or changes the player's display name.
- `seeOwnTag`: (default: false) Whether players can see their own tags.
