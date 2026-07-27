# Changelog

All notable changes to the Silvery Music app are documented here. Newest first.

## v0.2.0 — 2026-07-27

Player controls, a working equalizer, Liked Songs, and smoother navigation.

### New

- **The equalizer now shapes real audio.** It was previously cosmetic — dragging a
  band only moved the on-screen curve. The EQ now binds an Android `AudioEffect`
  to the ExoPlayer audio session and applies the 7-band curve to what you hear.
  The UI's bands (60 Hz–15 kHz) are mapped onto the device's own bands by
  log-frequency interpolation, so presets and custom curves both take effect. The
  master toggle bypasses the effect. On the rare device with no EQ effect
  available, audio simply plays unprocessed instead of failing.
- **Liked Songs.** Tap the heart on any track and it collects under
  **Library → Liked Songs** — a shortcut card at the top of Library plus a
  dedicated screen with play-all, play-from-track, and unlike. (Likes are
  session-only; this build has no database by design.)
- **Shuffle the queue.** The player's "I'm Feeling Lucky" action is replaced by a
  **Shuffle** control that randomises the not-yet-played tail of the queue without
  interrupting the current track, with a confirmation toast.
- **Repeat button.** A repeat control on the player transport cycles
  **Off → Repeat All → Repeat One** on each tap, using minimalist `Repeat` /
  `RepeatOne` icons that match the rest of the app. It drives ExoPlayer's own
  repeat mode.
- **Smooth, native-feeling navigation.** All destinations now share a quick
  cross-fade, and Search rises gently from the search bar and settles back — so
  moving between the tabs and Search reads as one continuous surface rather than a
  stack of separate pages.
- **Swipe-down to dismiss the player.** Pull down anywhere on the Now Playing
  screen to slide it back to your music; it follows your finger, fades, and springs
  back if you don't pull far enough. A drag handle signals the gesture.
- **"How Silvery works" explainer.** A short tour shown once during onboarding and
  reachable anytime from **Settings → About**, covering the music-only model,
  queue sovereignty, discovery control, and where Settings lives (the ⋮ on the
  player).

### Fixed

- Equalizer changes had no effect on playback; they now process the audio.
- Navigating from Search back to a tab is consistent and animated — tapping a
  bottom-nav icon reliably lands on that tab and closes Search.

### Under the hood

- Added the `MODIFY_AUDIO_SETTINGS` permission for the equalizer AudioEffect.
- `PlaybackController` / `MusicRepository` gained `likedTracks`, `repeatMode`,
  `cycleRepeatMode()`, `shuffleQueue()`, and `applyEqualizer()`; the shuffle only
  rewrites the queue tail after the current item, so playback never restarts.

## v0.1.0

Initial Android build: Home, Discover, Library, Search, Player (with in-place
lyrics), Queue, Discovery Control, Equalizer UI, Profiles, and Settings, streaming
Creative Commons tracks from Jamendo with an offline sample fallback.
