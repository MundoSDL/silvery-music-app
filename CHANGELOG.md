# Changelog

All notable changes to the Silvery Music app are documented here. Newest first.

## v0.2.5 — 2026-07-28

The app now remembers you, plays from the notification shade, and every "See all"
actually goes somewhere.

### New

- **"See all" opens a full list.** The link beside Recently Played, Made For You,
  Your Top Genres, Your Artists and Browse Genres was previously decorative text.
  Each now opens a dedicated screen listing everything in that section, with the
  same tap-to-play behaviour as the rest of the app.
- **Now Playing in the notification shade and on the lockscreen.** Playback runs
  in a media session service, so you can see what's playing and control it
  without opening the app: play/pause, previous, next, plus **like** and
  **repeat** buttons whose icons follow the current state. Swiping the app away
  from Recents stops playback and clears the notification.
- **A like button on the player.** The heart sits on the trailing edge of the
  track title, above the progress bar, and stays in step with the mini player,
  Liked Songs and the notification.
- **The app remembers you.** Onboarding runs once: after you create an account or
  continue as a guest, that choice is saved on the device and later launches open
  straight to Home.
- **Liked songs are saved on your phone** and survive a restart. Each profile
  keeps its own collection, so switching to the kid profile shows that profile's
  liked songs rather than the main account's.
- **Your name becomes your profile.** The name entered at sign-up names the main
  profile; continuing as a guest labels it **Guest**. The kid profile stays
  alongside it either way as a demo of profile switching.
- **Sign out**, under **Settings → Account**. It asks first and says exactly what
  it clears — the saved session, your profiles and every liked song — then
  returns to onboarding.

### Fixed

- **The app no longer crashes shortly after playback starts.** The playback
  service never registered its media session, so no notification was posted and
  Android shut the app down for failing to start in the foreground in time. This
  was also why nothing appeared in the notification shade — one cause, both
  symptoms.
- **Playback state no longer half-breaks after the app is swiped away.** If the
  process survived, reopening the app left a player that produced sound but never
  advanced the progress bar.
- The progress bar now keeps moving when playback is started from the
  notification rather than from inside the app.
- The version shown in Settings is read from the build, so it can't drift out of
  date again.

### Under the hood

- Persistence is SharedPreferences plus JSON — no new dependencies and no
  database. Session, profiles and per-profile likes each have a small store, all
  of which fall back to memory for previews and tests.
- Twelve Robolectric tests cover the persistence behaviour by rebuilding the
  stores over the same storage, which is what a fresh process does.
- The first navigation route to take an argument (`see_all/{section}`).

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
