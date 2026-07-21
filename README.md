---------------------------------------------------------------------
  Tweaks v1.0.0
  by SSrodri_
---------------------------------------------------------------------

Customize Minecraft's scoreboard, tab list and subtitles: position,
scale, colors, visibility and more. Client-side only — works on any
server, nothing is installed server-side.

---------------------------------------------------------------------
 REQUIREMENTS
---------------------------------------------------------------------
  - Minecraft 1.21.11
  - Fabric Loader 0.19.3 or newer
  - Fabric API
  - Mod Menu (optional, adds a config button in the mod list)

---------------------------------------------------------------------
 INSTALLATION
---------------------------------------------------------------------
  Drop the Tweaks jar (plus Fabric API) into your .minecraft/mods
  folder and start the game with the Fabric loader profile.

---------------------------------------------------------------------
 OPENING THE CONFIG
---------------------------------------------------------------------
  - Press O in game (rebindable under Options > Controls > Tweaks)
  - Or use the Tweaks config button in Mod Menu
  Settings are saved to .minecraft/config/tweaks.json and can also be
  edited by hand.

---------------------------------------------------------------------
 SETTINGS
---------------------------------------------------------------------
  Every module (Scoreboard, Tab List, Subtitles) has:
    Enabled            - turn the module's tweaks on/off
    Horizontal Anchor  - left / center / right of the screen
    Vertical Anchor    - top / center / bottom of the screen
    Offset X / Y       - fine position adjustment in pixels
    Scale              - 0.5x to 3x
    Text Shadow        - draw text with or without shadow
    Background         - RGBA color (see COLOR FORMAT below)

  Scoreboard:
    Hide Scoreboard    - hide the sidebar completely
    Hide Numbers       - hide the red score numbers

  Tab List:
    Toggle Mode        - press Tab once to open, again to close
                         (like toggle sprint)
    You On Top         - always show your own name first in the list
    Hide Header        - hide the server's header text
    Hide Footer        - hide the server's footer text
    Hide Ping          - hide the connection bars
    Numeric Ping       - show the ping in ms (color-coded) instead
                         of bars; your own ping is measured live
                         (4 times per second) for an exact value
    Rows               - RGBA color of the strip behind each name

  Subtitles:
    Display Time       - how long subtitles stay (0.25x to 4x vanilla)
    Max Subtitles      - maximum shown at once; 0 = unlimited
    Direction Arrows   - show the < > sound direction arrows

---------------------------------------------------------------------
 COLOR FORMAT
---------------------------------------------------------------------
  Colors are RGBA hex strings: #RRGGBBAA
    RR GG BB = red / green / blue (00-FF)
    AA       = opacity (00 = invisible, FF = solid)
  #RRGGBB without alpha is also accepted and treated as fully opaque.
  Examples:
    #00000080  black at 50% opacity
    #FF000040  red at 25% opacity
    #FFFFFF    solid white

---------------------------------------------------------------------
 NOTES
---------------------------------------------------------------------
  - With default settings every module renders pixel-identical to
    vanilla, so you only see changes for what you configure.
  - "You On Top" tries to find you by profile, by name and by display
    name; on servers that build a completely fake tab list it may not
    always be able to identify your entry.
  - Delete config/tweaks.json to reset everything to defaults.

---------------------------------------------------------------------
 LICENSE
---------------------------------------------------------------------
  MIT License - Copyright (c) 2026 SSrodri_. See LICENSE.txt.

