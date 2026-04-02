# Chris-Music
🌽专属听歌APP

## 歌曲文件

音乐文件不纳入版本控制，需手动放置。

### 目录结构

```
app/src/main/assets/music/
├── danqu/                    ← 专辑 ID，与 manifest.json 中 albums[].id 对应
│   ├── 回春天.mp3
│   ├── 回春天.lrc            ← 歌词文件，可选
│   └── ...
├── huanghou_yu_mengxiang/
│   ├── 皇后与梦想.mp3
│   └── ...
└── ...
```

### 配置方式

在 `app/src/main/assets/manifest.json` 中配置每首歌的信息：

```json
{
  "albums": [
    {
      "id": "danqu",
      "title": "单曲",
      "artwork": "artwork/单曲.jpg",
      "songs": [
        {
          "id": "danqu_01",
          "title": "回春天",
          "fileName": "music/danqu/回春天.mp3",
          "lyricsFile": "music/danqu/回春天.lrc",
          "duration": 239,
          "trackNumber": 1
        }
      ]
    }
  ]
}
```

- `fileName` / `lyricsFile`：相对于 `assets/` 的路径
- `.lrc` 歌词文件可选，没有时不配置或留空即可

## 打包

```bash
# 打正式包（release，已签名）
./gradlew :app:assembleRelease -x lintVitalRelease
```

输出路径：`app/build/outputs/apk/release/chris-music-{版本号}.apk`

版本号在 `app/build.gradle.kts` 中的 `versionName` 字段修改。
