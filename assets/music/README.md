# Music for SimpleSurvivor

## Quick Download (Recommended)

### Option 1: Kenney.nl Music (CC0)
**URL:** https://kenney.nl/assets/category:Audio
- License: CC0 (Public Domain)
- No attribution required

### Option 2: Kevin MacLeod - Incompetech (CC-BY)
**URL:** https://incompetech.com/music/royalty-free/music.html
- License: CC-BY 3.0 (attribution required)
- Professional quality

**Recommended tracks for this game:**

| For | Track Name | Direct Link |
|-----|------------|-------------|
| Menu | "Cylinder Six" | https://incompetech.com/music/royalty-free/mp3-royaltyfree/Cylinder%20Six.mp3 |
| Game | "Hitman" | https://incompetech.com/music/royalty-free/mp3-royaltyfree/Hitman.mp3 |
| Game | "Volatile Reaction" | https://incompetech.com/music/royalty-free/mp3-royaltyfree/Volatile%20Reaction.mp3 |

### Option 3: OpenGameArt
- https://opengameart.org/content/space-boss-battle-theme
- https://opengameart.org/content/through-space

---

## Required Music Files

Place these files in `assets/music/`:

| File | Use | Style |
|------|-----|-------|
| `menu_theme.ogg` | Main menu background | Ambient, space, calm |
| `game_theme.ogg` | Gameplay | Intense, action, driving |
| `game_over_theme.ogg` | Game over screen | Melancholic, short |

---

## Converting MP3 to OGG

LibGDX works best with OGG Vorbis for music:

### Using FFmpeg:
```bash
ffmpeg -i input.mp3 -c:a libvorbis -q:a 5 output.ogg
```

### Using Audacity:
1. Open MP3 file
2. File → Export → Export as OGG
3. Quality: 5-7 (good balance)

### Online converter:
https://cloudconvert.com/mp3-to-ogg

---

## Audio Specifications

- **Format:** OGG Vorbis (compressed, good quality)
- **Sample Rate:** 44100 Hz
- **Bitrate:** 128-192 kbps
- **Channels:** Stereo
- **Looping:** Menu and game themes should loop seamlessly

---

## Making Music Loop Seamlessly

1. Open in Audacity
2. Trim silence at start/end
3. Use crossfade if needed
4. Test loop: Effect → Repeat
5. Export as OGG

---

## Attribution

### menu_theme.ogg
"Hitman" by Kevin MacLeod (incompetech.com)
Licensed under Creative Commons: By Attribution 4.0 License
http://creativecommons.org/licenses/by/4.0/

### game_theme.ogg
By emanresU
Ko-fi: https://ko-fi.com/emanresu102396

### game_over_theme.ogg
By Devlin Bataric

---

## File Size Guidelines

Keep music files reasonable for mobile:
- Menu theme: < 2MB
- Game theme: < 3MB  
- Game over: < 500KB

Total music: < 6MB recommended



