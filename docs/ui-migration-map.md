# UI V2 Migration Map

## Token Layer
- `RuChuTheme.tokens.spacing` for all layout spacing.
- `RuChuTheme.tokens.radius` for all corner radii.
- `RuChuTheme.tokens.opacity` for semantic alpha values.
- `RuChuTheme.tokens.icon` for icon size consistency.
- `RuChuTheme.tokens.touch` for minimum touch target standards.

## Component Mapping
- Legacy mixed top bars -> `AppTopBar` in `CoreComponentsV2.kt`.
- Page-specific action buttons -> `PrimaryActionButton` / `SecondaryActionButton`.
- Custom lyrics switch chip -> `PillToggleButton`.
- Existing list action row -> now internally composes the V2 action buttons.

## Screen Migration Status
- `HomeScreen.kt`: migrated to V2 top bar + V2 action buttons + token spacing.
- `LibraryScreen.kt`: migrated to V2 top bar + tokenized spacing.
- `AlbumDetailScreen.kt`: migrated to V2 top bar + tokenized layout and hero art container.
- `PlayerScreen.kt`: migrated to V2 top bar + tokenized control sizes and lyrics toggle button.

## Follow-up Guidelines
- New pages should not hardcode reusable dimensions or alpha values.
- Prefer V2 components before adding new UI primitives.
- If a page needs unique style, define it as a semantic token first.
