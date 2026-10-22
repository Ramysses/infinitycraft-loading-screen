# InfinityCraft Loading Screen â€” corresponding source

Minecraft 1.7.10 / Forge, based on Better Loading Screen 1.3.10 by AlexIIL.
Original source and notices are retained under GNU GPL v2; see LICENSE.
Upstream baseline: https://github.com/AlexIIL/BetterLoadingScreen_1.7/tree/1cdaef88aad41ce62a508ed5214f86f182851e13

InfinityCraft changes the background UV scale in MinecraftDisplayer.drawRect from 1/256 to 1/2048 and supplies its background/progress artwork and images.json. Historical ForgeGradle download URLs use HTTPS and immutable official Mojang object URLs.

## Build

Use Java 8 and Gradle 2.0 from the official Gradle distribution: https://services.gradle.org/distributions/gradle-2.0-bin.zip
The historical wrapper scripts/configuration are retained; the wrapper JAR and LiteLoader binary are not redistributed in this source repository. Obtain LiteLoader 1.7.10 from its author: https://www.liteloader.com/download#stable_1710 and extract its JAR into libs/liteloader-1.7.10.jar. BUILD-DEPENDENCIES.json records exact binary input hashes used for the verified local build. Do not substitute a different library without checking compatibility.

Run `gradle --no-daemon build` from this directory. ForgeGradle resolves its remaining compile inputs from the configured repositories. The local full build succeeded with these inputs and the rebuilt mod rendered the InfinityCraft loading artwork and completed client startup. No claim of byte-for-byte reproducibility is made.

## Existing approved file

CurseForge project: https://www.curseforge.com/projects/1750345
The existing 1.3.10-r1 binary was made by changing one constant in the official upstream JAR. The complete modified source is here; the local source rebuild produces a different JAR hash. This repository does not authorize any modpack release or claim completion of network authentication/world testing.

Original author: AlexIIL. InfinityCraft artwork/customization: InfinityCraft. No Minecraft game classes, player data, credentials or game runtime binaries are included.
