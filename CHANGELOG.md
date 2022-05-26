## Changes in 2.10.0:
- Add new branching functionality:
    - This allows you to create separate branches for both normal and Ironman savegames
    - When descendants of these branched savegames are imported again,
      they will be automatically assigned to the right campaign
    - This makes folders obsolete. Therefore, there will no longer be any folders in 2.10.
      All existing folders are converted into campaigns
    - Melted savegames will automatically create a new campaign branch
    - The moving savegames feature is also replaced by the branching feature
- Add Rakaly Ironman converter support for HOI4 1.11.11
- Add japanese translations (Thanks to Ikumyon)
- Fix Stellaris 3.4 savegames failing to import
- Fix various issues with unusupported characters when exporting a savegame
- Fix wrong display information for CK3 <= 1.4 savegames
- Fix many other bugs