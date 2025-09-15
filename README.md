# Project Xeric

## Table of Contents

- Project Home
  - [Changelog](#changelog)
  - [Contributing](#contributing)
- [Contribution Guidelines](./docs/CONTRIBUTING.md)
  - [Jagex Account Integration](./docs/CONTRIBUTING.md#jagex-account-integration)
  - [Getting Started](./docs/CONTRIBUTING.md#getting-started)

**Zeah Clan Rank & Task Tracking Plugin**

Project Xeric is a plugin designed for the "**Zeah Ironmen**" clan. The plugin features task tracking, a ranking system, leaderboards, and account exception tracking.

The plugin adds a sidebar panel that features two tabs:

1. A Player Profile section which includes a task list of each different tier of task and a player status card, and
2. A Leaderboard section that has a link to the WiseOldMan group for the clan.

## Changelog

### 1.2.0

#### Features

- Tasks are stored and retrieved from https://api.projectxeric.com.
- Sends a notice upon login when tasks are updated.

### 1.1.0

#### Features

- Tasks are sorted by type then by name.
- Sends a warning when Collection Log chat message notifications are off.

#### Hotfixes

- Known CA and KC task completion issues fixed.
- Known collection log task completion issues fixed.

#### Hotfixes

- Removes item pickup subscriptions in favour of collection log chat messages.
- Fixes invalid regex for Combat Achievements.
- Adds Delve KC & Fortis Glory checking.
- Fixes KC message to Hiscore conversion.
- Warns the player if "Collection log - New addition notification" setting is off.

### 1.0.1

#### Hotfixes

- Plugin name is no longer hidden on the side panel when installing from the Plugin Hub.

### 1.0.0

- Initial release of the plugin. Leaderboard is still being developed.

## Screenshots

![Shows the side panel created by the Project Xeric plugin.](/examples/example_01.jpg "Player Profile Side Panel")

![Shows chat messages received upon completing Xeric tasks.](/examples/example_02.jpg "Task Completion Chat Messages")

![Shows the Slayer Exception configuration option and other plugin settings.](/examples/example_03.jpg "Plugin Configuration Options")

## Contributing

Please see the [Contribution Guidelines](./docs/CONTRIBUTING.md) for an overview on how to contribute to this project.
