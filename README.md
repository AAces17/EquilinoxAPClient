<p align="center">
  <img width="75%" alt="EQAP Logo Color" src="https://github.com/user-attachments/assets/2493c5da-bc15-4ee2-b508-b2e0f799ff74" />
</p>

# Equilinox AP

Archipelago client for the game *Equilinox*.

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#setup-instructions">Setup Instructions</a>
    </li>
    <li>
      <a href="#connecting-to-a-multiworld">Connecting to a Multiworld</a>
    </li>
    <li>
      <a href="#updating-the-mod">Updating the Mod</a>
    </li>
    <li>
      <a href="#yaml-options">YAML Options</a>
    </li>
    <li>
      <a href="#goal--check--item-information">Checks/Items</a>
    </li>
    <li>
      <a href="#known-issues">Known Issues</a>
    </li>
    <li>
      <a href="#reporting-issues">Reporting Issues</a>
    </li>
  </ol>
</details>

# Download the Latest Release [Here](https://github.com/AAces17/EquilinoxAPClient/releases/latest)

> [!IMPORTANT]
> Currently, the mod only supports the Windows version of the game (and has only been tested on the Steam installation). I hope to add support for other operating systems in the future.

## Setup Instructions

### Host
Only the host needs to have the Archipelago Client installed. General archipelago installation / world generation will not be covered here, 
instead see [the official guide](https://archipelago.gg/tutorial/Archipelago/setup_en). The host needs the `equilinox.apworld` file found in the [Releases](https://github.com/AAces17/EquilinoxAPClient/releases/latest) tab. Download this file, then double-click it to 
automatically place it in the `custom_worlds` folder. [Click here to view the source code of the .apworld file](https://github.com/AAces17/AAcesArchipelago/tree/main/worlds/equilinox).

### Player

You need to own a copy of the game to run this mod, it is not a stand-alone application. 
Download the latest `EquilinoxAP.zip` file from the [Releases](https://github.com/AAces17/EquilinoxAPClient/releases/latest) tab. Opening this zip file should show a similarly named folder; place this `EquilinoxAP` folder into the installation directory of the game. 
If you installed the game via Steam, you can find this by going to `Manage -> Browse local files` from the game's library page. After doing this, the game directory should look something like this:

<img width="75%" alt="image" src="https://github.com/user-attachments/assets/01b02f71-98af-4d5b-a550-6c452e3a19f3" />


> [!WARNING]
> The mod should not be able to touch your existing save files, since it will create a new folder for AP saves. However, to be safe, I recommend backing up your existing save files. To do this, create a copy of the `Equilinox_0_Saves` folder somewhere safe.

On a fresh installation of the mod, this `EquilinoxAP` folder should have a `res` folder and three files (the mod .jar and two .bat files):

<img width="75%" alt="image" src="https://github.com/user-attachments/assets/d36644a5-417e-4d26-a524-753612fd6a74" />


To run the game with the Archipelago mod, double click the `Run Equilinox AP.bat` file. A terminal window will open, but you can close this if you'd like. 

When running the game this way, 
a new UserConfigs file is created with the default settings; if you would like any options you changed (graphic settings, etc) to carry over to the modded launch, 
copy the `Equilinox_0_UserConfigs.dat` file from the root directory into the EquilinoxAP folder. Alternatively, just change the settings again once the game loads, after which they will be saved.

To run the game without the Archipelago mod, open the game via Steam or the original .exe file -- the mod is run if and only if one of the .bat files in the EquilinoxAP folder is used to start the game.

## Connecting to a multiworld

### For the first time

After running the game via the mod loader, the main menu should look something like this. If you see a rainbow oval instead of the logo, that means the mod failed to load the images.

<img width="75%" alt="image" src="https://github.com/user-attachments/assets/57baf3c8-0a07-460d-8989-919bae2f7e00" />
</br>


To connect to a multiworld for the first time, click the `New World (Archipelago)` button and input the connection information. 
To edit a text field, click the square button on the right-hand side of it. Then click that button again to stop editing that field before you start editing the next field,
or you will be typing in both fields at the same time (see <a href="#known-issues">Known Issues</a>).

<img width="75%" alt="image" src="https://github.com/user-attachments/assets/6d7e4db4-a378-4e7a-b7ee-b56349830811" />
</br>

  
Then select the desired world options (note, only "Normal" mode is supported, so the option to change the Game Mode has been removed) and click `Generate World & Connect`. 
You can make the World Name whatever you want\* -- it does not have to start with "\[AP\]".  

> [!IMPORTANT]
> Whenever you launch the game via the mod loader, you *must* either create a new world with the `New World` button, or load an existing world with the `Load World` button. Pressing the `Play` button without first
> doing one of these will open up a default world, where nothing is saved and you can not connect to a multiworld. If you accidentally press `Play`, just press esc to return to the main menu and connect properly.
>
> Once connected, you *can* use the `Play` button to return to the game from the menu.

\**Be weary of including special characters in the name of a save file (see the <a href="#known-issues">Known Issues</a> section)*

### Reconnecting to an existing world

To reload an existing world, simply load the save file for the world and the mod will automatically attempt to reconnect (the connection info is saved in the save file):

<img width="75%" alt="image" src="https://github.com/user-attachments/assets/e92790d0-4949-4afe-a386-fb5779837d78" />


Notice that the save preview does not display the AP connection info for that save yet. 
This is something I plan to add eventually, but in the meantime, if you are playing in more than one multiworld at once, it may be best to set the world names accordingly when creating them.

### Changing connection info

If the server port changes, or if you made a typo when creating the save file and don't want to back out an make a new world, you can change this in-game by clicking the "Archipelago Settings" button on the toolbar: 

<img width="90%" alt="image" src="https://github.com/user-attachments/assets/e86b678e-08d7-412f-83c7-8238d093ae92" />


> [!CAUTION]
> The menu that opens when you click this button will display the server password in plain text, as does the new world generation menu. I plan to address this in a future update.

You can then update the connection info and click "Connect". Eventually, I plan on adding info about completion progress and connection status to this menu.

<img width="75%" alt="image" src="https://github.com/user-attachments/assets/4086b344-7664-4d98-ab4e-e2e26882910d" />


## Updating the Mod

To update the mod, you can replace the entire `EquilinoxAP` folder with the new one from the new .zip file. This will reset your config file, so copying that (the `Equilinox_0_UserConfigs.dat` file in the EquilinoxAP folder) first may be desirable. 
Replacing the EquilinoxAP folder will *not* touch your existing save files, since they are saved in the `Equilinox_AP_Saves` folder in the game directory. 
However, you should still pay attention to compatibility warnings in the release notes for a new version. 

## YAML Options

### `goal_selection`:

Currently, there are two possible goal conditions: 

| Option      | Description |
| ----------- | ----------- |
| `evolve_camel` | Evolve a Camel |
| `completionist` | Complete every task |

Recommendations for additional goal conditions are welcome.

### `free_evolution`:
Default: `true`.

If `true`, starting an evolution costs 0 DP. The other requirements must still be met. If `false`, the vanilla behavior remains (charging DP to start an evolution).

### `tasks_reward_dp`:
Default: `true`.

If `true`, tasks will give their normal DP amount upon completion, in addition to sending a check. If `false`, completing tasks just sends a check, and some of the small
filler DP items are replaced with larger DP items.

### `starting_dp`:
Default: `5000`.
Range: `2500` - `100000`.

The vanilla default starting amount is 2,500 DP. The actual starting DP value will be close to the value of this setting, but not exact.

### `itempool_dp`:
Default: `150000`.
Range: `0` - `500000`.

Can be used to add additional large DP items to the item pool. As with `starting_dp`, the actual value of the added DP items will be close to this, but not exact.

### `size_checks`:
Default: `true`.

Include checks for breeding species of size 1.10 and size 1.20. Adds 288 checks (or 286 if `evolve_camel` is the goal).

## Goal / Check / Item Information

### Goal options

See YAML options above for a description of the possible goal conditions.

### Checks

Currently, there are two types of checks that are always included:

<ul>
  <li>
    Task Checks - each task sends a check upon completion (60 checks, or 59 if "Completionist" is the goal)
  </li>
  <li>
    Evolution Checks - evolving a species for the first time sends a check (107 checks, or 106 if "Camel" is the goal)
  </li>
</ul>

In the yaml, you have the option to turn on additional checks:

<ul>
  <li>Size Checks - breeding a species of size 1.10 is a check, and breeding a species of size 1.20 is a check, for each species (288 checks, or 286 if "Camel" is the goal)</li>
</ul>

Additional optional checks planned but not yet implemented: 

<ul>
  <li>More trait checks</li>
  <li>Biome checks</li>
  <li>Species count checks</li>
</ul>

### Items

Currently, there are only two types of items. All of the base species need to be unlocked by an item (eg, "Sheep Permit" to unlock the sheep), and DP can be received in varying amounts (100 - 100,000). 
Filler DP items range from 100dp to 10,000dp, with amounts around 1,000 most common. Suggestions for additional items / item types are welcome.

## Known Issues 

<ul>
  <li>
    On pages with multiple text input fields, you click the icon that looks like a pen in a square to edit the field. You then need to click it again to stop editing that field before editing the next text field. This happens because I am 
    using the game's built-in text field object, which does not automatically deselect when you select a different one (because in the vanilla game, there are never 2+ text fields in the same menu). This is something I'd like to fix
    because it is kind of annoying, but it doesn't break anything. Since it will likely be somewhat involved to actually fix, it'll be left as-is for now.
  </li>
  <li>
    Certain characters in a world (save file) name stop the game from loading the world properly. I know of "-" and "." breaking things, so if you put these in the "World Name" field, it just removes them before creating the world.
    If you create a world that later will not show up in the "load world" menu, send me the world save file and I'll take a look at it (see below for instructions on how to do this, in the Reporting Issues section).
  </li>
  <li>
    If the mod fails to load the images, they are replaced with a rainbow wheel. This is most noticable on the main menu, where the game logo would get replaced with a rainbow oval. This is not an issue in and of itself, but it does
    mean there was an issue loading the assets. Make sure there is a folder called "res" in the mod folder, and that it contains .png files.
  </li>
  <li>
    Deathlink is not currently supported. If you have ideas for what deathlink would look like in this game, feel free to share in the Equilinox thread in the AP discord server.
  </li>
</ul>

## Reporting Issues

Feel free to report bugs or other issues in the Equilinox thread on the AP discord server, or in the "Issues" tab on this Github repository. 

<img width="80%" alt="image" src="https://github.com/user-attachments/assets/3b17fe7f-0ca7-4811-b032-37e005b2b525" />


If you encounter a bug, please make a copy of the `APEqLog.log` file in the EquilinoxAP mod directory before restarting the game (this file gets wiped each time the game starts). If the game *crashes*, it should create a folder called `EquilinoxErrorLogs`
in the mod directory, with an `ERROR_<numbers>.txt` file. Both of these files can help me diagnose an issue. 

I could also benefit from seeing the save file for the world, which you can find in the `Equilinox_AP_Saves` folder. Note, this folder gets created in the main game directory, not in the mod's EquilinoxAP file.
This means you can update the mod without losing existing save files (however, you should pay attention to any compatibility warnings on new releases). If your issue pertains to save file loading, then I will definitely want to see this file.

<img width="80%" alt="image" src="https://github.com/user-attachments/assets/5263b10f-35b5-44f7-883c-97486a638ae1" />

Inside this folder should be your save files. Note that the game can not read these files without the mod running, so you shouldn't have to worry about them if you decide to play vanilla. 

<img width="80%" alt="image" src="https://github.com/user-attachments/assets/83939a30-3b51-4ab2-8248-03f89a327d92" />

> [!WARNING]
> Connection information is saved in your save file. Without the game, the .dat file alone is not enough to view it (ie, you can't just open the file in Notepad). But if someone else loads your save file in game, it will try to connect automatically.

If you don't feel comfortable sharing these files in the public server, you are welcome to DM them to me on Discord instead (username `aaces17`).
















