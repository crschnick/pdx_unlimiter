# Pdx-Unlimiter

The Pdx-Unlimiter is a tool aimed at improving your gameplay experience for various Paradox Games.
It does that by providing a powerful savegame manager that allows
you to organize, upload, play and delete all of your savegames.
It also comes with full mod support, paradox game integration
and support for interacting with [Rakaly](https://rakaly.com) and [Skanderbeg](https://skanderbeg.pm).

It you want to familiarize yourself with all the features, you can
read the [Pdx-Unlimiter Guide](https://github.com/crschnick/pdx_unlimiter/blob/master/docs/GUIDE.md).
There is also a short [FAQ page](https://github.com/crschnick/pdx_unlimiter/blob/master/docs/faq.md)
to answer some of your questions.

## Download

Below you can find installers for all supported platforms.

#### Windows

Download the
[windows installer](https://github.com/crschnick/pdxu_installer/releases/latest/download/pdxu_installer-windows.msi).

Note that there might be some issues with Windows SmartScreen blocking the installer.
If you are facing these issues, click on "More info" and then "Run anyway".

#### Linux

Download the
[.deb installer](https://github.com/crschnick/pdxu_installer/releases/latest/download/pdxu_installer-linux.deb)

Note that the Linux version is relatively new and has only been tested on a few systems.
Please report any issues that you are having with it.

There is currently no .rpm installer for Fedora/CentOS/RHEL.
However, if there is a need for it, it should be possible to create one.

## Savegame management for Paradox games

Since keeping track of all your savegames is getting more complicated
with every new savegame you start or backup you revert to,
the Pdx-Unlimiter provides you with an easy-to-use interface for choosing the right save file.
This is done by collecting all savegames and creating a browsable and editable
savegame history so you can load any prior saved version of a campaign. 
This works for both Ironman and Non-Ironman savegames.
All savegames can be also launched directly from
within the Pdx-Unlimiter without needing to visit the main menu.

If you want to revert to a previously imported savegame,
you can also kill the currently running Paradox game and load the latest save.
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

## Third party integration

The Pdx-Unlimiter has built-in support for [Rakaly](https://rakaly.com),
a website to analyze and share your eu4 achievements and compete against other players.
If you are interested in Rakaly, you can also join the [Rakaly Discord](https://discord.gg/WMJPnvSe).

It is also possible to upload your savegames to [Skanderbeg](https://skanderbeg.pm),
a website to generate maps and statistics for your savegames.
If you are interested in Skanderbeg, you can also join the [Skanderbeg Discord](https://discord.gg/uzkMPjc).

## Community and Support

If you have suggestions, need help, run into any issues or just want to talk to other friendly people,
you can join the [Pdx-Unlimiter Discord](https://discord.gg/afErBW9Z).
You can also help the project by reporting issues, fixing bugs and making the planned issues a reality.
Contributing guidelines are coming soon. 

## Development

To build this project, [JDK 15](https://openjdk.java.net/projects/jdk/15/) is required.
You can build the gradle project with `gradlew build` or `gradlew.bat build`.
To create a jlink image, use `gradlew createDist`.
For running, you can use `gradlew run`.
To correctly run the Pdx-Unlimiter in a development environment,
you need to set the property `dataDir=<installation directory>`
in the `pdxu.properties` file. This is needed for a dev build to
specify where to save data such as savegames and settings.

Any contribution is welcomed!
There are no real formal contribution guidelines right now, they will maybe come later.
