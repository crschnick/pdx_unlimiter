## Features

### Savegame management for EU4

Since keeping track of all your savegames is getting more complicated with every new savegame you start or backup you revert to,
the Pdx-Unlimiter provides you with an easy to use interface for choosing the right save file.
This is done by collecting all savegames and creating a browsable and editable savegame history so you can load any prior saved version of
a campaign. 
This works for both Ironman and Non-Ironman savegames.
All savegames can be also launched directly from within the Pdx-Unlimiter without needing to vist the EU4 main menu.

![Example](docs/screenshot.png)

### Reverting savegames

If you are ingame and the Pdx-Unlimiter is running, you can press (CTRL+SHIFT+I) at any time to import the latest saved savegame.
If you want to revert to a previously imported savegame, you can press (CTRL+SHIFT+K) to quickly close EU4.
This kind of manual savegame management for ironman games allows you to easily revert to previous saves,
i.e. to savescum, which is kinda necessary for hard achievements.

### Custom achievements and challenges

Every imported save game is parsed and converted into a more readable and usable json format internally.
This allows for operations on these savegames such as custom achievement creation and validation.
If you are interested in creating and sharing your own achievements,
visit [https://github.com/crschnick/pdxu_achievements]() for a documentation.

![Example](docs/achievements.png)

## Planned features

### Savegame management for other Paradox games

Right now, only EU4 savegames are supported. If the EU4 component works completely, support for other games can easily be added.

Status: Planned

### Loading time improvements

When using the EU4 Unlimiter for savegame management, you can also enable a feature which automatically removes all unnecessary
data from a savegame file.
This includes country histories, province histories, war histories and economic statistics of the ledger and should only be done
if you are not interested in these kinds of data.
This speeds up the loading time for an eu4 savegame.

Status: Planned

### Gameplay assistance

Every imported savegame date is parsed and converted into a more readable and usable json format and therefore
would allow the Pdx-Unlimiter to take over some of the tedious micromanagement,
e.g. building buildings, converting or razing all provinces, and carpet sieging provinces.

Status: Planned


## Contributing

You can help the project by reporting issues, fixing bugs and making the planned issues a reality.
Contributing guidelines coming soon. 
This project is only in its infancy and only a handful of features are currently included,
however the solid foundation needed for more planned features already exists.
The current focus primarily lies on perfecting already existing features.

## Building and running

You can build the project with `gradle build`.
To create jlink images and installers, use `gradle createDist`.
For running, you can use `gradle run`.
To correctly run the Pdx-Unlimiter in a development environment, you need to set the property `installDir=<installation directory>`
in the `pdxu.properties` file. This is needed for a dev build to simulate the program operating
in a real installation directory and not in the build directory.
