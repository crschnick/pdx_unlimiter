<h1 align="center">
  <img width=60 height=60 src="misc/pdxu.png">
  <div>Pdx-Unlimiter</div>
</h1>

<div align="center">
  <a href="https://discord.gg/BVE4vxqFpU"><img alt="Discord" src="https://img.shields.io/discord/786465137191682088?logo=discord&logoColor=white"></a> <a href="https://github.com/crschnick/pdx_unlimiter/actions/workflows/gradle.yml"><img alt="CI" src="https://github.com/crschnick/pdx_unlimiter/actions/workflows/build.yml/badge.svg"></a>
</div>

The Pdx-Unlimiter is a tool for all major Paradox Grand Strategy games that provides a
powerful and smart savegame manager to quickly organize and play all of your savegames with ease.
Furthermore, it also comes with an Ironman converter,
a savegame editor, some savescumming tools, integrations for various other
great community-made tools, and full support for the following games:

* Europa Universalis V
* Victoria III
* Europa Universalis IV
* Crusader Kings III
* Hearts of Iron IV
* Stellaris
* Crusader Kings II
* Victoria II

## Downloads

### Windows

Installers are the easiest way to get started:

- [Windows .msi Installer (x86-64)](../../releases/latest/download/kickstartfx-installer-windows-x86_64.msi)

If you don't like installers, you can also use a portable version that is packaged as an archive:

- [Windows .zip Portable (x86-64)](../../releases/latest/download/kickstartfx-portable-windows-x86_64.zip)

### macOS

Installers are the easiest way to get started:

- [MacOS .pkg Installer (x86-64)](../../releases/latest/download/kickstartfx-installer-macos-x86_64.pkg)
- [MacOS .pkg Installer (ARM 64)](../../releases/latest/download/kickstartfx-installer-macos-arm64.pkg)

If you don't like installers, you can also use a portable version that is packaged as an archive:

- [MacOS .dmg Portable (x86-64)](../../releases/latest/download/kickstartfx-portable-macos-x86_64.dmg)
- [MacOS .dmg Portable (ARM 64)](../../releases/latest/download/kickstartfx-portable-macos-arm64.dmg)

### Linux

#### Debian-based distros

The following debian installers are available:

- [Linux .deb Installer (x86-64)](../../releases/latest/download/kickstartfx-installer-linux-x86_64.deb)

Note that you should use apt to install the package with `sudo apt install <file>` as other package managers, for example dpkg,
are not able to resolve and install any dependency packages.

#### RHEL-based distros

The following rpm installers are available:

- [Linux .rpm Installer (x86-64)](../../releases/latest/download/kickstartfx-installer-linux-x86_64.rpm)

The same applies here, you should use a package manager that supports resolving and installing required dependencies if needed.

#### Portable

In case you prefer to use an archive version that you can extract anywhere, you can use these:

- [Linux .tar.gz Portable (x86-64)](../../releases/latest/download/kickstartfx-portable-linux-x86_64.tar.gz)

Note that the portable version assumes that you have some basic packages for graphical systems already installed
as it is not a perfect standalone version. It should however run on most systems.

## Features

The main components of the Pdx-Unlimiter are:
- [Savegame manager](#savegame-manager)
- [Smart savegame launcher](#smart-launcher)
- [Ironman converter](#ironman-converter)
- [Savegame editor](#savegame-editor)
- [Savescumming tools](#savescumming-tools)
- [Integrations for other Paradox tools](#third-party-tool-integration)

If you want to know how to use a certain feature, you can
read about it in the [Pdx-Unlimiter Guide](https://github.com/crschnick/pdx_unlimiter/wiki/User-Guide).
If you like videos, there's now also a [tutorial video on YouTube](https://www.youtube.com/watch?v=i67Q2PxAnzQ).

### Savegame manager

Since keeping track of all your savegames is getting more complicated
with every new savegame you start or backup you revert to,
the Pdx-Unlimiter provides you with an easy-to-use interface for choosing the right save file of a campaign.
This is done by collecting all savegames and creating a browsable and editable
savegame history so you can load any prior saved version of a campaign.
All types of savegames are supported, including Ironman and modded savegames.
The smart launcher also allows you instantly continue a savegame
without having to visit the Paradox Launcher or main menu.

![Example](misc/screenshot.png)

![ExampleCK3](misc/ck3_screenshot.png)

**Usage guide:** [Savegame management](https://github.com/crschnick/pdx_unlimiter/wiki/User-Guide#savegame-importing-and-storage)



### Smart launcher

The problem of playing modded savegames it that you always need to enable the right mods in the Paradox Launcher.
It can become tedious to always keep track of which mods are required by which savegame.
The solution to this problem is that the Pdx-Unlimiter is a *smart* launcher
that is fully aware of which mods and DLCs are needed for a certain savegame,
*before* you start the game.

All savegames can be launched directly from
within the Pdx-Unlimiter without needing to visit the in-game main menu ever again.
When you directly launch a savegame from within the Pdx-Unlimiter,
the required mods and DLCs are automatically enabled without having to use Paradox launcher configurations.
It is also checked whether a mod is missing or incompatible with the current version,
so that nothing can potentially corrupt your savegames.
This means that you never have to worry about keeping track of your mod
configurations if you regularly switch between them.
Skipping the main menu also saves a lot of time when loading a savegame.

**Usage guide:** [Launching savegames](https://github.com/crschnick/pdx_unlimiter/wiki/User-Guide#launching-savegames)



### Savegame editor

The Pdx-Unlimiter comes with a savegame editor,
which can also be used as a savegame navigator to quickly find certain entries in a savegame.
You can use the included [Ironman converter](#ironman-converter) to also edit converted Ironman savegames.

![Editor](misc/editor.png)

The editor supports multiple different styles of editing savegame files.
The first one, the more simple version, allows you to edit already existing values for entries.
It is meant for users that are new to savegame editing, is relatively user-friendly,
and tries to prevent users from making savegames invalid or corrupt.
The second editing style allows you to use your
favourite text-editor and to completely change the data structure if wanted.
Using the Pdx-Unlimiter editor has the following benefits:

- You don't have to deal with compressed savegames, the editor does all
  the extracting and repackaging of zipped savegame files for you
  
- The editing process is broken down into editing smaller parts of the savegame.
  You can still use your favourite text editor to edit smaller parts of the savegame
  and therefore don't have to open and edit >50 MB files in your text-editor

- You can easily navigate the hierarchical data with filters compared to the
  laborious process of locating a specific entry with a text editor search function
  
**Usage guide:** [Editing savegames](https://github.com/crschnick/pdx_unlimiter/wiki/Editor-Guide)



### Ironman converter

Internally, the Pdx-Unlimiter uses the [pdx.tools](https://pdx.tools/) melter to convert
any ironman savegame into a text-based, non-ironman savegame.
It provides an easy-to-use frontend for the pdx.tools Ironman converter,
i.e. a graphical user interface to convert your own
ironman savegames into playable and editable non-ironman savegames.

This is useful if you have ever encountered a situation in which you wanted to
create a non-ironman copy of an ironman savegame, but couldn't because the games don't let you do it.
You can for example use the included ironman converter
to first create a non-ironman savegame and then edit it using the [Savegame editor](#savegame-editor).
Moreover, you can also use the in-game console when playing converted savegames.

**Usage guide:** [Ironman Converter](https://github.com/crschnick/pdx_unlimiter/wiki/User-Guide#savegame-actions)



### Savescumming tools

The Pdx-Unlimiter allows you to create savegame checkpoints using the GUI or keyboard shortcuts
while being ingame.
Having a history of previous savegames of your campaign available protects
you against bugs, glitches, and terrible luck that can easily ruin campaigns.
You can also enable a timed checkpoint option in the settings that will
automatically create a campaign checkpoint every *x* minutes.

If you want to load a previous savegame checkpoint,
you can also kill the currently running Paradox game and load the latest save using a keystroke.
This feature also allows you to easily savescum, which is kinda necessary for hard achievements.

**Usage guide:** [Savescumming](https://github.com/crschnick/pdx_unlimiter/wiki/User-Guide#savegame-checkpoints-and-savescumming)



### Tool integrations

The Pdx-Unlimiter is also built as an application that gives you easy access to many
other great community-made tools.
The supported tools are:

- The [Paradox game converters](https://paradoxgameconverters.com/).
  This means that you can interact with the converter from within the Pdx-Unlimiter
  and basically convert any managed savegame with just one click as well.

- The [Irony Mod Manager](https://bcssov.github.io/IronyModManager/), which can
  replace the mod-management capabilities of the Paradox launcher, can be designated to be used
  as the default launcher instead of the Paradox launcher.

**Usage guide:** [Savegame tools](https://github.com/crschnick/pdx_unlimiter/wiki/User-Guide#savegame-actions)



## Community and Support

If you have suggestions, need help, ran into any issues or just want to talk to other friendly people,
you can join the [Pdx-Unlimiter Discord](https://discord.gg/BVE4vxqFpU).

[![Banner](https://discordapp.com/api/guilds/786465137191682088/widget.png?style=banner3)](https://discord.gg/BVE4vxqFpU)



## Contributing

See [CONTRIBUTING.md](/CONTRIBUTING.md).
