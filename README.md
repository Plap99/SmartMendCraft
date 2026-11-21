# Smart Mend Craft

Smart Mend Craft improves Mending with configurable repair priorities, inventory repair modes, a visual repair HUD, and Smart Hotbar.

## Structure

- `common/`: shared gameplay, UI, networking payloads, mixins, and assets.
- `fabric/`: Fabric entrypoints, networking adapter, metadata, and Gradle project.
- `forge/`: Forge entrypoints, networking adapter, metadata, and Gradle project.

Both loader builds compile from the shared `common/` source so gameplay changes stay aligned.

## Compatibility

- Minecraft 26.2
- Java 25+
- Fabric Loader 0.19.3+ with Fabric API 0.158.0+26.2
- Forge 65.1.6+
- Mod version 1.1.0

For multiplayer, install the matching loader build on both the client and server.

## Build and run

From the repository root on Windows:

```powershell
.\gradlew.bat :fabric:build
.\gradlew.bat :forge:build
.\gradlew.bat :fabric:runClient
.\gradlew.bat :forge:runClient
```

The JARs are generated separately in `fabric/build/libs/` and `forge/build/libs/`.

## License

See [LICENSE](LICENSE).
