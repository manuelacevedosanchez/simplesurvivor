# Sound Effects for SimpleSurvivor

## Quick Download (Recommended)

Download these free CC0 sound packs and extract the sounds you need:

### Option 1: Kenney.nl Sci-Fi Sounds (Best for this game)
**URL:** https://kenney.nl/assets/sci-fi-sounds
- License: CC0 (Public Domain - no attribution required)
- Contains: Lasers, explosions, UI sounds
- Perfect for: shoot.wav, explosion.wav, hit.wav

### Option 2: Kenney.nl UI Audio
**URL:** https://kenney.nl/assets/ui-audio
- License: CC0
- Contains: Button clicks, confirmations
- Perfect for: button_click.wav, shop_purchase.wav

### Option 3: Kenney.nl Impact Sounds  
**URL:** https://kenney.nl/assets/impact-sounds
- License: CC0
- Contains: Various impacts
- Perfect for: hit.wav, explosion.wav

---

## Required Sound Files

Place these files in `assets/sounds/`:

| File | Duration | Description | Suggested Source |
|------|----------|-------------|------------------|
| `shoot.wav` | ~0.2s | Laser/blaster shot | Kenney Sci-Fi: `laserSmall_000.ogg` |
| `explosion.wav` | ~0.5s | Enemy destroyed | Kenney Sci-Fi: `explosionCrunch_000.ogg` |
| `powerup.wav` | ~0.3s | Collect power-up | Kenney Sci-Fi: `powerUp1.ogg` |
| `hit.wav` | ~0.2s | Player takes damage | Kenney Impact: `impactMetal_000.ogg` |
| `game_over.wav` | ~1.5s | Game over | Kenney Sci-Fi: `lowDown.ogg` |
| `button_click.wav` | ~0.1s | UI button click | Kenney UI: `click1.ogg` |
| `level_up.wav` | ~0.8s | Level/milestone | Kenney Sci-Fi: `powerUp4.ogg` |
| `shop_purchase.wav` | ~0.3s | Shop purchase | Kenney UI: `confirmation_002.ogg` |

---

## Converting OGG to WAV (if needed)

If you download OGG files and need WAV:

### Using FFmpeg:
```bash
ffmpeg -i input.ogg output.wav
```

### Using Audacity:
1. Open OGG file
2. File → Export → Export as WAV

### Online converter:
https://cloudconvert.com/ogg-to-wav

---

## Audio Specifications

- **Format:** WAV (uncompressed) - LibGDX loads these faster
- **Sample Rate:** 44100 Hz
- **Bit Depth:** 16-bit
- **Channels:** Mono (smaller files, works with panning)
- **Max File Size:** Keep under 100KB each

---

## Alternative Free Sources

1. **OpenGameArt.org**
   - https://opengameart.org/content/sci-fi-sound-effects
   - https://opengameart.org/content/space-shooter-sounds

2. **Freesound.org** (requires free account)
   - https://freesound.org/search/?q=laser+8bit
   - https://freesound.org/search/?q=explosion+game

3. **Pixabay**
   - https://pixabay.com/sound-effects/search/game/

---

## Testing Sounds

After adding sounds, test with:
```kotlin
// In any screen's show() method:
AudioManager.playShoot()
AudioManager.playExplosion()
```



