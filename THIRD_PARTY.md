# Third-party components — 0.2.1

This inventory is not a license grant for third-party code or map content.

- Google Navigation SDK for Android 7.9.0: https://developers.google.com/maps/documentation/navigation/android-sdk/overview . Proprietary Google Maps Platform terms apply. The complete LICENSE supplied in the 7.9.0 AAR is included as `app/src/main/assets/legal/NavigationSDK-LICENSE.txt` and is accessible from the app's licenses button. The SDK renders Google's map attribution; it must remain visible.
- Google Maps Grounding through the Gemini Developer API: https://ai.google.dev/gemini-api/docs/maps-grounding . Network service, not an embedded Gemini model. Responses and Google Maps source links are displayed on the tablet.
- Places API (New): https://developers.google.com/maps/documentation/places/web-service/place-details . Used only to resolve a selected grounded Place ID to its location. Requires the user's enabled API and billing/quota.
- AndroidX Car App Library 1.7.0 (app, app-projected, app-testing), Activity 1.9.3 and transitive AndroidX components: https://android.googlesource.com/platform/frameworks/support/ . Apache License 2.0 unless specified by the component.
- Kotlin standard library, BOM 2.2.0: https://github.com/JetBrains/kotlin . Apache License 2.0.
- Core library desugaring, desugar_jdk_libs_nio 2.1.5: https://github.com/google/desugar_jdk_libs . See upstream LICENSE and third-party notices.
- Gradle Wrapper / Gradle 8.13: https://github.com/gradle/gradle . Apache License 2.0.
- Android Gradle Plugin 8.13.2: https://android.googlesource.com/platform/tools/base/ . Apache License 2.0.
- Tests only: JUnit 4.13.2 (EPL 1.0) and Robolectric 4.14.1 (MIT): https://github.com/junit-team/junit4 and https://github.com/robolectric/robolectric .

No Google map tiles, search results, routes or production API keys are bundled in the source ZIP. They are retrieved by the SDK/API after user setup. OpenStreetMap is not used in this version. The old synthetic-map renderer was removed.
The deliberately included test signing key preserves upgrade continuity with version 0.1. It must not be used as a production signing key.

## 研究用追加物
research/upstreamに上流ソース/ライセンス、research/binariesに上流配布APKを収録。Thetaとの結合・動作保証・一括ライセンス付与ではない。research/README_JA.md参照。
