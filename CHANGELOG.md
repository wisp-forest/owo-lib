# Changelog

## 0.13.0+26.2

### Minecraft 26.2 port

This release ports owo-lib to Minecraft 26.2. No intentional breaking changes to owo-lib's public API — all adaptations are for Minecraft's own API removals and changes.

### Removed

- **`OwoUIPipelines.GUI_BLUR`** — Removed custom blur render pipeline. Blur now uses Minecraft's built-in `menuBackgroundBlurriness` mechanism. Call sites using `Surface.blur()` or the Braid `Blur` widget are unaffected.
- **`BlurQuadElementRenderState` constructor and all static members** — The class is no longer a `GuiElementRenderState` record. It now serves as a simple utility with a single `static blurBackground(GuiRenderState, float, float)` method. `uniforms`, `input`, `inputView`, `initialize()`, `BlurSetup`, and `Uniforms` are all removed.
- **Custom blur shaders** (`assets/owo/shaders/core/blur.fsh`, `blur.vsh`) — No longer needed; replaced by Minecraft's built-in blur.

### Added

- **`io.wispforest.owo.util.Pair<A, B>`** — Simple pair class replacing `net.minecraft.util.Tuple` which was removed in MC 26.2. Internal use; not intended as public API.

### Changed

- **`TextOps.color(ChatFormatting)`** — Internal implementation switched from `ChatFormatting.getColor()` (removed) to a hardcoded switch. Return values unchanged.
- **`Color.ofFormatting(ChatFormatting)`** — Same as above; uses hardcoded color table instead of `ChatFormatting.getColor()`.
- **`Color.NAMED_TEXT_COLORS`** — Uses `ChatFormatting.name()` instead of `getName()` (removed); uses inline `Predicate` instead of `isColor()` (removed).
- **`EntityComponent`** — Removed `entityBuffers` field (MC 26.2 moved buffer source away from Minecraft). Added reflection-based client entity ID assignment for rendering.
- **`I18n.exists()` → `!I18n.get(key).equals(key)`** — `I18n.exists()` was removed in MC 26.2. Affected files: `ConfigScreen`, `ConfigEnumButton`.
- **`Minecraft.setScreen()` → `Minecraft.gui.setScreen()`** — Screen management moved to `Gui` class in MC 26.2. All owo-lib call sites updated.
- **`Minecraft.getInstance().screen` → `Minecraft.getInstance().gui.screen()`** — Same refactor.
- **Braid `RenderPipeline` construction** — Updated to MC 26.2's new pipeline builder API (`withBindGroupLayout`, `withPrimitiveTopology`, `withVertexBinding`). Public `OwoUIPipelines` constants (`GUI_TRIANGLE_FAN`, `GUI_TRIANGLE_STRIP`, `GUI_TEXTURED_NO_BLEND`, `GUI_HSV`) remain available.
- **`ItemGroupButton.link()`** — Internal implementation updated for `mc.gui` API.
- **Mixin targets** — `MinecraftMixin` (itemgroup) now targets `Gui.class` instead of `Minecraft.class`. `GuiMixin` (display) now targets `Hud.class` instead of `Gui.class`. Removed `LevelRendererMixin` (braid), `GuiRendererMixin` (ui), and `GlCommandEncoderAccessor`.

### Fixed

- Blur size regression when using `Surface.blur()`.
- `EntityComponent` crash due to entity ID conflicts in rendering.
- `CursedTranslatableContents` null argument handling.
- `BraidDisplay` rendering after pipeline port.

### Upgrading from 26.1

If your mod only uses owo-lib's high-level APIs (`Surface.blur()`, owo-ui components, config system, networking), no changes are required beyond updating your MC version to 26.2. If your mod directly referenced `OwoUIPipelines.GUI_BLUR` or the `BlurQuadElementRenderState` internals, update to use `BlurQuadElementRenderState.blurBackground()`.

---

## 0.13.0+26.1

Initial 26.1 release.
