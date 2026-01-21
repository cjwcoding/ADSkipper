# AndroidADSkipper

An Android accessibility service that auto-detects and clicks "skip ad" buttons on splash screens.

## Features
- Accessibility-based ad skip detection
- Keyword matching (global + per-app rules)
- Rule manager UI to customize keywords per installed app

## Requirements
- Android 7.0+ (minSdk 24)
- Accessibility service enabled by the user

## Getting Started
1. Build and install the app.
2. Open the app and enable the accessibility service in system settings.
3. Use "Rules" to add per-app keywords if needed.

## Build
```
./gradlew assembleDebug
```

## Notes
- This tool focuses on splash ads. Some in-feed/video ads may not expose a skip button in the accessibility tree.

## License
TBD
