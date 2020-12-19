# Pdx-Unlimiter

## Download

Below you can find installers for all supported platforms.
It is recommended to read the [Pdx-Unlimiter Guide](https://github.com/crschnick/pdx_unlimiter/blob/master/docs/GUIDE.md)
after the installation.

#### Windows

Download the [windows installer](https://github.com/crschnick/pdx_unlimiter/raw/master/installers/Pdx-Unlimiter-2.0.msi).

Note that there might be some issues with Windows SmartScreen blocking the installer.
If you are facing these issues, click on "More info" and then "Run anyway".

## Savegame management for Paradox games

Since keeping track of all your savegames is getting more complicated
with every new savegame you start or backup you revert to,
the Pdx-Unlimiter provides you with an easy to use interface for choosing the right save file.
This is done by collecting all savegames and creating a browsable and editable
savegame history so you can load any prior saved version of a campaign. 
This works for both Ironman and Non-Ironman savegames.
All savegames can be also launched directly from
within the Pdx-Unlimiter without needing to vist the main menu.

f you want to revert to a previously imported savegame,
you can kill the currenty running Paradox game and load the latest save.
This kind of manual savegame management for ironman games allows you to easily revert to previous saves,
i.e. to savescum, which is kinda necessary for hard achievements.

For a full explanation of all features, refer to the
[Pdx-Unlimiter Guide](https://github.com/crschnick/pdx_unlimiter/blob/master/docs/GUIDE.md).

![Example](docs/screenshot.png)

## Full Mod support

Savegames that use mods are also fully supported.
This includes displaying modded country information in the savegame manager and mod installation support.
When you launch a savegame from within the Pdx-Unlimiter, the required mods and DLCs are automatically
enabled without having to use Paradox launcher configurations.
It is also checked whether a mod is missing or incompatible with the current version,
so that nothing can potentially corrupt your savegames.

## Achievements and challenges

The Pdx-Unlimiter has built-in support for [Rakaly](https://rakaly.com),
a website to analyze and share your eu4 achievements and compete against other players.
It is possible to upload savegames to Rakaly to share your achievements and compete with others.
You can also download savegames of others to inspect their campaign.
If you are interested in Rakaly, you can also join the [Rakaly Discord](https://discord.gg/WMJPnvSe).

## Community and Support

If you have suggestions, need help, run into any issues or just want to talk to other friendly people,
you can join the [Pdx-Unlimiter Discord](https://discord.gg/afErBW9Z).
You can also help the project by reporting issues, fixing bugs and making the planned issues a reality.
Contributing guidelines are coming soon. 

## Building and running

To build this project, [JDK 15](https://openjdk.java.net/projects/jdk/15/) is required.
You can build the gradle project with `gradlew build` or `gradlew.bat build`.
To create a jlink image, use `gradlew createDist`.
For running, you can use `gradlew run`.
To correctly run the Pdx-Unlimiter in a development environment,
you need to set the property `dataDir=<installation directory>`
in the `pdxu.properties` file. This is needed for a dev build to
specify where to save data such as savegames and settings.
