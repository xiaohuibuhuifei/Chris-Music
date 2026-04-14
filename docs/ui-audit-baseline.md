# UI Audit Baseline (Mobile / Compose)

## Scope
- `app/src/main/java/com/ruchu/player/ui/screen/home/HomeScreen.kt`
- `app/src/main/java/com/ruchu/player/ui/screen/player/PlayerScreen.kt`
- `app/src/main/java/com/ruchu/player/ui/screen/library/LibraryScreen.kt`
- `app/src/main/java/com/ruchu/player/ui/screen/album/AlbumDetailScreen.kt`
- `app/src/main/java/com/ruchu/player/ui/components/SharedComponents.kt`
- `app/src/main/java/com/ruchu/player/ui/components/SongListComponents.kt`

## Inconsistency Findings
- Top bar styles are mixed:
  - `LibraryScreen` uses `TopAppBar`.
  - `PlayerScreen` and `AlbumDetailScreen` use custom top header blocks.
  - `HomeScreen` uses a bespoke title row.
- Action button sizing and typography differ by page:
  - `HomeScreen` action buttons use `12.sp`, custom `contentPadding`.
  - `SongListActionRow` uses `13.sp` with different spacing.
- Card shape and spacing vary without shared tokens:
  - `QuoteCard` / `AlbumCard` / list rows have independent corner and spacing values.

## Hardcoded Token Hotspots
- Spacing and sizes are embedded directly across screens/components (`4.dp`, `6.dp`, `8.dp`, `12.dp`, `16.dp`, `20.dp`, `24.dp`, etc.).
- Corner radius values are repeated (`4.dp`, `8.dp`, `16.dp`, `20.dp`).
- Opacity usage is scattered via `copy(alpha=...)` in multiple files.
- Touch target sizes are inconsistent (`40.dp`, `44.dp`, `48.dp`, `83.dp`).

## Performance Hotspots
- `HomeScreen` recomputes `albums.chunked(2)` in multiple places inside `LazyColumn`.
- `PlayerScreen` contains frequent animated elements and mixed overlays; style values are often inlined.
- Progress and mini-player states are updated frequently; shared components need stable tokens and compact recomposition surfaces.

## Refactor Baseline Targets
- Introduce theme tokens for:
  - spacing
  - radius
  - elevation
  - opacity
  - icon sizes
  - touch targets
- Replace inline button/header implementations with shared V2 components.
- Migrate all four core pages to token-driven spacing and consistent action rows.
- Add developer checklists for consistency and performance acceptance.
