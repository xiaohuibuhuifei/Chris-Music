# Chris-Music
🌽专属听歌APP

## 打包

```bash
# 打正式包（release，已签名）
./gradlew :app:assembleRelease -x lintVitalRelease
```

输出路径：`app/build/outputs/apk/release/chris-music-{版本号}.apk`

版本号在 `app/build.gradle.kts` 中的 `versionName` 字段修改。
