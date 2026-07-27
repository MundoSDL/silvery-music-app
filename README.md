# Silvery

**Music. Just music.**

Silvery is a concept music-streaming app built around one idea — giving the
listening experience back to the listener. No feeds, no video tabs, no podcasts
bolted on, no algorithm quietly deciding what you hear next. Just your music, and
controls that stay in your hands.

It is a **Hypermedia course project at Hochschule Trier** (Trier University of
Applied Sciences) — a UX/UI design study taken from personas and journey maps all
the way through to a working Android app.

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Course](https://img.shields.io/badge/Hochschule%20Trier-Hypermedia-lightgrey)

---

## About the project

Most streaming apps have grown into everything-stores — social feeds, video,
promotional pushes, autoplay that never stops. Silvery is the argument for the
opposite: a focused, high-fidelity music app that strips the noise and hands the
controls back.

The work began as a UX/UI design project — research, two personas (a purist
curator and an intentional listener), a five-stage user journey, information
architecture, wireframes, and a full high-fidelity design in Figma. This
repository is the next step: a real Android front end that turns those screens
into something you can actually hold and play.

## The idea

Every screen answers to the same six principles that came out of the design work:

- **Radical minimalism** — strip ads, clutter, and anything that isn't the music.
- **Tactile control** — thumb-friendly, deep controls surfaced where you can reach
  them, one gesture per action.
- **Visual serenity** — a calm dark canvas, high-contrast type, layouts that hold
  still.
- **Queue sovereignty** — nothing autoplays, reshuffles, or hijacks your queue
  unless you ask.
- **Ethical architecture** — transparent by design, no dark patterns.
- **Sanctuary by default** — customisable when you want it, never overwhelming.

## Features

- **Music-first browsing** — Home, Discover, Library, and Search, with a mini
  player that follows you everywhere so the queue is always one tap away.
- **A player that stays out of the way** — album art crossfades to in-place lyrics
  (synced when available) without the transport controls ever moving. Swipe down
  anywhere to slide it back to your music.
- **A working equalizer** — a seven-band graphic EQ with presets that shapes the
  real audio output, not just an on-screen curve.
- **Discovery Control** — you decide how familiar or adventurous recommendations
  feel, in plain steps rather than a mystery slider.
- **Liked Songs** — heart any track and it collects in one place, yours alone.
- **Shuffle and repeat** — randomise what's coming up, or cycle repeat between off,
  the whole queue, and a single track.
- **Profiles, including a kid profile** — separate people keep separate tastes, so
  a child's listening never bleeds into the main account's recommendations.
- **Sync Play** — a listening-status pill on the player for listening along with a
  friend in real time.
- **A short "How Silvery works" tour** — shown once at first launch and reachable
  any time from Settings.

## Screens

Onboarding · Home · Discover · Library · Liked Songs · Search · Player (with
lyrics) · Queue · Discovery Control · Equalizer · Profiles · Add Profile ·
Settings.

## Built with

- **Kotlin** and **Jetpack Compose** (Material 3), single-activity, MVVM with
  `ViewModel` + `StateFlow`.
- **Navigation Compose** for the screen graph and shared transitions.
- **Media3 ExoPlayer** for playback, with an `AudioEffect` equalizer bound to the
  audio session.
- **Retrofit + OkHttp + kotlinx.serialization** for the catalog and lyrics APIs.
- **Coil** for artwork.
- **JUnit + Robolectric** for unit tests.
- Hand-rolled dependency wiring — small enough that a DI framework would be more
  ceremony than help.

## Running it locally

You'll need **JDK 17** and the Android SDK (`compileSdk 34`, `minSdk 26`). Android
Studio bundles a suitable JDK; from the command line, point `JAVA_HOME` at a
17 install.

```bash
git clone https://github.com/MundoSDL/silvery-music-app.git
cd silvery-music-app
./gradlew assembleDebug        # build
./gradlew installDebug         # install on a running device/emulator
```

**Music source (optional).** Silvery streams Creative Commons tracks from
[Jamendo](https://developer.jamendo.com/). To enable live streaming, register for a
free client id and add it to `local.properties` (which stays out of version
control):

```properties
jamendo.clientId=your_client_id_here
```

Without a key the app still runs end to end on a small bundled sample catalogue —
so it works out of the box, just offline.

## Project status

This is coursework and a concept, not a shipping product, and it's honest about
that. Accounts, profiles, and likes live in memory for the length of a session —
there's no database by design. The catalogue is real Creative Commons music from
Jamendo, with lyrics from Jamendo and LRCLIB.

See [`CHANGELOG.md`](CHANGELOG.md) for what's changed across versions.

## Design files

The full high-fidelity design lives in Figma:
[Silvery – Music](https://www.figma.com/design/YbzqjS3jEvePMBnTQVliWN/Silvery-Music).

## Credits

Music by independent artists via **Jamendo**, used under Creative Commons. Lyrics
via **Jamendo** and **LRCLIB**. Built for the **Hypermedia** course at **Hochschule
Trier**.
