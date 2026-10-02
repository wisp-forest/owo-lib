# Minecraft 26.3 source build

This port starts from the upstream `26.2` branch at
`9c9772e1728dd4c5f1178ce955d1b3747f53493a`. It produces
`io.wispforest:owo-lib:0.13.1+26.3` for Minecraft **26.3 exactly**.

The pinned toolchain is Java 25, Gradle 9.4.0, Fabric Loom 1.16.3,
Fabric Loader 0.19.5 and Fabric API 0.161.0+26.3. Mod Menu 21.0.0 is
a compile-time compatibility dependency. Remaining dependency pins are in
`gradle.properties` and `build.gradle`.

## Build and publish to a local dependency repository

Use a JDK 25 installation selected through `JAVA_HOME`. From a directory
containing sibling `owo-lib` and `NoFrills` source checkouts:

```bash
mkdir -p port-dependencies
port_repo="$(pwd)/port-dependencies"
cd owo-lib
bash ./gradlew :build :publishMavenJavaPublicationToPortDependenciesRepository \
  --no-daemon --max-workers=4 \
  -PportDependencyRepository="$port_repo"
```

The production and source artifacts appear in `build/libs/`. The publication
task writes the actual library JAR, sources and generated dependency POM below
`port-dependencies/io/wispforest/owo-lib/0.13.1+26.3/`.
`portDependencyRepository` is optional and accepts a path or repository URI;
there is no machine-specific default. Existing dependency and publication
repositories remain available. The explicitly named task above publishes only
the root library to the chosen local repository. It does not invoke the general
`publish` task or require remote Maven credentials.

## API changes retained by the port

- RenderPearl supplies the pipeline, GPU device/resource and backend types.
  Braid external windows use `GpuDevice.createSurface` and
  `GpuSurface.blitFromTexture`, explicit surface/renderer disposal, resizing and
  the renderer's reversed-depth clear value. The frontend texture is never cast
  to an OpenGL texture.
- Native input, window events and cursor lifetime use SDL. Event data is copied
  before the native callback returns, then dispatched on the game thread.
  Native left/right/middle mouse values are 1/3/2. Cursor identity distinguishes
  the existing `NONE` and `POINTER` options even when the native IDs coincide.
- Uniform storage uses the real mapped RenderPearl interface and combined
  sampler binding. The public `GlProgramAccessor` interface remains available;
  the registered accessor reads the map introduced by the higher-priority
  required `GlProgramMixin`, using the real uniform layout and indexed
  `GlProgram.getUniform` API.
- Recipe parsing attaches to the actual registry resource load task. Custom
  recipe remainders remain supported. The recipe accessor is a genuine field
  accessor; loot construction uses the new optional condition holder. The
  public tooltip method retains heading spacing.

No required feature or GUI was replaced with a stub, and required mixins remain
enabled.

## Validation and re-audits

The production build and access-widener validation pass. A joint static audit
with the NoFrills port checked 675 accessor/invoker, shadow, injection ownership,
handler-argument and injection-site descriptors against the official 26.3
classes. There are no missing vanilla targets. Four loader bootstrap calls and
one Fabric-injected creative-tab method are intentionally absent before loader
transformation; full runtime mixin auditing is required to validate them.

Earlier candidates passed an isolated full mixin audit and reached the title and
world-creation screens. The final registered-accessor compatibility refinement
requires a fresh isolated runtime audit before release. Compilation alone does
not prove mixin transformation or rendering behavior.

For every new Minecraft, Fabric, Loom or source revision, repeat the production
build, exact owner/descriptor/injection-site audit and complete runtime mixin
audit. Exercise the configuration UI, tooltips, text/cursors/input, Braid window
resize/close and surface disposal separately, then run alongside NoFrills and
the intended mod pack. Test OpenGL and Vulkan where supported; the Vulkan API
path compiles, but Vulkan runtime behavior has not yet been verified. Run client
validation with an isolated game directory and synthetic data, without copying
account/session data or configuring development authentication.
