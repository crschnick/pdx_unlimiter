# Pdx-Unlimiter

## Download

#### Windows

Download the [windows installer](https://github.com/crschnick/pdx_unlimiter/raw/master/installers/Pdx-Unlimiter-2.0.msi).

Note that there might be some issues with Windows SmartScreen blocking the installer.
If you are facing these issues, click on "More info" and then "Run anyway".

## Features

### Savegame management for Paradox games

Since keeping track of all your savegames is getting more complicated with every new savegame you start or backup you revert to,
the Pdx-Unlimiter provides you with an easy to use interface for choosing the right save file.
This is done by collecting all savegames and creating a browsable and editable savegame history so you can load any prior saved version of
a campaign. 
This works for both Ironman and Non-Ironman savegames.
All savegames can be also launched directly from within the Pdx-Unlimiter without needing to vist the main menu.

Supported games: EU4, HOI4, Stellaris, CK3

![Example](imgs/screenshot.png)

### Reverting savegames

If you are ingame and the Pdx-Unlimiter is running, you can import the latest saved savegame any time.
If you want to revert to a previously imported savegame, you can kill the currenty running Paradox game and load the latest save.
This kind of manual savegame management for ironman games allows you to easily revert to previous saves,
i.e. to savescum, which is kinda necessary for hard achievements.

### Custom achievements and challenges

Every imported save game is parsed and converted into a more readable and usable json format internally.
This allows for operations on these savegames such as custom achievement creation and validation.
If you are interested in creating and sharing your own achievements,
visit [the achievement repository](https://github.com/crschnick/pdxu_achievements) for a documentation.

![Example](imgs/achievements.png)

It is also possible to upload your savegames to [Rakaly](https://rakaly.com),
a website to analyze and share your eu4 achievements and compete against other players.

### Full Mod support

Savegames that use mods are also fully supported.
This includes displaying modded country information in the savegame manager and mod installation support.
When you launch a savegame from within the Pdx-Unlimiter, exactly the required mods and DLCs are automatically
enabled without having to use Paradox launcher configuration.
It is also checked whether a mod is missing or incompatible with the current version, so that nothing can potentially corrupt your savegames.


## Community and Support

If you have suggestions, need help, run into any issues or just want to talk to other friendly people, you can join the [Pdx-Unlimiter Discord](https://discord.gg/afErBW9Z).
You can also help the project by reporting issues, fixing bugs and making the planned issues a reality.
Contributing guidelines are coming soon. 

## Building and running

To build this project, [JDK 15](https://openjdk.java.net/projects/jdk/15/) is required.
You can build the project with `gradlew build` or `gradlew.bat build`.
To create jlink images and installers, use `gradlew createDist`.
For running, you can use `gradlew run`.
To correctly run the Pdx-Unlimiter in a development environment, you need to set the property `dataDir=<installation directory>`
in the `pdxu.properties` file. This is needed for a dev build to specify where to save data such as savegames.
