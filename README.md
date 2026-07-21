# Tweaks

A client-side Fabric mod for Minecraft **1.21.11** that lets you customize the **scoreboard**, **tab list** and **subtitles**: position, scale, colors, visibility and more. Works on any server — nothing is installed server-side.

Made by **SSrodri_**.

## Features

Every module (Scoreboard, Tab List, Subtitles) supports:

| Setting | Description |
|---|---|
| Enabled | Turn the module's tweaks on/off |
| Horizontal / Vertical Anchor | Pin to any screen edge or center |
| Offset X / Y | Fine position adjustment in pixels |
| Scale | 0.5x to 3x |
| Text Shadow | Draw text with or without shadow |
| Background | RGBA color (`#RRGGBBAA`) |

**Scoreboard**: hide the sidebar completely, hide the red score numbers.

**Tab List**:
- Toggle Mode — press Tab once to open, again to close (like toggle sprint)
- You On Top — always show your own name first in the list
- Hide header / footer / ping
- Numeric Ping — exact ping in ms, color-coded; your own ping is measured live (4x per second) with vanilla query-ping packets
- Row background color (RGBA)

**Subtitles**: display time multiplier, max subtitles shown (0 = unlimited), direction arrows toggle.

With default settings every module renders pixel-identical to vanilla.

## Requirements

- Minecraft 1.21.11
- [Fabric Loader](https://fabricmc.net/) 0.19.3+
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, adds a config button)

## Usage

Press **O** in game (rebindable under Options → Controls → Tweaks) or use the config button in Mod Menu. Settings are saved to `config/tweaks.json`.

Colors use RGBA hex: `#RRGGBBAA` where `AA` is opacity (`00` invisible → `FF` solid). `#RRGGBB` is accepted as fully opaque. Example: `#00000080` is black at 50% opacity.

## Building

```bash
./gradlew build
```

The jar is generated in `build/libs/`.

## License

[MIT](LICENSE.txt) © 2026 SSrodri_
