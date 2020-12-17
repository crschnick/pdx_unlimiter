## Pdx-Unlimiter Guide

Upon installation, you probably have a lot of savegames of different Paradox games on your computer,
either saved in your documents directories or your Steam cloudsave directories.

The Pdx-Unlimiter will try to automatically detect any of those game installation and save game directories.
If any installation is not detected, you can set them manually in the settings menu.

Furthermore, the Pdx-Unlimiter has an internal savegame storage independent of the savegame directories of installed games.
The default location is `<User Home>\Pdx-Unlimiter\savegames` on windows and `<User Home>/.Pdx-Unlimiter/savegames` on Linux.
You can however change this location in the settings menu.

### Importing savegames

There are several ways of importing savegames into the Pdx-Unlimiter storage:

- By clicking the 'import' button in the menu bar which will open a separate import dialog
- Dragging and dropping the savegame files into the Pdx-Unlimiter. This also works for folders that contain savegames
- Using the import status bar at the bottom that will open after you quit a Paradox game allows you to import the latest savegame
- Pressing `CTRL+SHIFT+I` while a supported game is open will import the latest savegame.
  This also works if the game is in fullscreen mode
  If the key combination is pressed, a confirmation sound will play
- Double clicking any savegame file if its extension is associated with the Pdx-Unlimiter.
  This includes `.eu4` `.hoi4`, `.ck3` and `sav` files.
- Opening an URL using the `pdxu` protocol. For example, opening `pdxu://rakaly.com/eu4/saves/_k7SRqT-1C7TQz3bD6EzR`
  will automatically download and import the savegame from Rakaly
  
In the settings menu you can also enable the option `Delete on import` which specifies
whether to delete savegames after succesfully importing it into the Pdx-Unlimiter storage.
  
### Managing savegames

TODO

### Launching savegames

You can launch a selected savegame directly by clicking `Launch` in the bottom status bar.
This direct launch will automatically attempt to enable every required mod and dlc.
If some content is missing, or the game version is not compatible, a warning will be shown.
This will bypass the Paradox launcher and main menu, i.e. saving a lot of time.

By clicking `Export` in the status bar, you can copy the savegame into savegame directory of the current game.
This will however not launch the game automatically.

Any supported paradox game can also be started from within the Pdx-Unlimiter even
without selecting a savegame by clicking the `Launch` button in menu bar.
This will start the Paradox Launcher and Steam if needed. If you don't want
to start Steam as well, you can disable this feature in the settings.

### Reverting savegames

In case of any unfortunate ingame event that you want to revert,
the Pdx-Unlimiter gives you the ability to savescum, i.e. reverting to the previous save when playing in ironman.
By pressing `CTRL+SHIFT+K`, you can kill the currently running game without it overriding the latest savegame.
You can simply select your latest imported savegame in the Pdx-Unlimiter and launch it again.

### Exporting savegames

TODO