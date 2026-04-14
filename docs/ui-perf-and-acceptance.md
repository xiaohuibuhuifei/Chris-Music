# UI Performance and Acceptance Gate

## Consistency Gate
- Top bars on Home/Library/Album/Player are built from `AppTopBar`.
- Primary/secondary actions use `PrimaryActionButton` or `SecondaryActionButton`.
- Shared spacing/radius/icon/touch values come from `RuChuTheme.tokens`.

## Performance Gate (Compose)
- Avoid repeated expensive transformations in composition (example: Home album rows now memoized with `remember(...chunked(2))`).
- Keep frequently updating states isolated in mini player composables.
- Prefer stable list keys (`SongListPane` keeps `key = { it.id }`).

## Manual QA Checklist
- Home:
  - Quote card, action buttons, album grid spacing are visually aligned.
  - Sleep timer icon state color changes correctly.
- Library / Album:
  - Top bar back behavior and title truncation are correct.
  - Song list actions keep equal button height and baseline.
- Player:
  - Back navigation, slider interaction, and lyrics toggle are responsive.
  - Control buttons keep consistent touch target and icon size.

## Release Strategy
- Stage 1: internal QA build focusing on the four migrated screens.
- Stage 2: limited rollout with visual and interaction feedback.
- Stage 3: full rollout after no blocking regressions are found.
