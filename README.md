# Pdx-Unlimiter

The Pdx-Unlimiter is a tool that provides a powerful savegame manager that allows
you to organize, filter, and play all of your savegames with ease.
Furthermore, it also comes with the ability to convert savegames to a non-ironman version,
a powerful savegame and game data file editor, savescumming tools, and integrations for various
other great community-made tools like 
[Rakaly](https://rakaly.com), [Skanderbeg](https://skanderbeg.pm) and the
[CK3 to EU4 converter](https://github.com/ParadoxGameConverters/CK3toEU4).

## Download

Below you can find installers for all supported platforms.

#### Windows

Download the
[windows installer](https://github.com/crschnick/pdxu_launcher/releases/latest/download/pdxu_installer-windows.msi).

Note that there might be some issues with Windows SmartScreen blocking the installer.
If you are facing these issues, click on "More info" and then "Run anyway".

#### Linux

Download the
[.deb installer](https://github.com/crschnick/pdxu_launcher/releases/latest/download/pdxu_installer-linux.deb)

Note that the Linux version is relatively new and has only been tested on a few systems.
Please report any issues that you are having with it.

# Features

The main components of the Pdx-Unlimiter are:
- [Savegame manager](#savegame-management)
- [savegame reverter](#savegame-reverter-savescumming)
- [Savegame converter](#savegame-converter)
- [Savegame editor](#savegame-editor)
- [Third party tool support](#third-party-integration)

It you want to know how to use a certain feature, you can just
read the [Pdx-Unlimiter Guide](https://github.com/crschnick/pdx_unlimiter/blob/master/docs/GUIDE.md).
There is also a short [FAQ page](https://github.com/crschnick/pdx_unlimiter/blob/master/docs/faq.md)
to answer some of your questions.


## Savegame manager

Since keeping track of all your savegames is getting more complicated
with every new savegame you start or backup you revert to,
the Pdx-Unlimiter provides you with an easy-to-use interface for choosing the right save file.
This is done by collecting all savegames and creating a browsable and editable
savegame history so you can load any prior saved version of a campaign. 
This works for both Ironman and Non-Ironman savegames.
All savegames can be also launched directly from
within the Pdx-Unlimiter without needing to visit the main menu.

You can also organize your savegames by renaming them or creating custom folders.

![Example](docs/screenshot.png)

Savegames that use mods are also fully supported.
This includes displaying modded country information in the savegame manager and mod installation support.
When you launch a savegame from within the Pdx-Unlimiter, the required mods and DLCs are automatically
enabled without having to use Paradox launcher configurations.
It is also checked whether a mod is missing or incompatible with the current version,
so that nothing can potentially corrupt your savegames.


## Savegame converter

Internally, the Pdx-Unlimiter uses [Rakaly](https://github.com/rakaly) tools to convert
any ironman savegame into a text-based, non-ironman savegame.
It also provides an easy-to-use interface to convert
your own ironman savegames into playable non-ironman savegames.
This is useful if you have ever encountered a situation in which you wanted to
create a non-ironman copy of an ironman savegame, but couldn't because the games don't let you do it.


## Savegame editor

The Pdx-Unlimiter also features a savegame editor.
With this editor you can edit any file that is saved in a Paradox text format.
This includes EU4 and CK3 non-ironman savegames,
any game data files for mods and other purposes, and all Stellaris savegames.
Note that you can use the included ironman converter
to first create a non-ironman savegame and then edit it.

![Editor](docs/editor.png)


## Savegame reverter ("savescumming")

The Pdx-Unlimiter allows you to create a sort of savegame checkpoints
using keyboard shortcuts while being ingame that you can always revert back to.
If you want to revert to a previous savegame checkpoint,
you can also kill the currently running Paradox game and load the latest save using a keystroke.
This feature allows you to easily savescum, which is kinda necessary for hard achievements.


## Third party tool integration

The Pdx-Unlimiter is also built as an application that gives you easy access to many
other great community-made tools.
The supported tools are:

- [Rakaly](https://rakaly.com),
  a website to analyze and share your eu4 achievements and compete against other players.
  It is possible to upload your EU4 savegames to rakaly and analyze them from there.

- [Skanderbeg](https://skanderbeg.pm),
  a website to generate maps and statistics for your savegames.
  It is possible to upload your EU4 savegames to skanderbeg with just one click.

- The [CK3 to EU4 converter](https://github.com/ParadoxGameConverters/CK3toEU4).
  This means that you can interact with the converter from within the Pdx-Unlimiter
  and basically convert any managed savegame with just one click as well.

## Community and Support

If you have suggestions, need help, run into any issues or just want to talk to other friendly people,
you can join the [Pdx-Unlimiter Discord](https://discord.gg/afErBW9Z).
You can also help the project by reporting issues, fixing bugs and making the planned issues a reality.
Contributing guidelines are coming soon. 

## Development

To build this project, [JDK 15](https://openjdk.java.net/projects/jdk/15/) and gradle is required.
You can build the gradle project with `gradlew build` or `gradlew.bat build`.
For running, you can use `gradlew run`.

To correctly run the Pdx-Unlimiter in a development environment,
you can set the property `dataDir` in the `pdxu.properties` file to any directory such that your development
environment does not interfere with your Pdx-Unlimiter installation data.
If not set, the data directory of your Pdx-Unlimiter installation is used.

Any contribution is welcomed!
There are no real formal contribution guidelines right now, they will maybe come later.
