# Silvery — Documentation

A condensed version of the full Design Project documentation. This covers the reasoning end to end — research through post-mortem — in enough depth to follow the design, without the length of the complete Word document submitted for the course.

**Try it live:** [appetize.io/app/b_emzzlhv7ptazji5l3h7a52mdma](https://appetize.io/app/b_emzzlhv7ptazji5l3h7a52mdma) (free tier, 3-minute sessions — relaunch if it cuts off)
**Design file:** [Figma — Silvery Music](https://www.figma.com/design/YbzqjS3jEvePMBnTQVliWN/Silvery-Music)

---

## Research question

How might a music-streaming interface put listening — rather than algorithmic engagement — back at the centre of the experience, and what specific interface and interaction decisions would that require?

Three recurring frictions motivated the project: **interface complexity** (podcasts, video, and social features layered onto a single core task), **algorithmic weight** (home screens dominated by machine-generated shelves that narrow over time — the *algorithm paradox*), and **engagement spam** (notifications and autoplay defaults optimised for time-in-app rather than a good listening session).

## Creative concept — six principles

Every screen, default, and copy decision is checked against these:

1. **Radical Minimalism** — strip ads, unsolicited content, anything that isn't music.
2. **Tactile Control** — thumb-friendly, one-gesture actions; deep controls surfaced high.
3. **Visual Serenity** — a static, high-contrast dark canvas built around album art.
4. **Queue Sovereignty** — no autoplay overrides, no algorithmic hijacking of the queue.
5. **Ethical Architecture** — transparent payouts, honest copy, no dark patterns.
6. **Sanctuary by Default** — customisable, but calm defaults that demand nothing unearned.

## Personas

**Marcus Vance — The Purist Curator.** 27, freelance sound designer. Wants guest mode, granular sub-genre filters, no algorithm overrides. Leaves if the UI bloats or core controls go behind a paywall.

**Elena Rostova — The Intentional Listener.** 22, architecture student. Wants visual calm, full-album immersion, queue sovereignty, no notifications. Leaves if the app adds video/podcast tabs or gets loud.

## Information architecture

Deliberately shallow: **Onboarding** (entry point only, never revisited) → persistent bottom nav (**Home, Discover, Library, Search**) → **Player** (reachable from any track, any tab, not a tab itself) → **Profile Switcher** (bottom sheet from the top-bar avatar) → **Settings** (Audio, Discovery, Privacy, Account).

**Must-have features:** core playback, Queue Sovereignty (autoplay off by default), a seven-band Equalizer saved per profile, Discovery Control (Familiar/Balanced/Adventurous, applies immediately), multi-profile support including an algorithm-isolated Kid profile, and in-player synced lyrics.

## Style guide, condensed

A near-black canvas (`#121212`) with two off-white text tones, one silver/white accent (`#E6E6E6`), and exactly one chromatic colour reserved for destructive actions and the Liked state (`#E0526C`). Typeface is Inter throughout, one variable font, with uppercase +1.5-tracked labels marking every group header. Shape language: 12dp cards, 17dp pills, 22dp primary CTAs, 24dp bottom sheets (top corners only).

## Logo

Two concentric circles — a silver ring (`#B8BCC4`) around a near-white core (`#F2F3F5`) — already shipping as the Android launcher icon. The name comes first: "Silvery" describes a colour and a quality of light, not an object, so the mark reads the name directly rather than needing a separate metaphor. Reads at every size without simplification; a perfect circle is the calmest shape available, which is Visual Serenity applied to the one asset with no room to be anything else.

## From concept to build

Unlike most concept-stage Design Projects, Silvery includes a working Android app (Kotlin + Jetpack Compose, Media3 ExoPlayer) streaming real Creative Commons audio from Jamendo — not static mock data. Four screens (Equalizer, Manage Profiles, Add Profile, Settings) were designed first in code, under real data constraints, then back-ported into Figma so the design file and the running app describe the same product.

## Post-mortem, condensed

**What worked:** naming the six principles before drawing a single screen turned into a genuine decision filter — Shuffle was dropped from the Player because it duplicated "I'm Feeling Lucky" and quietly worked against Queue Sovereignty; autoplay and notifications ship off by default with copy that says so.

**What's still open:** the Figma prototype isn't linked into a connected click-through yet; the Player screen in Figma still shows three action buttons where the build calls for four; Discovery Control's Figma frame still has a leftover Apply button that contradicts the apply-immediately behaviour already built. Named here rather than hidden.

**Future perspective:** the open question isn't mechanical — it's whether Silvery's principles survive contact with features that introduce a second person, since Sync Play and Blend Interests both do that to a design language built for one deliberate listener at a time.

---

*Full documentation — research, personas, scenarios, user stories, IA, feature list, style guide, logo development, wireframes, and post-mortem in complete form — is available as part of the coursework submission (Hypermedia UX/UI, Hochschule Trier).*
